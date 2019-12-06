# Start from CMAC

CMAC is a computational-intensive module, includes 4 major parts, 1. retiming to buffer, 2. active to deliver the correct weight and dat, 3. atomic k's mac, and 4. corresponding ping-pong register. 

Under your-project-dir, create a folder, name it cmac.

## Retiming 

Includes input retiming, output retiming, retiming during mac calculations. Because cmac is a computational-intensive module, the delay can be large, if there is no retiming and set cmac as one stage within one pipe, the timing result can be bad. Retiming is to give the computational-intensive module more duty cycles, so that we can reduce the time/cycle of the whole design.

Because we are not sure about how much delay it will take during retiming. We can set input retiming latency, output retiming latency, active retiming latency, mac retiming latency as parameters in the module configurations.


## MAC

One mac can perform one vector multiply and accumulate per time, 






