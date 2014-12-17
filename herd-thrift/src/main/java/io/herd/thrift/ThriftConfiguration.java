package io.herd.thrift;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DefaultThriftConfiguration.class)
public interface ThriftConfiguration {

    int getPort();

}
