# Tuple-server

This repo is a reworking of a project I did in class in a group of 3, and that I thought was fun.
Hence I decided to rewrite it from scratch, only reusing some classes given for the project, these can be found in the utils folder.

Now, all the work has been written by me, the <code>CoreLinda</code> class being entirely rewritten using different tools for synchronization.

The server package however reuses the work I did at the time of the project. 


## Details

### Package core
Implements the core of the syncronization and storage system.

### Package server

It contains a server and client implementation that extends <code>CoreLinda</code> as a server thus being accessible through the client.


## Improvements

The <code>CoreLinda</code> class should implements a better more efficient storage system for large quantity of data.

In regards the use of this tuple-server, multiple improvements are possible to study :
 - when using a wide variety of tuple, a better implementation would be to break the Array used for storage into multiple ones that are accessed by exploring a tree (mostly read).
 - if there is no knowledge we can just break the array in multiple ones of roughly the same size.

The first improvement is unefficient if using always the same "kind" of tuple (for example [integer integer]). The second one is always efficient for it allows to write
and read at the same time, but this improvement is less significant than the first one.

The two improvements described above can be combined, to be even more powerfull.
