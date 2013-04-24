Assignment 7- Distributed HTTP Server
=====================================
http://www.cs.rutgers.edu/~pxk/352/hw/a-7.html


Group Members
-------------------
* William Lynch
* Bilal Quadri
* Bryant Satterfield


Usage
-------------------

###Server

    java Server <port to run on>
    ex: java Server 4000
	
###Client
Requires curl: http://curl.haxx.se/

The `-i` flag can be used to display the response headers.

`GET` Get content from server

    curl -i <host>:<port><file>
    ex: curl -i localhost:1234/local.html

`PUT` Put content on server

    curl -iT <localfile> <host>:<port><directory>
    ex: curl -iT test.html localhost:1234

`DELETE` Delete content from server

    curl -iX DELETE <host>:<port><file>
    ex: curl -iX DELETE localhost:1234/test.html

`LIST` List content the given peer hosts locally

    curl -iX LIST <host>:<port>
    ex: curl -iX LIST localhost:1234

`PEERS` List the peers the given peer has knowledge of

    curl -iX PEERS <host>:<port>

`ADD` Add peer2 to the group of peer1

    curl -iX "ADD <host2>:<port2>" <host1>:<port1>
    ex: curl -iX "ADD localhost:3000" localhost:4000

`REMOVE` Remove peer2 from the group of peer1. Removing peer2 redistributes all
files that peer2 kept track of to its successor and terminates peer2. To read
peer2, restart the server.

    curl -X "REMOVE <host2>:<port2>" <host1>:<port1>
    ex: curl -X "REMOVE localhost:3000" localhost:4000


Compilation
-------------------

    make


Architecure
-------------------
###Content
Content is stored on each peer with a HashMap that uses a \<key,value\> pair of
\<filename hash, FileNode\>. The filename hash is generated with a 32 bit MD5
hash. FileNode is a supplementary data structure that represents a file.
FileNodes contain the name of the file and the raw bytes of the data.

###Peers
Peers are tracked within each peer with an PeerNode ArrayList that contains
peers in sorted order with respect to the MD5 hash of their "host:port". The
peers are stored this way in order to keep track of a peer's predecessor and
successor. A PeerNode is a supplementary data structure that represents a peer.
PeerNodes contain the hash of a peer's "host:port", the address of the peer, and
the port the peer is running on. When a peer is added or removed from a group,
each peer is notified to update their list of peers.

###File distribution
When a file is added to a group of peers, the peer that originally receives the
request looks to see what peer the file should belong to. This is done by
partitioning the server (the collection of all peers in the group) by the number
of peers by their MD5 hash. Each section of the partition represents a
particular peer in the group.  A peer determines which peer a file belongs to by
looking at the MD5 hash of the file name, and determining which peer partition
the file falls under.  For example, suppose we have a simple hash that ranges
from [0-9], and 3 peers with hashes a=3, b=5, and c=7. The peer partition then
looks like:

    |--------|-----|-----|-----|
    0    a      b     c     a  9

When a peer is added from a group, it is inserted in it's proper location in the
list, and files that should now belong to it are then moved from it's previous
peer.

Similarly, if a peer is removed, all files are moved to its successor.  When a
peer is removed, it is also terminated. If the peer is the only peer in the
server, all files will be removed and the server will terminate.


Design Choices
-------------------

**REMOVE**: When a peer is removed, it is also terminated. If you want to re add the
peer, you must restart the server.

**Handling recursion**: Recursion is handled by passing in a special argument to the
server that notifies it not to recurse to other peers.

**ADD**: ADD must be called independently of the server being created. This is done
to send responses back if the users sends a request that is not valid (e.g. if
the server does not exist).

Testing
-------------------


Limitations
-------------------
