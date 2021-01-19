import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class WatcherDemo implements Watcher{

    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSTION_TIMEOUT = 3000;
    private static final String TARGET_ZNODE = "/target_znode";
    private ZooKeeper zooKeeper;
    private String currentZNodeName;
    private static final String ELECTION_NAMESPACE = "/election";

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        WatcherDemo watcherDemo = new WatcherDemo();
        watcherDemo.connectToZookeeper();
        watcherDemo.watchTargetZnode();
        watcherDemo.run();
        watcherDemo.close();
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

    public void watchTargetZnode() throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(TARGET_ZNODE, this);
        if(stat == null){
            return ;
        }
        byte[] data = zooKeeper.getData(TARGET_ZNODE, this, stat);
        List<String> children = zooKeeper.getChildren(TARGET_ZNODE, this);

        System.out.println("Data : " + new String(data) + " children: " + children);
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
                System.out.println(TARGET_ZNODE + " was deleted");
                break;
            case NodeCreated:
                System.out.println(TARGET_ZNODE + " was created");
                break;
            case NodeDataChanged:
                System.out.println(TARGET_ZNODE + " data changed");
                break;
            case NodeChildrenChanged:
                System.out.println(TARGET_ZNODE + " children changed");
                break;
        }

        try{
            watchTargetZnode();
        }
        catch(KeeperException e){}
            catch(InterruptedException e){
        }
    }
}
