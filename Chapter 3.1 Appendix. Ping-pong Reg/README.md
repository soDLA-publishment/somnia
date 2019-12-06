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

To make your accelerators be compatible with [tensorRT](https://github.com/NVIDIA/TensorRT), check [hardware specification manuals] (https://github.com/nvdla/hw/tree/master/spec/manual), it provides the information of configuration specifications, so that your accelerator can be supported by [NVDLA Kernel Mode Driver](http://nvdla.org/sw/runtime_environment.html#kernel-mode-driver).

To setup dual_register, for example, you create a cmac block under your-project_dir
, in cmac_config file, add a bundle named dual_reg_outputs

```
class cmac_reg_dual_flop_outputs extends Bundle{
    val conv_mode = Output(Bool())
    val proc_precision = Output(UInt(2.W))
}
```
Under your-project-dir, base on the [hardware specification manuals] (https://github.com/nvdla/hw/tree/master/spec/manual), create a dual_reg, describe the relationships between address space and output configurations. You can use the example within this repo.

Rdl files under hardware specification manuals can be automaticly generated to a ping-pong register, this could be done using a script. Hope you can finish that script.

Common parameters from ping-pong registers are initial with reg2dp. Some parameters are initial with dp2reg, for example, dp2reg_done, means this processor finished its job.










