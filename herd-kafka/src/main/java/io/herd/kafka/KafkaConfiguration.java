package io.herd.kafka;


import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DefaultKafkaConfiguration.class)
public interface KafkaConfiguration {
    
    String getKeySerializerClass();

    String getMetadataBrokerList();

    String getSerializerClass();

    /**
     * Converts this {@link KafkaConfiguration} instance to a {@link Properties} object.
     */
    default Properties toProperties() {
        Properties properties = new Properties();
        properties.put("metadata.broker.list", getMetadataBrokerList());
        properties.put("serializer.class", getSerializerClass());
        properties.put("key.serializer.class", getKeySerializerClass());
        return properties;
    }
}
