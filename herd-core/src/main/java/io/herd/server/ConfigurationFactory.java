package io.herd.server;

import io.herd.base.Configuration;
import io.herd.base.ConfigurationException;
import io.herd.base.FileInputStreamProvider;
import io.herd.base.ResourceProvider;
import io.herd.base.StreamProvider;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

class ConfigurationFactory<T extends Configuration> {

    private static final String DEFAULT_SERVER_FILE = "server.yaml";

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationFactory.class);

    private final Class<T> targetClass;
    private final StreamProvider configurationProvider;
    private final YAMLFactory yamlFactory;
    private final ObjectMapper objectMapper;

    ConfigurationFactory(Class<T> configurationClass) {
        this(configurationClass, new ResourceProvider(DEFAULT_SERVER_FILE));
    }

    private ConfigurationFactory(Class<T> configurationClass, StreamProvider streamProvider) {
        this.targetClass = configurationClass;
        this.configurationProvider = streamProvider;
        this.yamlFactory = new YAMLFactory();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new AfterburnerModule());
    }

    ConfigurationFactory(Class<T> configurationClass, String serverConfig) {
        this(configurationClass, new FileInputStreamProvider(serverConfig));
    }

    public T build() {
        logger.debug("Loading configuration from {}", this.configurationProvider);
        try (InputStream input = configurationProvider.open()) {
            final JsonNode node = objectMapper.readTree(yamlFactory.createParser(input));
            return build(node);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    private T build(JsonNode node) {
        try {
            if (node == null) {
                return targetClass.newInstance();
            }
            T result = objectMapper.readValue(new TreeTraversingParser(node), this.targetClass);
            return result;
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

}
