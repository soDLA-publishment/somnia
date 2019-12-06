package nvdla

import chisel3._
import chisel3.util._
import chisel3.experimental._


//  flow valid
class cmac2cacc_if(implicit val conf: somniaConfig) extends Bundle{
    val mask = Output(Vec(conf.CMAC_ATOMK, Bool()))
    val data = Output(Vec(conf.CMAC_ATOMK, UInt(conf.CMAC_RESULT_WIDTH.W)))
    //val mode = Output(Bool())
//pd
//   field batch_index 5
//   field stripe_st 1
//   field stripe_end 1
//   field channel_end 1
//   field layer_end 1
    val pd = Output(UInt(9.W))
}

class csb2dp_if extends Bundle{
    val req = Flipped(ValidIO(UInt(63.W)))
    val resp = ValidIO(UInt(34.W))
}

class somnia_clock_if extends Bundle{
    val somnia_core_clk = Output(Clock())
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




















