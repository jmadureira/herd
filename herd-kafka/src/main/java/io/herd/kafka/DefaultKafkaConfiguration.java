package io.herd.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("default")
public class DefaultKafkaConfiguration implements KafkaConfiguration {

    private String metadataBrokerList;
    private String serializerClass;
    private String keySerializerClass;

    @JsonProperty("key.serializer.class")
    @Override
    public String getKeySerializerClass() {
        return keySerializerClass;
    }

    @JsonProperty("metadata.broker.list")
    @Override
    public String getMetadataBrokerList() {
        return metadataBrokerList;
    }

    @JsonProperty("serializer.class")
    @Override
    public String getSerializerClass() {
        return serializerClass;
    }

    @JsonProperty("key.serializer.class")
    public void setKeySerializerClass(String keySerializerClass) {
        this.keySerializerClass = keySerializerClass;
    }

    @JsonProperty("metadata.broker.list")
    public void setMetadataBrokerList(String metadataBrokerList) {
        this.metadataBrokerList = metadataBrokerList;
    }

    @JsonProperty("serializer.class")
    public void setSerializerClass(String serializerClass) {
        this.serializerClass = serializerClass;
    }

}
