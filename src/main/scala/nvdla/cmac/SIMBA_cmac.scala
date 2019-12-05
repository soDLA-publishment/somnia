package nvdla

import chisel3._
import chisel3.experimental._
import chisel3.util._


class SIMBA_cmac(implicit val conf: simbaConfig) extends Module {
    val io = IO(new Bundle {
        //general clock
        val simba_clock = Flipped(new simba_clock_if)
        val simba_core_rstn = Input(Bool())
        //csb
        val csb2cmac_a = new csb2dp_if
        //odif
        val mac2accu = ValidIO(new cmac2cacc_if) /* data valid */
        val sc2mac_dat = Flipped(ValidIO(new csc2cmac_data_if))  /* data valid */
        val sc2mac_wt = Flipped(ValidIO(new csc2cmac_wt_if))    /* data valid */
        
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

withReset(!io.simba_core_rstn){
    
    //==========================================================
    // core
    //==========================================================
    //==========================================================
    // reg
    //==========================================================
    val u_core = Module(new SIMBA_CMAC_core)
    val u_reg = Module(new SIMBA_CMAC_reg)
    //clk
    u_core.io.simba_clock <> io.simba_clock         //|< b
    u_reg.io.simba_core_clk := io.simba_clock.simba_core_clk        //|< i
    u_core.io.slcg_op_en := u_reg.io.slcg_op_en           
    
    u_core.io.sc2mac_dat <> io.sc2mac_dat               //|< b
    u_core.io.sc2mac_wt <> io.sc2mac_wt         //|< b
    io.mac2accu <> u_core.io.mac2accu                 //|> b
      
    u_reg.io.dp2reg_done := u_core.io.dp2reg_done       //|< i
    u_reg.io.csb2cmac_a <> io.csb2cmac_a        //|< b

}}


object SIMBA_cmacDriver extends App {
  implicit val conf: simbaConfig = new simbaConfig
  chisel3.Driver.execute(args, () => new SIMBA_cmac())
}
