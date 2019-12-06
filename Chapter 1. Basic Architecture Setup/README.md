# Basic Architecture Setup

A new kind of computational architecture starts from sketching up the data flow(in a piece of paper, or in your mind), 
setup basic configurations(BPE(bandwidth per element), number of resources), and standard interface design. 

## Setup Configurations

In the src/main/your-project-name, create a spec folder, create a basic configuration file, like this:

```
package nvdla

class nv_simba
{
  val RETIMING_ENABLE = false
  val SIMBA_BPE = 8
  val SPLIT_NUM = 4
  val MAC_ATOMIC_C_SIZE = 32
  val MAC_ATOMIC_K_SIZE = 32
}
```

Then, create an advanced configuration file as an extension, specifing the calculation steps from basic configuraion file.

```
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
 

```

In the configuration tree, we use a ring structure, namely, the configuration is like a ring, 

basic configurations -> advanced configurations -> module a configurations -> module b configurations -> project configurations. To avoid parameters in module a and parameters in module b are messed up, use a style of A_parameter_this and B_parameter_this, to make a separate. 

I like to call it 'fixed rope', which is a term in mountain climbing.

## Setup Standard Interface

A set of standard interface can be built out of Bundle, see this(https://github.com/freechipsproject/chisel3/wiki/Bundles-and-Vecs).
In nvdla, an Open DLA Interface Definition was given here https://github.com/nvdla/hw/tree/master/spec/odif.

We create a set of bundles like this:

```
package nvdla

import chisel3._
import chisel3.util._
import chisel3.experimental._


// flow valid
class csc2cmac_data_if(implicit val conf: simbaConfig)  extends Bundle{
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
class csc2cmac_wt_if(implicit val conf: simbaConfig) extends Bundle{
    val sel = Output(Vec(conf.CMAC_ATOMK, Bool()))
    val mask = Output(Vec(conf.CMAC_ATOMC, Bool()))
    val data = Output(Vec(conf.CMAC_ATOMC, UInt(conf.CMAC_BPE.W)))
}
```







