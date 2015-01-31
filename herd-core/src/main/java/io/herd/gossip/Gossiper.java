package io.herd.gossip;

import io.herd.base.ServerRuntimeException;
import io.herd.base.Service;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

public class Gossiper implements Service {
    
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
    private final Random random = new Random();
    
    private final Comparator<InetSocketAddress> inetSocketComparator = new Comparator<InetSocketAddress>() {

        @Override
        public int compare(InetSocketAddress o1, InetSocketAddress o2) {
            return o1.getAddress().getHostAddress().compareTo(o2.getAddress().getHostAddress());
        }
    };

    // the map holding the entire information this node has about the remaining nodes on the cluster
    final ConcurrentMap<InetSocketAddress, EndpointState> endpointStateMap = new ConcurrentHashMap<>();
    
    // the list of nodes that are known to be live (including seeds)
    final ConcurrentSkipListSet<InetSocketAddress> liveNodes = new ConcurrentSkipListSet<>(inetSocketComparator);

    // the list of seeds that constitute the core of the cluster
    final ConcurrentSkipListSet<InetSocketAddress> seedNodes = new ConcurrentSkipListSet<>(inetSocketComparator);

    // the executor used when gossiping with other nodes. The gossip tasks shouldn't be daemon threads btw
    private final ScheduledExecutorService executor = Executors
            .newSingleThreadScheduledExecutor(new DefaultThreadFactory("GossipTask-"));
    
    // the executor responsible for dispatching notifications to listeners
    private final ExecutorService dispatcherService = Executors.newSingleThreadExecutor(new DefaultThreadFactory(
            "GossipNotificationDispatcher-"));

    // collection of gossip listeners
    private final ConcurrentSkipListSet<GossipChangeListener> listeners = new ConcurrentSkipListSet<>();

    private volatile boolean isRunning = false;
    private final InetSocketAddress localhost;

    Gossiper(InetSocketAddress localhost) {
        this.localhost = localhost;
    }

    public void addSeedNode(InetSocketAddress address) {
        logger.info("Registering {} as a new seed node", address);
        this.seedNodes.add(address);
    }
    
    /**
     * Adds or updates an {@link ApplicationState} belonging to this node. Calling this method has no effect if the
     * service isn't running.
     * 
     * @param state
     * @param value
     * @see #start()
     */
    public void addState(ApplicationState state, String value) {
        if (isRunning()) {
            EndpointState localState = endpointStateMap.get(this.localhost);
            localState.addApplicationState(state, new VersionedValue(value));
        }
    }

    private List<GossipDigest> createGossipDigest() {

        List<InetSocketAddress> endpoints = new ArrayList<>(endpointStateMap.keySet());
        // TODO why I'm I shuffling this anyway?
        Collections.shuffle(endpoints);

        return endpoints.stream().map(this::createGossipDigest).collect(Collectors.toList());
    }

    /**
     * Creates a {@link GossipDigest} message with the information this node currently knows about the given endpoint.
     * If the endpoint is unknown returns a {@link GossipDigest} with generation and version set to 0 which will trigger
     * a full sync when gossiping.
     */
    private GossipDigest createGossipDigest(InetSocketAddress endpoint) {
        EndpointState epState = endpointStateMap.get(endpoint);
        if (epState != null) {
            int generation = epState.getGeneration();
            long maxVersion = epState.getMaxVersion();
            return new GossipDigest(endpoint, generation, maxVersion);
        }
        // we do not know this endpoint so we return a dummy digest
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
    
    public void sendDelta(GossipDigest digest, Map<InetSocketAddress, EndpointState> nodeStateMap, long version) {
        EndpointState state = endpointStateMap.get(digest.endpoint);
        if (state == null) {
            return;
        }
        EndpointState deltaState = state.copyState(version);
        if(deltaState != null) {
            nodeStateMap.put(digest.endpoint, deltaState);
        }
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
    
    private void sendNotification(final Object notification) {
        this.dispatcherService.execute(() -> {
            for (GossipChangeListener listener : listeners) {
                listener.onChange(notification);
            }
        }); 
    }

    public void start() {
        try {
            if (isRunning()) {
                logger.info("Gossiper is already running");
                return;
            }
            /*
             * our generation number is simply the time the service started since it's more than enough to guarantee
             * that it's different than previous generations
             */
            int generationNumber = (int) (System.currentTimeMillis() / 1000);
            HeartBeatState hbState = new HeartBeatState(generationNumber);
            EndpointState epState = new EndpointState(hbState);

            endpointStateMap.putIfAbsent(this.localhost, epState);

            this.executor.scheduleWithFixedDelay(new GossiperTask(), 1000, 1000, TimeUnit.MILLISECONDS);
            isRunning = true;
        } catch (Exception e) {
            logger.error("Failed to start gossiper", e);
            throw new ServerRuntimeException(e);
        }
    }
    
    public void stop() {
        if (isRunning()) {
            try {
                executor.shutdown();
                dispatcherService.shutdown();
                executor.awaitTermination(3000, TimeUnit.MILLISECONDS);
                dispatcherService.awaitTermination(3000, TimeUnit.MILLISECONDS);
                isRunning = false;
            } catch (InterruptedException e) {
                logger.error("Failed to wait for gossiper to stop.", e);
            }
        }
    }

    /**
     * Updates the {@link Gossiper}'s internal knowledge of the nodes with the information provided. Has no effect if
     * the map is <code>null</code> or empty.
     * 
     * @param newEndpointStates A map with a set of changes that should be reconciled with the {@link Gossiper}'s
     *            internal state.
     */
    public void updateStates(Map<InetSocketAddress, EndpointState> newEndpointStates) {
        if (newEndpointStates == null) {
            return;
        }
        for (Entry<InetSocketAddress, EndpointState> stateEntry : newEndpointStates.entrySet()) {
            InetSocketAddress epAddress = stateEntry.getKey();
            EndpointState remoteState = stateEntry.getValue();

            EndpointState localState = this.endpointStateMap.get(epAddress);

            if (localState == null) {
                // there's no local state which means the node is new so add it
                this.endpointStateMap.put(epAddress, remoteState);
                this.liveNodes.add(epAddress);
            } else {
                int localGeneration = localState.getGeneration();
                int remoteGeneration = remoteState.getGeneration();
                if (remoteGeneration > localGeneration) {
                    // node restarted so apply everything
                    this.endpointStateMap.put(epAddress, remoteState);
                } else if (remoteGeneration == localGeneration) {
                    long remoteMaxVersion = remoteState.getMaxVersion();
                    long localMaxVersion = localState.getMaxVersion();
                    if (remoteMaxVersion > localMaxVersion) {
                        // apply the new states only
                        for (Entry<ApplicationState, VersionedValue> remoteEntry : remoteState.getApplicationState()
                                .entrySet()) {
                            ApplicationState remoteKey = remoteEntry.getKey();
                            VersionedValue remoteValue = remoteEntry.getValue();

                            localState.addApplicationState(remoteKey, remoteValue);
                        }
                    }
                }
            }
        }
    }

}
