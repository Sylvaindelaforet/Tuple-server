# Client-Cache

These classes add to the implementation of the server and client a cache on each client.

Thus when a client read a Tuple from a server, it will save a copy on its cache, so that it can read it again if needed.
When the original is removed from the server, it communicates to cache that saved a copy to remove the local copy.


