# Start from CMAC

CMAC is a computational-intensive module, includes 4 major parts, 1. retiming to buffer, 2. active to deliver the correct weight and dat, 3. atomic k's mac, and 4. corresponding ping-pong register. 

Under your-project-dir, create a folder, name it cmac. Copy all the files from [soDLA cmac](https://github.com/soDLA-publishment/soDLA/tree/soDLA_beta/src/main/scala/nvdla/cmac). The following is the brief introduction of submodules.

## Retiming 

Includes input retiming, output retiming, retiming during mac calculations. Because cmac is a computational-intensive module, the delay can be large, if there is no retiming and set cmac as one stage within one pipe, the timing result can be bad. Retiming is to give the computational-intensive module more duty cycles, so that we can reduce the time/cycle of the whole design.

Because we are not sure about how much delay it will take during retiming. We can set input retiming latency, output retiming latency, active retiming latency, mac retiming latency as parameters in the module configurations.


## MAC

One mac can perform one vector multiply and accumulate per time. 


```
    val mout = VecInit(Seq.fill(conf.CMAC_ATOMC)(0.asSInt((2*conf.CMAC_BPE).W)))

    for(i <- 0 to conf.CMAC_ATOMC-1){
        when(io.wt_actv(i).valid&io.wt_actv(i).bits.nz&io.dat_actv(i).valid&io.dat_actv(i).bits.nz){                       
             mout(i) := io.wt_actv(i).bits.data.asSInt*io.dat_actv(i).bits.data.asSInt
        }
    }  

    val sum_out = mout.reduce(_+&_).asUInt
    
    //add retiming
    val pp_pvld_d0 = io.dat_actv(0).valid&io.wt_actv(0).valid

    io.mac_out.bits := ShiftRegister(sum_out, conf.CMAC_OUT_RETIMING, pp_pvld_d0)
    io.mac_out.valid := ShiftRegister(pp_pvld_d0, conf.CMAC_OUT_RETIMING, pp_pvld_d0)

    }
```

The final width is 2*m+log2(n), m is the bandwidth per element(BPE), n is the input vector length.


## Active

Because weight is stored in a kernel, it can be re-used for multiple times, weight has one more shadow stage to cache. 

```
wt : in --> pre --> sd --> actv 
dat: in --> pre ---------> actv
```

When a stripe start, push the weight from the shadow zone to actv. When the stripe is not start or end, keep using the weight in the output(actv). When the stripe is end, discard the weight that is from actve, and introducing new weight to shadow.

## Adjust soDLA modules to simba

In the cmacConfigurations, set
CMAC_ATOMC = PE_MAC_ATOMIC_C_SIZE, CMAC_ATOMK = PE_MAC_ATOMIC_K_SIZE.

Modify the corresponding implicit parameters from soDLA, CMAC_ATOMK_HALF should be modified as CMAC_ATOMK. 

Modify the nvdla_core_clock to simba_core_clock.














