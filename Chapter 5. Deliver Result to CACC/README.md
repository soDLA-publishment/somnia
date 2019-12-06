# Deliver Result to CACC

Under your-project-dir, create a folder, name it cacc. Copy all the files from [soDLA cacc](https://github.com/soDLA-publishment/soDLA/tree/soDLA_beta/src/main/scala/nvdla/cacc). A CACC requires two stages, accumulate the results and deliver the results to the next stages.

## Accumulate

Accumulate need assembly and calculation. Assembly buffer consists of memories and controls, calculation part will calculate the accumulative mac results from cmac stage, the basic unit is CACC_CALC_int8, it will keep adding up, after in_sel is one, the result with rounding will show up in the result. 


## Delivery buffer

A delivery buffer consists of delivery buffer and delivery control, as a buffer to the next stage.


## Add relu to CACC as a local unit

In CACC_CALC_int8, before the final output, add the following,

```
val u_x_relu = Module(new NV_NVDLA_HLS_relu(32))
u_x_relu.io.data_in := i_final_result
val relu_out = u_x_relu.io.data_out
val relu_dout = Mux(io.cfg_relu_bypass, i_final_result, relu_out)
```

To find the corresponding programming space of cfg_relu_bypass, check [SDP manual](https://github.com/nvdla/hw/blob/master/spec/manual/NVDLA_SDP.rdl), notice 'cfg_bn_relu_bypass'.  Congifure the register file, add bn_relu_bypass to cacc_reg_dual. So that relu configuration is connected to the programming layer.

















