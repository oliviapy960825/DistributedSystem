import cluster.management.OnElectionCallback;
import cluster.management.ServiceRegistry;
import cluster.management.LeaderReelection;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Application implements Watcher {
    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSTION_TIMEOUT = 3000;
    private static final int DEFAULT_PORT = 8080;
    private ZooKeeper zooKeeper;


    public ZooKeeper connectToZookeeper() throws IOException{
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSTION_TIMEOUT, this);
        return zooKeeper;
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
}


    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        int currentServerPort = args.length == 1 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        Application application = new Application();
        ZooKeeper zooKeeper = application.connectToZookeeper();
        ServiceRegistry serviceRegistry = new ServiceRegistry(zooKeeper);
        OnElectionAction onElectionAction = new OnElectionAction(serviceRegistry, currentServerPort);
        LeaderReelection leaderReelection = new LeaderReelection(zooKeeper, onElectionAction);
        //application.connectToZookeeper();
        leaderReelection.volunteerForLeadership();
        leaderReelection.reelectLeader();
        application.run();
        application.close();
        System.out.println("Disconnected from Zookeeper, exiting application");
    }
}
