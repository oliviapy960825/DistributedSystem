import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
public class LeaderElection implements Watcher{

    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSTION_TIMEOUT = 3000;
    private ZooKeeper zooKeeper;

    public static void main(String[] args) throws IOException, InterruptedException{
        LeaderElection leaderElection = new LeaderElection();
        leaderElection.connectToZookeeper();
        leaderElection.run();
        leaderElection.close();
        System.out.println("Disconnected from Zookeeper, exiting application");
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
        }
    }
}
