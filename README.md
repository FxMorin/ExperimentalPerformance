# ExperimentalPerformance  

This is a mod that deals with experimental changes to greatly boost performance ;)  

## Current Changes:  
### Memory Allocation  
Some classes can be very memory dense, they have a big memory footprint.  
This is an issue because your computer can only fit 64 bytes into a processor's cache line, so anything over 64 needs to be split into multiple lines. This can drastically slow down operations based on what is being called.
So obviously the best solution is to make sure nothing is over 64 bytes, this is a lot easier said than done, since there's multiple downsides to this, although they don't matter in the circumstances that ive optimized.
#### Chunk Memory Allocation
Chunks are very memory dense and take up 80 bytes. Using some hacky fabric-ASM we are able to nuke variables from the Chunk class and redirect all the calls. 
This allows us to bring the chunk back down to 64, which on some computers can make most chunk operations 0.25x faster!
#### Block Memory Allocation
Blocks are also very memory dense, taking up 72 bytes.
We do the same thing as Chunks, we move stuff info a BlockInfo class and reduce the weight of the class.
This not only makes it so most blocks can now be passed along faster, but also prevents some blocks from going over 128 bytes, which would need 3 cache lines.
