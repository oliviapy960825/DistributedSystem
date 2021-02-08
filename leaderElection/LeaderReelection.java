import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class LeaderReelection implements Watcher{

    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSTION_TIMEOUT = 3000;
    private ZooKeeper zooKeeper;
    private String currentZNodeName;
    private static final String ELECTION_NAMESPACE = "/election";

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        LeaderReelection leaderElection = new LeaderReelection();
        leaderElection.connectToZookeeper();
        leaderElection.volunteerForLeadership();
        leaderElection.reelectLeader();
        leaderElection.run();
        leaderElection.close();
        System.out.println("Disconnected from Zookeeper, exiting application");
    }
    public void volunteerForLeadership() throws KeeperException, InterruptedException {
        String zNodePrefix = ELECTION_NAMESPACE + "/c_";
        String zNodeFullPath = zooKeeper.create(zNodePrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        System.out.println("zNode name is : " + zNodeFullPath);
        this.currentZNodeName = zNodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
    }

    public void reelectLeader() throws KeeperException, InterruptedException {
        Stat predecessorStat = null;
        String predecessorZnode = "";

        while(predecessorStat == null) {
            List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
            Collections.sort(children);
            String child = children.get(0);

            if (child.equals(currentZNodeName)) {
                System.out.println("I am the leader");
                return;
            } else {
                System.out.println("I am not the leader, " + child + " is the leader.");
                int predecessorIndex = Collections.binarySearch(children, currentZNodeName) - 1;
                predecessorZnode = children.get(predecessorIndex);
                predecessorStat = zooKeeper.exists(ELECTION_NAMESPACE + "/" + predecessorZnode, this);
            }
        }

        System.out.println("Watching Znode : " + predecessorZnode);

    }
    public void connectToZookeeper() throws IOException{
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSTION_TIMEOUT, this);
    }

    public void run() throws InterruptedException{
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    private void close() throws InterruptedException {
        this.zooKeeper.close();
    }
    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()){
            case None:
                if(watchedEvent.getState() == Event.KeeperState.SyncConnected){
                    System.out.println("Successfully connected");
                }

                else{
                    synchronized (zooKeeper){
                        System.out.println("Disconnected from Zookeeper");
                        zooKeeper.notifyAll();
                    }
                }
                break;
            case NodeDeleted:
                try{
                    reelectLeader();
                }
                catch (KeeperException e){

                }
                catch (InterruptedException e) {

                }
                break;
        }
    }
}
