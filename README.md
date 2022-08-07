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
We do the same thing as Chunks, we move stuff into a BlockInfo class and reduce the weight of the class.
This not only makes it so most blocks can now be passed along faster, but also prevents some blocks from going over 128 bytes, which would need 3 cache lines.

### Why is Mojang not doing this?
Well they kinda are?
When making my optimizations I noticed that mojang has actually made the same performance improvements that I'm doing. Such as LevelInfo, although that was years ago?
Mojang does not seem to care about performance much these days, they make really weird performance changes but never seem to really care too much. Some classes have grown a lot in the last years, and they haven't really notices how dense they have become.
That's fine tho, cause im here to help.

### The game of cat and mouse
Performance is hard to get more of, it's a very complex puzzle which requires you to understand every single part of it in a lot of detail.
Put simply, computers are not simple. They try really hard to make it easier for the programmer to not think about it, although programmers should be thinking about it at all times.
What am I talking about?
Well the performance I get with this mod comes at a cost, extra calls and memory allocation. I'm simply optimizing the structures within the game to work as smoothly and quickly with the computer, although that requires the computer to remember more things. 
Unfortunately, you won't notice that cost since in most of these situations the performance win greatly overcomes the loss.
