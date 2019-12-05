package nvdla

import chisel3._
import chisel3.util._
import chisel3.experimental._


// flow valid
class csc2cmac_data_if(implicit val conf: nvdlaConfig)  extends Bundle{
    val mask = Output(Vec(conf.CMAC_ATOMC, Bool()))
    val data = Output(Vec(conf.CMAC_ATOMC, UInt(conf.NVDLA_BPE.W)))
//pd
//   field batch_index 5
//   field stripe_st 1
//   field stripe_end 1
//   field channel_end 1
//   field layer_end 1
    val pd = Output(UInt(9.W))
}


//  flow valid
class csc2cmac_wt_if(implicit val conf: nvdlaConfig) extends Bundle{
    val sel = Output(Vec(conf.CMAC_ATOMK, Bool()))
    val mask = Output(Vec(conf.CMAC_ATOMC, Bool()))
    val data = Output(Vec(conf.CMAC_ATOMC, UInt(conf.NVDLA_BPE.W)))
}


//  flow valid
class cmac2cacc_if(implicit val conf: nvdlaConfig) extends Bundle{
    val mask = Output(Vec(conf.CMAC_ATOMK, Bool()))
    val data = Output(Vec(conf.CMAC_ATOMK, UInt(conf.NVDLA_MAC_RESULT_WIDTH.W)))
    //val mode = Output(Bool())
//pd
//   field batch_index 5
//   field stripe_st 1
//   field stripe_end 1
//   field channel_end 1
//   field layer_end 1
    val pd = Output(UInt(9.W))
}



class nvdla_dma_wr_rsp_if(implicit val conf: nvdlaConfig) extends Bundle{
    val complete = Output(Bool())
}


class csb2dp_if extends Bundle{
    val req = Flipped(ValidIO(UInt(63.W)))
    val resp = ValidIO(UInt(34.W))
}

class nvdla_clock_if extends Bundle{
    val nvdla_core_clk = Output(Clock())
    val dla_clk_ovr_on_sync = Output(Clock())
    val global_clk_ovr_on_sync = Output(Clock())
    val tmc2slcg_disable_clock_gating = Output(Bool())
}

// Register control interface
class reg_control_if extends Bundle{
    val rd_data = Output(UInt(32.W))
    val offset = Input(UInt(12.W))
    val wr_data = Input(UInt(32.W))
    val wr_en = Input(Bool())
}


class updt_entries_slices_if(implicit val conf: nvdlaConfig) extends Bundle{
    val entries = Output(UInt(conf.CSC_ENTRIES_NUM_WIDTH.W))
    val slices = Output(UInt(14.W))
}

class updt_entries_kernels_if(implicit val conf: nvdlaConfig) extends Bundle{
    val entries = Output(UInt(conf.CSC_ENTRIES_NUM_WIDTH.W))
    val kernels = Output(UInt(14.W))
}

class nvdla_wr_if(addr_width:Int, width:Int) extends Bundle{
    val addr = ValidIO(UInt(addr_width.W))
    val data = Output(UInt(width.W))

    override def cloneType: this.type =
    new nvdla_wr_if(addr_width:Int, width:Int).asInstanceOf[this.type]
}

class nvdla_rd_if(addr_width:Int, width:Int) extends Bundle{
    val addr = ValidIO(UInt(addr_width.W))
    val data = Input(UInt(width.W))

    override def cloneType: this.type =
    new nvdla_rd_if(addr_width:Int, width:Int).asInstanceOf[this.type]
}



















