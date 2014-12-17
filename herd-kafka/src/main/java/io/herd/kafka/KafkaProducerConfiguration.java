package io.herd.kafka;

import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DefaultKafkaProducerConfiguration.class)
public interface KafkaProducerConfiguration {

    /**
     * This is for bootstrapping and the producer will only use it for getting metadata (topics, partitions and
     * replicas). The socket connections for sending the actual data will be established based on the broker information
     * returned in the metadata. The format is host1:port1,host2:port2, and the list can be a subset of brokers or a VIP
     * pointing to a subset of brokers.
     */
    String METADATA_BROKER_LIST = "metadata.broker.list";
    /**
     * The serializer class for messages. The default encoder takes a byte[] and returns the same byte[].
     */
    String SERIALIZER_CLASS = "serializer.class";

    String getKeySerializerClass();

    String getMetadataBrokerList();

    String getSerializerClass();

    /**
     * Converts this {@link KafkaProducerConfiguration} instance to a {@link Properties} object.
     */
    default Properties toProperties() {
        Properties properties = new Properties();
        properties.put(METADATA_BROKER_LIST, getMetadataBrokerList());
        properties.put(SERIALIZER_CLASS, getSerializerClass());
        properties.put("key.serializer.class", getKeySerializerClass());
        return properties;
    }

}
