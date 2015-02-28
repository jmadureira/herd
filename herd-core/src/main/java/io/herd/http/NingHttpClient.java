package io.herd.http;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;

import io.herd.base.Service;

public class NingHttpClient implements HttpClient, Service {
    
    private AsyncHttpClient delegate;

    public NingHttpClient() {
        this.delegate = new AsyncHttpClient();
    }

    @Override
    public void start() {
        // no-op
    }

    @Override
    public void stop() {
        this.delegate.close();
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public BoundRequestBuilder prepareGet(String url) {
        return delegate.prepareGet(url);
    }

}
