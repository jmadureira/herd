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
    
    default void addApplicationState(Map<ApplicationState, VersionedValue> stateMap, ApplicationState state, String value) {
        stateMap.put(state, new VersionedValue(value));
    }
    
    default void addApplicationState(Map<ApplicationState, VersionedValue> stateMap, ApplicationState state, String value, long version) {
        stateMap.put(state, new VersionedValue(value, version));
    }
}
