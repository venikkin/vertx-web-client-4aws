package com.venikkin.vertx.ext.web.client.aws;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.impl.HttpRequestImpl;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.uritemplate.Variables;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

public class AwsSigningHttpRequest<T> implements HttpRequest<T> {

    private final HttpRequestImpl<T> delegate;
    private final AwsSigningOptions signingOptions;
    private final Aws4Signer signer = Aws4Signer.create();

    private String protocol;

    AwsSigningHttpRequest(final HttpRequest<T> delegate, final AwsSigningOptions signingOptions) {
        if (delegate instanceof HttpRequestImpl) {
            this.delegate = (HttpRequestImpl<T>) delegate;
        } else {
            throw new ClassCastException(
                    "Internal vertx implementation has drifted and is not compatible with this interceptor");
        }
        this.signingOptions = signingOptions;
    }

    @Override
    public HttpRequest<T> method(HttpMethod value) {
        delegate.method(value);
        return this;
    }

    @Override
    public HttpMethod method() {
        return delegate.method();
    }

    @Override
    public HttpRequest<T> port(int value) {
        delegate.port(value);
        return this;
    }

    @Override
    public int port() {
        return delegate.port();
    }

    @Override
    public <U> HttpRequest<U> as(BodyCodec<U> responseCodec) {
        return new AwsSigningHttpRequest<>(delegate.as(responseCodec), signingOptions);
    }

    @Override
    public BodyCodec<T> bodyCodec() {
        return delegate.bodyCodec();
    }

    @Override
    public HttpRequest<T> host(String value) {
        delegate.host(value);
        return this;
    }

    @Override
    public String host() {
        return delegate.host();
    }

    @Override
    public HttpRequest<T> virtualHost(String value) {
        delegate.virtualHost(value);
        return this;
    }

    @Override
    public String virtualHost() {
        return delegate.virtualHost();
    }

    @Override
    public HttpRequest<T> uri(String value) {
        delegate.uri(value);
        return this;
    }

    @Override
    public String uri() {
        return delegate.uri();
    }

    @Override
    public HttpRequest<T> putHeaders(MultiMap headers) {
        delegate.putHeaders(headers);
        return this;
    }

    @Override
    public HttpRequest<T> putHeader(String name, String value) {
        delegate.putHeader(name, value);
        return this;
    }

    @Override
    public HttpRequest<T> putHeader(String name, Iterable<String> value) {
        delegate.putHeader(name, value);
        return this;
    }

    @Override
    public MultiMap headers() {
        return delegate.headers();
    }

    @Override
    public HttpRequest<T> authentication(Credentials credentials) {
        delegate.authentication(credentials);
        return this;
    }

    @Override
    public HttpRequest<T> ssl(Boolean value) {
        delegate.ssl(value);
        return this;
    }

    @Override
    public Boolean ssl() {
        return delegate.ssl();
    }

    @Override
    public HttpRequest<T> timeout(long value) {
        delegate.timeout(value);
        return this;
    }

    @Override
    public long timeout() {
        return delegate.timeout();
    }

    @Override
    public HttpRequest<T> addQueryParam(String paramName, String paramValue) {
        delegate.addQueryParam(paramName, paramValue);
        return this;
    }

    @Override
    public HttpRequest<T> setQueryParam(String paramName, String paramValue) {
        delegate.setQueryParam(paramName, paramValue);
        return this;
    }

    @Override
    public HttpRequest<T> setTemplateParam(String paramName, String paramValue) {
        return delegate.setTemplateParam(paramName, paramValue);
    }

    @Override
    public HttpRequest<T> setTemplateParam(String paramName, List<String> paramValue) {
        return delegate.setTemplateParam(paramName, paramValue);
    }

    @Override
    public HttpRequest<T> setTemplateParam(String paramName, Map<String, String> paramValue) {
        return delegate.setTemplateParam(paramName, paramValue);
    }

    @Override
    public HttpRequest<T> followRedirects(boolean value) {
        delegate.followRedirects(value);
        return this;
    }

    @Override
    public boolean followRedirects() {
        return delegate.followRedirects();
    }

    @Override
    public HttpRequest<T> proxy(ProxyOptions proxyOptions) {
        return delegate.proxy(proxyOptions);
    }

    @Override
    public ProxyOptions proxy() {
        return delegate.proxy();
    }

    @Override
    public HttpRequest<T> expect(ResponsePredicate predicate) {
        delegate.expect(predicate);
        return this;
    }

    @Override
    public List<ResponsePredicate> expectations() {
        return delegate.expectations();
    }

    @Override
    public MultiMap queryParams() {
        return delegate.queryParams();
    }

    @Override
    public Variables templateParams() {
        return delegate.templateParams();
    }

    @Override
    public HttpRequest<T> copy() {
        return new AwsSigningHttpRequest<>(delegate.copy(), signingOptions);
    }

    @Override
    public HttpRequest<T> multipartMixed(boolean allow) {
        delegate.multipartMixed(true);
        return this;
    }

    @Override
    public boolean multipartMixed() {
        return delegate.multipartMixed();
    }

    @Override
    public HttpRequest<T> traceOperation(String traceOperation) {
        return delegate.traceOperation(traceOperation);
    }

    @Override
    public String traceOperation() {
        return delegate.traceOperation();
    }

    public HttpRequest<T> protocol(final String protocol) {
        this.protocol = protocol;
        return this;
    }

    public String protocol() {
        return protocol;
    }

    // SigV4 has not been supported yet.
    @Override
    public void sendStream(ReadStream<Buffer> body, Handler<AsyncResult<HttpResponse<T>>> handler) {
        delegate.sendStream(body, handler);
    }

    @Override
    public void sendBuffer(Buffer body, Handler<AsyncResult<HttpResponse<T>>> handler) {
        prepareRequest();
        sign(bufferContentStreamProvider(body));
        delegate.sendBuffer(body, handler);
    }

    @Override
    public void sendJsonObject(JsonObject body, Handler<AsyncResult<HttpResponse<T>>> handler) {
        final Buffer buffer = body != null ? Buffer.buffer(body.encode()) : null;
        delegate.putHeader(CONTENT_TYPE.toString(), "application/json");
        sendBuffer(buffer, handler);
    }

    @Override
    public void sendJson(Object body, Handler<AsyncResult<HttpResponse<T>>> handler) {
        final Buffer buffer = body != null ? Json.encodeToBuffer(body) : null;
        delegate.putHeader(CONTENT_TYPE.toString(), "application/json");
        sendBuffer(buffer, handler);
    }

    @Override
    public void sendForm(MultiMap body, Handler<AsyncResult<HttpResponse<T>>> handler) {
        final Buffer buffer = urlEncodeParameters(body, StandardCharsets.UTF_8.name());
        delegate.putHeader(CONTENT_TYPE.toString(), "application/x-www-form-urlencoded");
        sendBuffer(buffer, handler);
    }

    @Override
    public void sendForm(MultiMap body, String charset, Handler<AsyncResult<HttpResponse<T>>> handler) {
        final Buffer buffer = urlEncodeParameters(body, charset);
        delegate.putHeader(CONTENT_TYPE.toString(), "application/x-www-form-urlencoded");
        sendBuffer(buffer, handler);
    }

    // SigV4 has not been supported yet.
    @Override
    public void sendMultipartForm(MultipartForm body, Handler<AsyncResult<HttpResponse<T>>> handler) {
        delegate.sendMultipartForm(body, handler);
    }

    @Override
    public void send(Handler<AsyncResult<HttpResponse<T>>> handler) {
        prepareRequest();
        sign(null);
        delegate.send(handler);
    }

    private void sign(final ContentStreamProvider contentStreamProvider) {
        final Aws4SignerParams aws4SignerParams = Aws4SignerParams.builder()
                .awsCredentials(signingOptions.getCredentialsProvider().resolveCredentials())
                .signingName(signingOptions.getServiceName())
                .signingRegion(signingOptions.getRegion())
                .timeOffset(signingOptions.getTimeOffset())
                .signingClockOverride(signingOptions.getClock())
                .build();

        final SdkHttpFullRequest.Builder requestBuilder = SdkHttpFullRequest.builder()
                .host(delegate.host())
                .port(delegate.port())
                .method(SdkHttpMethod.fromValue(delegate.method().name()))
                .protocol(protocol)
                .encodedPath(delegate.uri())
                .contentStreamProvider(contentStreamProvider);
        delegate.queryParams().forEach(entry ->
                requestBuilder.appendRawQueryParameter(entry.getKey(), entry.getValue())
        );

        final SdkHttpFullRequest signedRequest = signer.sign(requestBuilder.build(), aws4SignerParams);

        signedRequest.headers().forEach((key, values) ->
                values.forEach(value -> delegate.putHeader(key, value))
        );
    }

    private ContentStreamProvider bufferContentStreamProvider(final Buffer buffer) {
        if (buffer == null) {
            return null;
        }
        return () -> new ByteArrayInputStream(buffer.getBytes());
    }

    private void prepareRequest() {
        if (protocol == null) {
            protocol = delegate.ssl() != null && delegate.ssl() ? "https" : "http";
        }
        if (signingOptions.getApiKey() != null) {
            delegate.putHeader("x-api-key", signingOptions.getApiKey());
        }
    }

    private Buffer urlEncodeParameters(final MultiMap multiMap, String charset) {
        if (multiMap == null) {
            return null;
        }
        if (multiMap.isEmpty()) {
            return Buffer.buffer();
        }
        final StringBuilder sb = new StringBuilder();
        multiMap.forEach(pair ->
                sb.append(encode(pair.getKey(), charset))
                        .append('=')
                        .append(encode(pair.getValue(), charset))
                        .append('&')
        );
        return Buffer.buffer(sb.substring(0, sb.length() - 1));
    }

    private static String encode(final String s, String charset) {
        try {
            return URLEncoder.encode(s, charset);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

}
