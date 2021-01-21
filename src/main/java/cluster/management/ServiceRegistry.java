package cluster.management;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceRegistry implements Watcher {
    private static final String REGISTRY_ZNODE = "/service_registry";
    private ZooKeeper zooKeeper;
    private String currentZNode;
    private List<String> allServiceAddresses;

    public ServiceRegistry(ZooKeeper zooKeeper){
        this.zooKeeper = zooKeeper;
        createServiceRegistryZnode();
    }
    public void registerToCluster(String metadata) throws KeeperException, InterruptedException {
        this.currentZNode = zooKeeper.create(
                REGISTRY_ZNODE + "/n_", metadata.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL
        );

        System.out.println("Registered to the cluster");
    }
    private void createServiceRegistryZnode() {
        try {
            if (zooKeeper.exists(REGISTRY_ZNODE, false) == null) {
                zooKeeper.create(REGISTRY_ZNODE, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        }
        catch(KeeperException e){
            e.printStackTrace();
        } catch(InterruptedException e){
            e.printStackTrace();
        }
    }
    public void registerForUpdates(){
        try{
            updateAddress();
        }
        catch(KeeperException e){

        }
        catch(InterruptedException e){

        }
    }

    public synchronized List<String> getAllServiceAddresses() throws KeeperException, InterruptedException {
        if(allServiceAddresses == null){
            updateAddress();
        }
        return allServiceAddresses;
    }

    public void unregisterForCluster() throws KeeperException, InterruptedException {
        if(currentZNode != null && zooKeeper.exists(currentZNode, false) != null){
            zooKeeper.delete(currentZNode, -1);
        }
    }
    private synchronized void updateAddress() throws KeeperException, InterruptedException {
        List<String> workerNodes = zooKeeper.getChildren(REGISTRY_ZNODE, this);
        List<String> addresses = new ArrayList<>(workerNodes.size());

        for(String zNode : workerNodes){
            String workerFullPath = REGISTRY_ZNODE + "/" + zNode;
            Stat stat = zooKeeper.exists(workerFullPath, false);
            if(stat == null){
                continue;
            }

            byte[] addressBytes = zooKeeper.getData(workerFullPath, false, stat);
            String address = new String(addressBytes);
            addresses.add(address);
        }

        this.allServiceAddresses = Collections.unmodifiableList(addresses);

        System.out.println("The cluster address is : " + this.allServiceAddresses);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try{
            updateAddress();
        }
        catch(KeeperException e){
            e.printStackTrace();
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}
