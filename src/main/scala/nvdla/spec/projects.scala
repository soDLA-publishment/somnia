package nvdla

import chisel3._
import chisel3.experimental._
import chisel3.util._
import scala.math._


class project_spec extends nv_simba
{
    val PE_MAC_ATOMIC_C_SIZE = MAC_ATOMIC_C_SIZE/SPLIT_NUM
    val PE_MAC_ATOMIC_K_SIZE = MAC_ATOMIC_K_SIZE/SPLIT_NUM
    val PE_MAC_RESULT_WIDTH = 2*SIMBA_BPE + log2Ceil(PE_MAC_ATOMIC_C_SIZE)
}
 

