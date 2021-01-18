# DistributedSystem

This is the repo for the Udemy class "Distributed System & Cloud Computing with JAVA"


## Section 1 Cluster Coordination Service and Distributed Algorithms

### Node

A node is a process running on a dedicated machine. Those processes could communicate with each other through network

### Cluster

A cluster is a collection of computers / nodes connected to each other. The nodes in a cluster are working on the same task and typically running the same code


### How to break the work among nodes?

  1. Manual distribution
  2. Manually elect a leader -> The leader will be responsible for distribution and collection of the work -> Potential threats : leader failure
  3. Automatic leader election -> If the elected leader failed, the remaining nodes will elect a new leader, and if the failed leader recovers from the failure 
  later, it will rejoin the cluster as a member
    
### Challenges of Automatic Leader Election
  1. Automatic and System Leader election is no trivial task
  2. Arriving to an agreement on a leader in a large cluster of nodes is even harder
  3. By default each node knows only about itself -> Service registry and discovery is required
  4. Failure Detection Mechanism is necessary to trigger automatic leader reelection in a cluster
  
### Apache Zookeeper

Apache Zookeeper is a high performance coordination service designed specifically for distributed systems, and is a popular technology used by many companies and projects including Kafka, Hadoop, HBase and etc.. Apache Zookeeper provides an abstraction layer for higher level distributed algorithms

Apache Zookeeper is a distributed system itself that provides us high availability and reliability. It typically runs in a cluster of an odd number of nodes that is higher than 3. It uses redundancy to allow failures and stay functional. Nodes in a Zookeeper cluster communicate directly with their cluster coordinator instead of communicating with each other.

Zookeeper's abstraction and data model is like a tree and very similar to file system. Each element in the tree is called a Znode

### Znode

Znodes' properties:

Znode is like a hybrid between a file and a directory -> Znodes can store data inside like a file, and Znode can also have children Znodes like a directory

Two types of Znodes:
  1. Persistent -> presists between sessions -> if disconnect and then reconnect, the data and children Znodes will still be there intact
  2. Ephemeral -> is deleted when the session ends -> a great tool to check if the parent node went down
  
Zookeeper Leader Election algorithm in 3 steps:

  1. Each node connected to Zookeeper volunteers to become the leader. Each node submits its candidancy by adding a Znode that represents itself under the election 
  parent -> Zookeeper maintains a global order so it can rename each Znode according to the order of their addition
  2. After each Znode finishes creating a Znode, it would query the current children of the election parent. Because of that order that Zookeeper provides us, each 
  node when querying children of the election parent is guaranteed to see all the Znodes created prior to its own Znode creation
  3. If the Znode the current node created is the smallest number it knows that it is now the leader. Others would know that they are not the leader and would then 
  wait for instructions from the elected leader
  

### Zookeeper config

useful commands:
   ./zkServer.sh start -> start zookeeper server
   ./zkCli.sh -> client environment
   ls / -> check the list of nodes
   ls /parent -> check the nodes under parent directory
   creat /parent "blahblah" -> create parent node with content "blahblah"
   get /parent -> get information about the parent node
   help -> show help menu
  
### Zookeeper Threading Model

Application's start code in the main method is executed on the main thread
When Zookeeper object is created, two additional threads are created -> Event thread & IO thread

  IO Thread -> we don't interact with IO thread directly but IO Thread handles the following
    Handles all the network communications with Zookeeper servers
    Handles Zookeeper requests and responses
    Responds to pings
    Session Management
    Session Timeouts
    
  Event Thread 
    Manages Zookeeper events
      Connection (KeeperState.SyncConnected)
      Disconnection (KeeperState.Disconnected)
    Custom Znode watchers and triggers that we subscribe to
    Events are executed on Event Thread in order
    
