package io.herd.http;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class HttpModule extends AbstractModule {

    private final HttpConfiguration httpConfiguration;

    public HttpModule() {
        this.httpConfiguration = null;
    }

    public HttpModule(HttpConfiguration thriftConfiguration) {
        this.httpConfiguration = thriftConfiguration;
    }

    @Override
    protected void configure() {
        bind(HttpClient.class).to(NingHttpClient.class).in(Singleton.class);
    }

    @Provides
    protected Http http() {
        return new Http(httpConfiguration);
    }

}
