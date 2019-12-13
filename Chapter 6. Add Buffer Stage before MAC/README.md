# Add Buffer Stage before MAC

A set of signal from router need buffering. To create a buffer, you might need a FIFO, adjust the depth. To keep the balance of commercial company's project and open-source project, I'm not going to discuss more and post design about that.

But I really encourage you to have a try. First, you need to create your own buffer, the FIFO here(https://github.com/freechipsproject/chisel3/wiki/Polymorphism-and-Parameterization) gives you a head start. 

