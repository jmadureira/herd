package io.herd.gossip;

import java.util.HashMap;
import java.util.Map;

public interface GossipTestable {

    default EndpointState createEndpointState(int generation) {
        return createEndpointState(generation, new HashMap<ApplicationState, VersionedValue>());
    }
    
    default EndpointState createEndpointState(int generation, Map<ApplicationState, VersionedValue> applicationStates) {
        EndpointState endpointState = new EndpointState(new HeartBeatState(generation, 0));
        
        endpointState.getApplicationState().putAll(applicationStates);
        return endpointState;
    }
}
