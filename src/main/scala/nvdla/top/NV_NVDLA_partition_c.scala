package nvdla

import chisel3._
import chisel3.experimental._
import chisel3.util._

class NV_NVDLA_partition_c(implicit val conf: nvdlaConfig) extends Module {
    val io = IO(new Bundle {
        //general clock
        val nvdla_core_clk = Input(Clock())
        val dla_reset_rstn = Input(Bool())
        val test_mode = Input(Bool())
        val direct_reset_ = Input(Bool())
        val nvdla_clk_ovr_on = Input(Clock())
        val global_clk_ovr_on = Input(Clock())
        val tmc2slcg_disable_clock_gating = Input(Bool())

        //csb
        val csb2cdma = new csb2dp_if 
        val csb2csc = new csb2dp_if
        //csb cross-through
        val csb2cacc_src = if(conf.NVDLA_RETIMING_ENABLE) Some(Flipped(new csb2dp_if)) else None
        val csb2cacc_dst = if(conf.NVDLA_RETIMING_ENABLE) Some(new csb2dp_if) else None

        val csb2cmac_b_src = if(conf.NVDLA_RETIMING_ENABLE) Some(Flipped(new csb2dp_if)) else None
        val csb2cmac_b_dst = if(conf.NVDLA_RETIMING_ENABLE) Some(new csb2dp_if) else None

        //accu2sc
        val accu2sc_credit_size = Flipped(ValidIO(UInt(3.W)))

        //2glb
        val cdma_dat2glb_done_intr_pd = Output(UInt(2.W))
        val cdma_wt2glb_done_intr_pd = Output(UInt(2.W))

        val cacc2glb_done_intr_src_pd = if(conf.NVDLA_RETIMING_ENABLE) Some(Input(UInt(2.W))) else None
        val cacc2glb_done_intr_dst_pd = if(conf.NVDLA_RETIMING_ENABLE) Some(Output(UInt(2.W))) else None

        //mcif
        val cdma_dat2mcif_rd_req_pd = DecoupledIO(UInt(conf.NVDLA_CDMA_MEM_RD_REQ.W))
        val mcif2cdma_dat_rd_rsp_pd = Flipped(DecoupledIO(UInt(conf.NVDLA_CDMA_MEM_RD_RSP.W)))

        val cdma_wt2mcif_rd_req_pd = DecoupledIO(UInt(conf.NVDLA_CDMA_MEM_RD_REQ.W))
        val mcif2cdma_wt_rd_rsp_pd = Flipped(DecoupledIO(UInt(conf.NVDLA_CDMA_MEM_RD_RSP.W)))

        //cvif
        val cdma_dat2cvif_rd_req_pd = if(conf.NVDLA_SECONDARY_MEMIF_ENABLE) Some(DecoupledIO(UInt(conf.NVDLA_CDMA_MEM_RD_REQ.W))) else None
        val cvif2cdma_dat_rd_rsp_pd = if(conf.NVDLA_SECONDARY_MEMIF_ENABLE) Some(Flipped(DecoupledIO(UInt(conf.NVDLA_CDMA_MEM_RD_RSP.W)))) else None

        val cdma_wt2cvif_rd_req_pd = if(conf.NVDLA_SECONDARY_MEMIF_ENABLE) Some(DecoupledIO(UInt(conf.NVDLA_CDMA_MEM_RD_REQ.W))) else None
        val cvif2cdma_wt_rd_rsp_pd = if(conf.NVDLA_SECONDARY_MEMIF_ENABLE) Some(Flipped(DecoupledIO(UInt(conf.NVDLA_CDMA_MEM_RD_RSP.W)))) else None
        
        //mac_dat & wt
        val sc2mac_dat_a = ValidIO(new csc2cmac_data_if)    
        val sc2mac_dat_b = ValidIO(new csc2cmac_data_if)    

        //mac_wt
        val sc2mac_wt_a = ValidIO(new csc2cmac_wt_if)    
        val sc2mac_wt_b = ValidIO(new csc2cmac_wt_if)    

        val pwrbus_ram_pd = Input(UInt(32.W))

           
    })
//     
//          ┌─┐       ┌─┐
//       ┌──┘ ┴───────┘ ┴──┐
//       │                 │
//       │       ───       │
//       │  ─┬┘       └┬─  │
//       │                 │
//       │       ─┴─       │
//       │                 │
//       └───┐         ┌───┘
//           │         │
//           │         │
//           │         │
//           │         └──────────────┐
//           │                        │
//           │                        ├─┐
//           │                        ┌─┘    
//           │                        │
//           └─┐  ┐  ┌───────┬──┐  ┌──┘         
//             │ ─┤ ─┤       │ ─┤ ─┤         
//             └──┴──┘       └──┴──┘ 
    val u_NV_NVDLA_RT_csc2cmac_b = if(conf.NVDLA_RETIMING_ENABLE)
                                  Some(Module(new NV_NVDLA_RT_csc2cmac_a(conf.RT_CSC2CMAC_B_LATENCY)))
                                  else None 
    val u_NV_NVDLA_RT_csb2cmac = if(conf.NVDLA_RETIMING_ENABLE)
                                 Some(Module(new NV_NVDLA_RT_csb2dp(3)))
                                 else None 
    val u_NV_NVDLA_RT_csb2cacc = if(conf.NVDLA_RETIMING_ENABLE)
                                 Some(Module(new NV_NVDLA_RT_csb2dp(3)))
                                 else None 
    val u_NV_NVDLA_RT_cacc2glb = if(conf.NVDLA_RETIMING_ENABLE)
                                 Some(Module(new NV_NVDLA_RT_dp2glb(2)))
                                 else None 
    val u_NV_NVDLA_cdma = Module(new NV_NVDLA_cdma)  
    val u_NV_NVDLA_cbuf = Module(new NV_NVDLA_cbuf)
    val u_NV_NVDLA_csc = Module(new NV_NVDLA_csc)
    ////////////////////////////////////////////////////////////////////////
    //  NVDLA Partition C:    Reset Sync                                  //
    ////////////////////////////////////////////////////////////////////////
    val u_partition_c_reset = Module(new NV_NVDLA_reset)
    u_partition_c_reset.io.nvdla_clk  := io.nvdla_core_clk
    u_partition_c_reset.io.dla_reset_rstn := io.dla_reset_rstn
    u_partition_c_reset.io.direct_reset_ := io.direct_reset_
    u_partition_c_reset.io.test_mode := io.test_mode
    val nvdla_core_rstn = u_partition_c_reset.io.synced_rstn

    ////////////////////////////////////////////////////////////////////////
    // SLCG override
    ////////////////////////////////////////////////////////////////////////
    val u_csc_dla_clk_ovr_on_sync = Module(new NV_NVDLA_sync3d)
    u_csc_dla_clk_ovr_on_sync.io.clk := io.nvdla_core_clk
    u_csc_dla_clk_ovr_on_sync.io.sync_i := io.nvdla_clk_ovr_on
    val csc_dla_clk_ovr_on_sync = u_csc_dla_clk_ovr_on_sync.io.sync_o 

    val u_cdma_dla_clk_ovr_on_sync = Module(new NV_NVDLA_sync3d)
    u_cdma_dla_clk_ovr_on_sync.io.clk := io.nvdla_core_clk
    u_cdma_dla_clk_ovr_on_sync.io.sync_i := io.nvdla_clk_ovr_on
    val cdma_dla_clk_ovr_on_sync = u_cdma_dla_clk_ovr_on_sync.io.sync_o 

    val u_global_csc_clk_ovr_on_sync = Module(new NV_NVDLA_sync3d_s)
    u_global_csc_clk_ovr_on_sync.io.clk := io.nvdla_core_clk
    u_global_csc_clk_ovr_on_sync.io.prst := nvdla_core_rstn
    u_global_csc_clk_ovr_on_sync.io.sync_i := io.global_clk_ovr_on
    val csc_global_clk_ovr_on_sync = u_global_csc_clk_ovr_on_sync.io.sync_o 

    val u_global_cdma_clk_ovr_on_sync = Module(new NV_NVDLA_sync3d_s)
    u_global_cdma_clk_ovr_on_sync.io.clk := io.nvdla_core_clk
    u_global_cdma_clk_ovr_on_sync.io.prst := nvdla_core_rstn
    u_global_cdma_clk_ovr_on_sync.io.sync_i := io.global_clk_ovr_on
    val cdma_global_clk_ovr_on_sync = u_global_cdma_clk_ovr_on_sync.io.sync_o 

    ////////////////////////////////////////////////////////////////////////
    // Retiming cell
    ////////////////////////////////////////////////////////////////////////
    if(conf.NVDLA_RETIMING_ENABLE){
        //Retiming path csc->cmac_b 
        u_NV_NVDLA_RT_csc2cmac_b.get.io.nvdla_core_clk := io.nvdla_core_clk
        u_NV_NVDLA_RT_csc2cmac_b.get.io.nvdla_core_rstn := nvdla_core_rstn
        u_NV_NVDLA_RT_csc2cmac_b.get.io.sc2mac_wt_src <> u_NV_NVDLA_csc.io.sc2mac_wt_b
        u_NV_NVDLA_RT_csc2cmac_b.get.io.sc2mac_dat_src <> u_NV_NVDLA_csc.io.sc2mac_dat_b

        //Retiming path csb->cmac_b
        u_NV_NVDLA_RT_csb2cmac.get.io.nvdla_core_clk := io.nvdla_core_clk
        u_NV_NVDLA_RT_csb2cmac.get.io.nvdla_core_rstn := nvdla_core_rstn
        u_NV_NVDLA_RT_csb2cmac.get.io.csb2dp_src <> io.csb2cmac_b_src.get
        io.csb2cmac_b_dst.get <> u_NV_NVDLA_RT_csb2cmac.get.io.csb2dp_dst

        //Retiming path csb<->cacc 
        u_NV_NVDLA_RT_csb2cacc.get.io.nvdla_core_clk := io.nvdla_core_clk
        u_NV_NVDLA_RT_csb2cacc.get.io.nvdla_core_rstn := nvdla_core_rstn
        u_NV_NVDLA_RT_csb2cacc.get.io.csb2dp_src <> io.csb2cacc_src.get
        io.csb2cacc_dst.get <> u_NV_NVDLA_RT_csb2cacc.get.io.csb2dp_dst

        //Retiming path cacc->glbc
        u_NV_NVDLA_RT_cacc2glb.get.io.nvdla_core_clk := io.nvdla_core_clk
        u_NV_NVDLA_RT_cacc2glb.get.io.nvdla_core_rstn := nvdla_core_rstn
        u_NV_NVDLA_RT_cacc2glb.get.io.dp2glb_done_intr_src_pd := io.cacc2glb_done_intr_src_pd.get
        io.cacc2glb_done_intr_dst_pd.get := u_NV_NVDLA_RT_cacc2glb.get.io.dp2glb_done_intr_dst_pd

    }   

    ////////////////////////////////////////////////////////////////////////
    //  NVDLA Partition C:    Convolution DMA                             //
    ////////////////////////////////////////////////////////////////////////
    u_NV_NVDLA_cdma.io.nvdla_clock.nvdla_core_clk := io.nvdla_core_clk
    u_NV_NVDLA_cdma.io.nvdla_core_rstn := nvdla_core_rstn
    u_NV_NVDLA_cdma.io.nvdla_clock.dla_clk_ovr_on_sync := cdma_dla_clk_ovr_on_sync
    u_NV_NVDLA_cdma.io.nvdla_clock.global_clk_ovr_on_sync := cdma_global_clk_ovr_on_sync
    u_NV_NVDLA_cdma.io.nvdla_clock.tmc2slcg_disable_clock_gating := io.tmc2slcg_disable_clock_gating     

    //csb
    io.csb2cdma <> u_NV_NVDLA_cdma.io.csb2cdma

    //glb
    io.cdma_dat2glb_done_intr_pd := u_NV_NVDLA_cdma.io.cdma_dat2glb_done_intr_pd
    io.cdma_wt2glb_done_intr_pd := u_NV_NVDLA_cdma.io.cdma_wt2glb_done_intr_pd

    //mcif
    io.cdma_dat2mcif_rd_req_pd <> u_NV_NVDLA_cdma.io.cdma_dat2mcif_rd_req_pd
    u_NV_NVDLA_cdma.io.mcif2cdma_dat_rd_rsp_pd <> io.mcif2cdma_dat_rd_rsp_pd

    io.cdma_wt2mcif_rd_req_pd <> u_NV_NVDLA_cdma.io.cdma_wt2mcif_rd_req_pd
    u_NV_NVDLA_cdma.io.mcif2cdma_wt_rd_rsp_pd <> io.mcif2cdma_wt_rd_rsp_pd

    //cvif
    if(conf.NVDLA_SECONDARY_MEMIF_ENABLE){ 
        io.cdma_dat2cvif_rd_req_pd.get <> u_NV_NVDLA_cdma.io.cdma_dat2cvif_rd_req_pd.get
        u_NV_NVDLA_cdma.io.cvif2cdma_dat_rd_rsp_pd.get <> io.cvif2cdma_dat_rd_rsp_pd.get

        io.cdma_wt2cvif_rd_req_pd.get <> u_NV_NVDLA_cdma.io.cdma_wt2cvif_rd_req_pd.get
        u_NV_NVDLA_cdma.io.cvif2cdma_wt_rd_rsp_pd.get <> io.cvif2cdma_wt_rd_rsp_pd.get
    }



    val cdma2sc_dat_pending_ack = u_NV_NVDLA_cdma.io.cdma2sc_dat_pending_ack
    val cdma2sc_wt_pending_ack = u_NV_NVDLA_cdma.io.cdma2sc_wt_pending_ack

    //pwrbus
    u_NV_NVDLA_cdma.io.pwrbus_ram_pd := io.pwrbus_ram_pd

    ////////////////////////////////////////////////////////////////////////
    //  NVDLA Partition C:    Convolution Buffer                         //
    ////////////////////////////////////////////////////////////////////////
    

    u_NV_NVDLA_cbuf.io.nvdla_core_clk := io.nvdla_core_clk
    u_NV_NVDLA_cbuf.io.nvdla_core_rstn := nvdla_core_rstn
    u_NV_NVDLA_cbuf.io.pwrbus_ram_pd := io.pwrbus_ram_pd

    if(conf.NVDLA_CC_ATOMC_DIV_ATOMK == 1){
        u_NV_NVDLA_cbuf.io.cdma2buf_wr.sel(0) := Fill(conf.CBUF_WR_BANK_SEL_WIDTH, true.B)
        u_NV_NVDLA_cbuf.io.cdma2buf_wr.sel(1) := Fill(conf.CBUF_WR_BANK_SEL_WIDTH, true.B)
    }

    if(conf.NVDLA_CC_ATOMC_DIV_ATOMK == 2){
        u_NV_NVDLA_cbuf.io.cdma2buf_wr.sel(0) := u_NV_NVDLA_cdma.io.cdma2buf_dat_wr_sel.get
        u_NV_NVDLA_cbuf.io.cdma2buf_wr.sel(1) := u_NV_NVDLA_cdma.io.cdma2buf_wt_wr_sel.get
    }

    if(conf.NVDLA_CC_ATOMC_DIV_ATOMK == 4){
        u_NV_NVDLA_cbuf.io.cdma2buf_wr.sel(0) := u_NV_NVDLA_cdma.io.cdma2buf_dat_wr_sel.get
        u_NV_NVDLA_cbuf.io.cdma2buf_wr.sel(1) := u_NV_NVDLA_cdma.io.cdma2buf_wt_wr_sel.get
    }


    ////////////////////////////////////////////////////////////////////////
    //  NVDLA Partition C:    Convolution Sequence Controller             //
    ////////////////////////////////////////////////////////////////////////

    
    //clock
    u_NV_NVDLA_csc.io.nvdla_clock.nvdla_core_clk := io.nvdla_core_clk
    u_NV_NVDLA_csc.io.nvdla_core_rstn := nvdla_core_rstn
    u_NV_NVDLA_csc.io.nvdla_clock.dla_clk_ovr_on_sync := csc_dla_clk_ovr_on_sync
    u_NV_NVDLA_csc.io.nvdla_clock.global_clk_ovr_on_sync := csc_global_clk_ovr_on_sync   
    u_NV_NVDLA_csc.io.nvdla_clock.tmc2slcg_disable_clock_gating := io.tmc2slcg_disable_clock_gating


    u_NV_NVDLA_csc.io.accu2sc_credit_size <> io.accu2sc_credit_size

    //csb
    io.csb2csc <> u_NV_NVDLA_csc.io.csb2csc

    //cdma
    u_NV_NVDLA_csc.io.cdma2sc_dat_updt <> u_NV_NVDLA_cdma.io.cdma2sc_dat_updt
    u_NV_NVDLA_cdma.io.sc2cdma_dat_updt <> u_NV_NVDLA_csc.io.sc2cdma_dat_updt
    u_NV_NVDLA_cdma.io.sc2cdma_dat_pending_req := u_NV_NVDLA_csc.io.sc2cdma_dat_pending_req
    u_NV_NVDLA_cdma.io.sc2cdma_wt_pending_req := u_NV_NVDLA_csc.io.sc2cdma_wt_pending_req
    u_NV_NVDLA_csc.io.cdma2sc_dat_pending_ack := u_NV_NVDLA_cdma.io.cdma2sc_dat_pending_ack
    u_NV_NVDLA_csc.io.cdma2sc_wt_pending_ack := u_NV_NVDLA_cdma.io.cdma2sc_wt_pending_ack

    //cbuf

    u_NV_NVDLA_cbuf.io.cdma2buf_wr.en(0) := u_NV_NVDLA_cdma.io.cdma2buf_dat_wr.addr.valid
    u_NV_NVDLA_cbuf.io.cdma2buf_wr.addr(0) := u_NV_NVDLA_cdma.io.cdma2buf_dat_wr.addr.bits(conf.CBUF_ADDR_WIDTH-1, 0)
    u_NV_NVDLA_cbuf.io.cdma2buf_wr.data(0) := u_NV_NVDLA_cdma.io.cdma2buf_dat_wr.data

    u_NV_NVDLA_cbuf.io.cdma2buf_wr.en(1) := u_NV_NVDLA_cdma.io.cdma2buf_wt_wr.addr.valid
    u_NV_NVDLA_cbuf.io.cdma2buf_wr.addr(1) := u_NV_NVDLA_cdma.io.cdma2buf_wt_wr.addr.bits(conf.CBUF_ADDR_WIDTH-1, 0)
    u_NV_NVDLA_cbuf.io.cdma2buf_wr.data(1) := u_NV_NVDLA_cdma.io.cdma2buf_wt_wr.data


    u_NV_NVDLA_cbuf.io.sc2buf_dat_rd <> u_NV_NVDLA_csc.io.sc2buf_dat_rd  
    u_NV_NVDLA_cbuf.io.sc2buf_wt_rd <> u_NV_NVDLA_csc.io.sc2buf_wt_rd          


    //mac_dat & wt
    if(conf.NVDLA_RETIMING_ENABLE){
        io.sc2mac_dat_b <> u_NV_NVDLA_RT_csc2cmac_b.get.io.sc2mac_dat_dst
        io.sc2mac_wt_b <> u_NV_NVDLA_RT_csc2cmac_b.get.io.sc2mac_wt_dst
    }
    else{
        io.sc2mac_dat_b <> u_NV_NVDLA_csc.io.sc2mac_dat_b
        io.sc2mac_wt_b <> u_NV_NVDLA_csc.io.sc2mac_wt_b
    }

    io.sc2mac_dat_a <> u_NV_NVDLA_csc.io.sc2mac_dat_a
    io.sc2mac_wt_a <> u_NV_NVDLA_csc.io.sc2mac_wt_a
    
    u_NV_NVDLA_csc.io.cdma2sc_wt_updt <> u_NV_NVDLA_cdma.io.cdma2sc_wt_updt
    u_NV_NVDLA_csc.io.cdma2sc_wmb_entries := 0.U
    u_NV_NVDLA_cdma.io.sc2cdma_wt_updt <> u_NV_NVDLA_csc.io.sc2cdma_wt_updt      

    u_NV_NVDLA_csc.io.pwrbus_ram_pd := io.pwrbus_ram_pd


}


object NV_NVDLA_partition_cDriver extends App {
  implicit val conf: nvdlaConfig = new nvdlaConfig
  chisel3.Driver.execute(args, () => new NV_NVDLA_partition_c())
}
