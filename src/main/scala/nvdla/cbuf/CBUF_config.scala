package nvdla

import chisel3._
import chisel3.experimental._
import chisel3.util._

class cbufConfiguration extends caccConfiguration
{
    

}


// class router2buf_wr_if(implicit val conf: somniaConfig) extends Bundle{
//     val en = Output(Bool())
//     val sel = Output(UInt(conf.CBUF_WR_BANK_SEL_WIDTH.W))
//     val addr = Output(UInt(conf.CBUF_ADDR_WIDTH.W)))
//     val data = Output(UInt(conf.CBUF_WR_PORT_WIDTH.W))
// }