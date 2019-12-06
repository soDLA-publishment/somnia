package nvdla

import chisel3._
import chisel3.experimental._
import chisel3.util._



// flow valid
class csc2cmac_data_if(implicit val conf: somniaConfig)  extends Bundle{
    val mask = Output(Vec(conf.CMAC_ATOMC, Bool()))
    val data = Output(Vec(conf.CMAC_ATOMC, UInt(conf.CMAC_BPE.W)))
//pd
//   field batch_index 5
//   field stripe_st 1
//   field stripe_end 1
//   field channel_end 1
//   field layer_end 1
    val pd = Output(UInt(9.W))
}


//  flow valid
class csc2cmac_wt_if(implicit val conf: somniaConfig) extends Bundle{
    val sel = Output(Vec(conf.CMAC_ATOMK, Bool()))
    val mask = Output(Vec(conf.CMAC_ATOMC, Bool()))
    val data = Output(Vec(conf.CMAC_ATOMC, UInt(conf.CMAC_BPE.W)))
}
