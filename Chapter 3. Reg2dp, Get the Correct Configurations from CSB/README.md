# Reg2dp, Get the Correct Configurations from CSB

Originally, I want to name to this chapter 'Ping-pong Synchronization Mechanism' from http://nvdla.org/hw/v1/hwarch.html, mainly discuss about how the discrete processor perform the correct behavior at the specified time. If I mainly talk about how ping-pong works in this chapter, you might think I'm just repeating. But I really encourage you to read about 'Ping-pong Synchronization Mechanism'.


## Add csb2dp, register control interface in your standard interface file

In the src/main/your-project-name/your-standard-interfaces, add the following:

csb2dp,

```
class csb2dp_if extends Bundle{
    val req = Flipped(ValidIO(UInt(63.W)))
    val resp = ValidIO(UInt(34.W))
}
```

register control interface,

```
class reg_control_if extends Bundle{
    val rd_data = Output(UInt(32.W))
    val offset = Input(UInt(12.W))
    val wr_data = Input(UInt(32.W))
    val wr_en = Input(Bool())
}

```

csb2dp is the connection between Configuration Space Bus and discrete processors, [csb to register control logic](https://github.com/soDLA-publishment/soDLA/blob/soDLA_beta/src/main/scala/slibs/NV_NVDLA_CSB_LOGIC.scala) will generate the register control information, tell ping-pong register which parameter to pass to discrete processors or receive from discrete processors.

```
    csb
    /\
    ||
    \/
    register control
    /\
    ||
    \/
    ping-pong registers
    /\
    ||
    \/
    discrete processors
    
```


## Setup CSB2Register and Ping-pong Register

Copy NV_NVDLA_CSB_LOGIC.scala to your library. it will translate csb2dp interface to register control logic. 

A ping-pong register is consisted of 2 dual_registers(generate configurations) and one single_register(decide which pointer and consumer to use). Single register is to decide which of the dual_register to use in this cycle, dual register is to produce a configurations to be passed to discrete processors.

A common NV_NVDLA_BASIC_REG_single is given [here](https://github.com/soDLA-publishment/soDLA/blob/soDLA_beta/src/main/scala/slibs/NV_NVDLA_BASIC_REG_single.scala), also copy it to your library.

Now you have csb2reg_control logic, single_register within your library.







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







