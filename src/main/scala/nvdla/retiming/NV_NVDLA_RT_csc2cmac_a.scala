package nvdla

import chisel3._
import chisel3.experimental._
import chisel3.util._


class NV_NVDLA_RT_csc2cmac_a(delay: Int)(implicit val conf: nvdlaConfig) extends Module {
    val io = IO(new Bundle {
        //general clock
        val nvdla_core_clk = Input(Clock())
        val nvdla_core_rstn = Input(Bool())

        //src
        val sc2mac_dat_src = Flipped(ValidIO(new csc2cmac_data_if))
        val sc2mac_wt_src = Flipped(ValidIO(new csc2cmac_wt_if))

        //dst
        val sc2mac_dat_dst = ValidIO(new csc2cmac_data_if)
        val sc2mac_wt_dst = ValidIO(new csc2cmac_wt_if)
})

withClockAndReset(io.nvdla_core_clk, !io.nvdla_core_rstn){

    val sc2mac_dat_pvld_d = retiming(Bool(), delay)
    val sc2mac_dat_mask_d = retiming(Vec(conf.NVDLA_MAC_ATOMIC_C_SIZE, Bool()), delay)
    val sc2mac_dat_data_d = retiming(Vec(conf.NVDLA_MAC_ATOMIC_C_SIZE, UInt(conf.NVDLA_BPE.W)), delay)
    val sc2mac_dat_pd_d = retiming(UInt(9.W), delay)

    val sc2mac_wt_pvld_d = retiming(Bool(), delay)
    val sc2mac_wt_mask_d = retiming(Vec(conf.NVDLA_MAC_ATOMIC_C_SIZE, Bool()), delay)
    val sc2mac_wt_data_d = retiming(Vec(conf.NVDLA_MAC_ATOMIC_C_SIZE, UInt(conf.NVDLA_BPE.W)), delay)
    val sc2mac_wt_sel_d = retiming(Vec(conf.NVDLA_MAC_ATOMIC_K_SIZE_DIV2, Bool()), delay)
    
    //assign input port
    sc2mac_dat_pvld_d(0) := io.sc2mac_dat_src.valid
    sc2mac_dat_mask_d(0) := io.sc2mac_dat_src.bits.mask
    sc2mac_dat_data_d(0) := io.sc2mac_dat_src.bits.data
    sc2mac_dat_pd_d(0) := io.sc2mac_dat_src.bits.pd

    sc2mac_wt_pvld_d(0) := io.sc2mac_wt_src.valid
    sc2mac_wt_mask_d(0) := io.sc2mac_wt_src.bits.mask
    sc2mac_wt_data_d(0) := io.sc2mac_wt_src.bits.data
    sc2mac_wt_sel_d(0) := io.sc2mac_wt_src.bits.sel

    //data flight
    for(t <- 0 to delay-1){
        //dat
        sc2mac_dat_pvld_d(t+1) := sc2mac_dat_pvld_d(t)  
        when(sc2mac_dat_pvld_d(t+1)|sc2mac_dat_pvld_d(t)){
            sc2mac_dat_pd_d(t+1) := sc2mac_dat_pd_d(t)
            sc2mac_dat_mask_d(t+1) := sc2mac_dat_mask_d(t)
        } 
        for(i <- 0 to conf.NVDLA_MAC_ATOMIC_C_SIZE-1){
            when(sc2mac_dat_mask_d(t)(i)){
                sc2mac_dat_data_d(t+1)(i):= sc2mac_dat_data_d(t)(i)
            }               
        }
        //wt 
        sc2mac_wt_pvld_d(t+1) := sc2mac_wt_pvld_d(t)  
        when(sc2mac_wt_pvld_d(t+1)|sc2mac_wt_pvld_d(t)){
            sc2mac_wt_sel_d(t+1) := sc2mac_wt_sel_d(t)
            sc2mac_wt_mask_d(t+1) := sc2mac_wt_mask_d(t)
        } 
        for(i <- 0 to conf.NVDLA_MAC_ATOMIC_C_SIZE-1){
            when(sc2mac_wt_mask_d(t)(i)){
                sc2mac_wt_data_d(t+1)(i):= sc2mac_wt_data_d(t)(i)
            }               
        }     
    }  

    //output assignment
    io.sc2mac_dat_dst.valid := sc2mac_dat_pvld_d(delay)
    io.sc2mac_dat_dst.bits.mask := sc2mac_dat_mask_d(delay)
    io.sc2mac_dat_dst.bits.data := sc2mac_dat_data_d(delay)
    io.sc2mac_dat_dst.bits.pd := sc2mac_dat_pd_d(delay)

    io.sc2mac_wt_dst.valid := sc2mac_wt_pvld_d(delay)
    io.sc2mac_wt_dst.bits.mask := sc2mac_wt_mask_d(delay)
    io.sc2mac_wt_dst.bits.data := sc2mac_wt_data_d(delay)
    io.sc2mac_wt_dst.bits.sel := sc2mac_wt_sel_d(delay)

  }}