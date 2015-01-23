package io.herd.gossip;

import io.herd.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

public class Gossiper {

    private class GossiperTask implements Runnable {

        @Override
        public void run() {
            logger.debug("Starting gossip round");
            try {
                logger.debug("My heartbeat is now {}", endpointStateMap.get(localhost).getHeartBeatState().version);

                final List<GossipDigest> digestList = createGossipDigest();

                /*
                 * It highly unlikely to have an empty list of digests. At least we will send our own state.
                 */
                if (!digestList.isEmpty()) {
                    GossipDigestSyn synMessage = new GossipDigestSyn(digestList);

                    // gossip with some live node first
                    InetSocketAddress targetNode = sendGossip(synMessage, liveNodes);

                    // TODO gossip to some unreachable node just in case he is back up

                    /*
                     * Gossip to a seed node if we haven't done so already or if we haven't seen all seeds. This
                     * prevents partitions where each group of nodes is gossiping to a subset of seeds. To avoid
                     * checking all seeds we reason that: if there's only one node on the cluster (which would be us)
                     * then we're talking to ourselves (which we don't by the way); if there are only seed nodes we will
                     * continue to talk to each other until we all have information about all of us. New non seed nodes
                     * will introduce themselves to a seed node initially so no problem there. If there's already one or
                     * more non-seed nodes eventually someone will gossip to it the gossip to one of the seeds. See
                     * CASSANDRA-150 for a better explanation.
                     */
                    if (isSeedNode(targetNode) || liveNodes.size() < seedNodes.size()) {
                        sendGossip(synMessage, seedNodes);
                    }

                }
            } catch (Exception e) {
                logger.error("Gossip round failed", e);
            }

        }

    }

    private static final Logger logger = LoggerFactory.getLogger(Gossiper.class);

    // used to randomize the selections of nodes to gossip with
    final Random random = new Random();
    
    private final Comparator<InetSocketAddress> inetSocketComparator = new Comparator<InetSocketAddress>() {

        @Override
        public int compare(InetSocketAddress o1, InetSocketAddress o2) {
            return o1.getAddress().getHostAddress().compareTo(o2.getAddress().getHostAddress());
        }
    };

    final ConcurrentMap<InetSocketAddress, EndpointState> endpointStateMap = new ConcurrentHashMap<>();
    
    // the list of nodes that are known to be live (including seeds)
    final ConcurrentSkipListSet<InetSocketAddress> liveNodes = new ConcurrentSkipListSet<>(inetSocketComparator);

    // the list of seeds that constitute the core of the cluster
    final ConcurrentSkipListSet<InetSocketAddress> seedNodes = new ConcurrentSkipListSet<>(inetSocketComparator);

    // the executor used when gossiping with other nodes. The gossip tasks shouldn't be daemon threads btw
    private final ScheduledExecutorService executor = Executors
            .newSingleThreadScheduledExecutor(new DefaultThreadFactory("GossipTask-"));

    private volatile boolean isRunning = false;

    private InetSocketAddress localhost;

    Gossiper() {

    }

    public void addSeedNode(InetSocketAddress address) {
        logger.info("Registering {} as a new seed node", address);
        this.seedNodes.add(address);
    }

    private List<GossipDigest> createGossipDigest() {

        List<InetSocketAddress> endpoints = new ArrayList<>(endpointStateMap.keySet());
        Collections.shuffle(endpoints);

        return endpoints.stream().map(this::createGossipDigest).collect(Collectors.toList());
    }

    /**
     * Creates a {@link GossipDigest} message with the information this node currently knows about the given endpoint.
     */
    private GossipDigest createGossipDigest(InetSocketAddress endpoint) {
        EndpointState epState = endpointStateMap.get(endpoint);
        if (epState != null) {
            int generation = epState.getGeneration();
            long maxVersion = epState.getMaxVersion();
            return new GossipDigest(endpoint, generation, maxVersion);
        }
        return new GossipDigest(endpoint, 0, 0);
    }

    /**
     * Compares the node versions provided with what we know about the clusters.
     * 
     * @param digests A list of {@link GossipDigest} from different nodes
     * @param deltaDigests A list of {@link GossipDigest} pertaining to nodes we figured out are out-of-date
     * @param nodeStateMap A map of {@link EndpointState} holding the most recent information about nodes
     */
    void determineEndpointStateDeltas(List<GossipDigest> digests, List<GossipDigest> deltaDigests,
            Map<InetSocketAddress, EndpointState> nodeStateMap) {

        for (GossipDigest digest : digests) {

            int remoteGeneration = digest.generation;
            long remoteMaxVersion = digest.maxVersion;

            EndpointState epState = this.endpointStateMap.get(digest.endpoint);

            if (epState == null) {
                // asking for version 0 means we are asking for everything
                deltaDigests.add(new GossipDigest(digest.endpoint, digest.generation, 0));
            } else {
                int localGeneration = epState.getGeneration();
                long localMaxVersion = epState.getMaxVersion();

                // we see the same version of this node
                if (remoteGeneration == localGeneration && remoteMaxVersion == localMaxVersion) {
                    continue;
                }

                if (remoteGeneration > localGeneration) {
                    // this node was restarted and we didn't know about that so we request everything
                    deltaDigests.add(new GossipDigest(digest.endpoint, digest.generation, 0));
                } else if (remoteGeneration < localGeneration) {
                    /*
                     * our generation is greater than the one provided which means that we know that the node was
                     * restarted but the caller still doesn't know that. We send everything in this case.
                     */
                    sendDelta(digest, nodeStateMap, 0);
                } else {
                    if (remoteMaxVersion > localMaxVersion) {
                        // we have an old version of the node so ask for all the changes after the version we have
                        deltaDigests.add(new GossipDigest(digest.endpoint, digest.generation, localMaxVersion));
                    } else if (remoteMaxVersion < localMaxVersion) {
                        // we have a more recent version so send back all the changes
                        sendDelta(digest, nodeStateMap, remoteMaxVersion);
                    }
                }
            }
        }
    }
    
    private void sendDelta(GossipDigest digest, Map<InetSocketAddress, EndpointState> nodeStateMap, long version) {
        EndpointState originalState = this.endpointStateMap.get(digest.endpoint);
        EndpointState returnedState = null;
        if (originalState != null) {
            for (Entry<ApplicationState, VersionedValue> entry : originalState.getApplicationState().entrySet()) {
                VersionedValue value = entry.getValue();
                if (value.getVersion() > version) {
                    if (returnedState == null) {
                        returnedState = new EndpointState(originalState.getHeartBeatState());
                    }
                    final ApplicationState key = entry.getKey();
                    returnedState.addApplicationState(key, value);
                }
            }
        }
        nodeStateMap.put(digest.endpoint, returnedState);
    }

    public EndpointState getEndpointState(InetSocketAddress address, long version) {

        EndpointState state = endpointStateMap.get(address);
        if (state == null) {
            return null;
        }
        long localVersion = state.getHeartBeatState().version;
        if (localVersion > version) {
            EndpointState reqEndpoint = new EndpointState(state.getHeartBeatState());
            return reqEndpoint;
        }
        return null;
    }

    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Checks if the provided {@link InetSocketAddress} belongs to the list of known seed nodes.
     * 
     * @param address A nullable {@link InetSocketAddress}
     * @return <code>true</code> if the address corresponds to a seed node or <code>false</code> otherwise or is
     *         <code>null</code>.
     */
    public boolean isSeedNode(InetSocketAddress address) {
        return address != null && seedNodes.contains(address);
    }

    private InetSocketAddress sendGossip(GossipDigestSyn message, Set<InetSocketAddress> nodeList) {

        if (nodeList.isEmpty()) {
            return null;
        }

        List<InetSocketAddress> nodes = ImmutableList.copyOf(nodeList);
        int size = nodes.size();
        int index = (size == 1) ? 0 : random.nextInt(size);
        InetSocketAddress target = nodes.get(index);
        logger.debug("Gossiping with {}", target);
        new GossipClient(this).gossip(message, target);
        return target;
    }

    public void start(InetSocketAddress localhost) {
        
        this.localhost = localhost;

        if (isRunning) {
            logger.info("Gossiper is already running");
            return;
        }
        try {
            start((int) (System.currentTimeMillis() / 1000));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void start(int generationNumber) throws Exception {

        HeartBeatState hbState = new HeartBeatState(generationNumber);
        EndpointState epState = new EndpointState(hbState);

        endpointStateMap.putIfAbsent(this.localhost, epState);

        this.executor.scheduleWithFixedDelay(new GossiperTask(), 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        try {
            executor.shutdown();
            executor.awaitTermination(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("Failed to wait for gossiper to stop.", e);
        }
    }

    public void updateStates(Map<InetSocketAddress, EndpointState> newEndpointStates) {
        endpointStateMap.putAll(newEndpointStates);
    }
}
