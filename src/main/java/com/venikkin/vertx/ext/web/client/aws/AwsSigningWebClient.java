package com.venikkin.vertx.ext.web.client.aws;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.uritemplate.UriTemplate;

public class AwsSigningWebClient implements WebClient {

    private final WebClient delegate;
    private final AwsSigningOptions signingOptions;

    public static AwsSigningWebClient create(final Vertx vertx, final AwsSigningOptions signingOptions) {
        return new AwsSigningWebClient(WebClient.create(vertx), signingOptions);
    }

    public static AwsSigningWebClient create(final Vertx vertx, final WebClientOptions webClientOptions,
                                      final AwsSigningOptions signingOptions) {
        return new AwsSigningWebClient(WebClient.create(vertx, webClientOptions), signingOptions);
    }

    public static AwsSigningWebClient create(final WebClient webClient, final AwsSigningOptions signingOptions) {
        return new AwsSigningWebClient(webClient, signingOptions);
    }

    private AwsSigningWebClient(final WebClient delegate, final AwsSigningOptions signingOptions) {
        this.delegate = delegate;
        this.signingOptions = signingOptions;
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, int port, String host, String requestURI) {
        return wrap(delegate.request(method, port, host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, int port, String host, String requestURI) {
        return wrap(delegate.request(method, serverAddress, port, host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, int port, String host, UriTemplate requestURI) {
        return wrap(delegate.request(method, serverAddress, port, host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, String host, String requestURI) {
        return wrap(delegate.request(method, host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String host, String requestURI) {
        return wrap(delegate.request(method, serverAddress, host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String host, UriTemplate requestURI) {
        return wrap(delegate.request(method, serverAddress, host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, String requestURI) {
        return wrap(delegate.request(method, requestURI));
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, String requestURI) {
        return wrap(delegate.request(method, serverAddress, requestURI));
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, UriTemplate requestURI) {
        return wrap(delegate.request(method, serverAddress, requestURI));
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, RequestOptions options) {
        return wrap(delegate.request(method, options));
    }

    @Override
    public HttpRequest<Buffer> request(HttpMethod method, SocketAddress serverAddress, RequestOptions options) {
        return wrap(delegate.request(method, serverAddress, options));
    }

    @Override
    public HttpRequest<Buffer> requestAbs(HttpMethod method, String absoluteURI) {
        return wrap(delegate.requestAbs(method, absoluteURI));
    }

    @Override
    public HttpRequest<Buffer> requestAbs(HttpMethod method, SocketAddress serverAddress, String absoluteURI) {
        return wrap(delegate.requestAbs(method, serverAddress, absoluteURI));
    }

    @Override
    public HttpRequest<Buffer> requestAbs(HttpMethod method, SocketAddress serverAddress, UriTemplate absoluteURI) {
        return wrap(delegate.requestAbs(method, serverAddress, absoluteURI));
    }

    @Override
    public HttpRequest<Buffer> get(String requestURI) {
        return wrap(delegate.get(requestURI));
    }

    @Override
    public HttpRequest<Buffer> get(int port, String host, String requestURI) {
        return wrap(delegate.get(port, host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> get(String host, String requestURI) {
        return wrap(delegate.get(host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> getAbs(String absoluteURI) {
        return wrap(delegate.getAbs(absoluteURI));
    }

    @Override
    public HttpRequest<Buffer> post(String requestURI) {
        return wrap(delegate.post(requestURI));
    }

    @Override
    public HttpRequest<Buffer> post(int port, String host, String requestURI) {
        return wrap(delegate.post(port, host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> post(String host, String requestURI) {
        return wrap(delegate.post(host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> postAbs(String absoluteURI) {
        return wrap(delegate.postAbs(absoluteURI));
    }

    @Override
    public HttpRequest<Buffer> put(String requestURI) {
        return wrap(delegate.put(requestURI));
    }

    @Override
    public HttpRequest<Buffer> put(int port, String host, String requestURI) {
        return wrap(delegate.put(port, host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> put(String host, String requestURI) {
        return wrap(delegate.put(host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> putAbs(String absoluteURI) {
        return wrap(delegate.putAbs(absoluteURI));
    }

    @Override
    public HttpRequest<Buffer> delete(String requestURI) {
        return wrap(delegate.delete(requestURI));
    }

    @Override
    public HttpRequest<Buffer> delete(int port, String host, String requestURI) {
        return wrap(delegate.delete(port, host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> delete(String host, String requestURI) {
        return wrap(delegate.delete(host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> deleteAbs(String absoluteURI) {
        return wrap(delegate.deleteAbs(absoluteURI));
    }

    @Override
    public HttpRequest<Buffer> patch(String requestURI) {
        return wrap(delegate.patch(requestURI));
    }

    @Override
    public HttpRequest<Buffer> patch(int port, String host, String requestURI) {
        return wrap(delegate.patch(port, host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> patch(String host, String requestURI) {
        return wrap(delegate.patch(host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> patchAbs(String absoluteURI) {
        return wrap(delegate.patchAbs(absoluteURI));
    }

    @Override
    public HttpRequest<Buffer> head(String requestURI) {
        return wrap(delegate.head(requestURI));
    }

    @Override
    public HttpRequest<Buffer> head(int port, String host, String requestURI) {
        return wrap(delegate.head(port, host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> head(String host, String requestURI) {
        return wrap(delegate.head(host, requestURI));
    }

    @Override
    public HttpRequest<Buffer> headAbs(String absoluteURI) {
        return wrap(delegate.headAbs(absoluteURI));
    }

    @Override
    public void close() {
        delegate.close();
    }

    private <T> AwsSigningHttpRequest<T> wrap(final HttpRequest<T> request) {
        return new AwsSigningHttpRequest<>(request, signingOptions);
    }
}
