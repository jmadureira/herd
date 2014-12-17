package io.herd.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("default")
public class DefaultKafkaProducerConfiguration implements KafkaProducerConfiguration {

    private String metadataBrokerList;
    private String serializerClass;
    private String keySerializerClass;

    @JsonProperty("key.serializer.class")
    @Override
    public String getKeySerializerClass() {
        return keySerializerClass;
    }

    @JsonProperty(METADATA_BROKER_LIST)
    @Override
    public String getMetadataBrokerList() {
        return metadataBrokerList;
    }

    @JsonProperty(SERIALIZER_CLASS)
    @Override
    public String getSerializerClass() {
        return serializerClass;
    }

    @JsonProperty("key.serializer.class")
    public void setKeySerializerClass(String keySerializerClass) {
        this.keySerializerClass = keySerializerClass;
    }

    @JsonProperty(METADATA_BROKER_LIST)
    public void setMetadataBrokerList(String metadataBrokerList) {
        this.metadataBrokerList = metadataBrokerList;
    }

    @JsonProperty(SERIALIZER_CLASS)
    public void setSerializerClass(String serializerClass) {
        this.serializerClass = serializerClass;
    }

}
