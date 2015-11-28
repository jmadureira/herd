package io.herd.http;

import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;

public interface HttpClient {

    BoundRequestBuilder prepareGet(String url);

    BoundRequestBuilder preparePost(String url);

}
