package com.venikkin.vertx.ext.web.client.aws;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.impl.SocketAddressImpl;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import net.javacrumbs.jsonunit.assertj.JsonAssert;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import net.javacrumbs.jsonunit.core.Option;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.Output;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.DISPLAY_NAME_PLACEHOLDER;

@ExtendWith(VertxExtension.class)
class AwsSigningHttpContextInterceptorTest {

    private final static int TEST_TIMEOUT_SECS = 30;

    private static final Map<String, WebClient> configuredClients = new HashMap<>();
    private static final Map<String, WebClient> notConfiguredClients = new HashMap<>();

    private static URL testUrl;

    private static AwsSigningOptions signingOptions;

    @BeforeAll
    static void init(final Vertx vertx) throws MalformedURLException {
        final ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create("aws-test");
        final CloudFormationClient cloudformation = CloudFormationClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.EU_WEST_1)
                .build();
        final DescribeStacksResponse stacks = cloudformation.describeStacks(DescribeStacksRequest.builder()
                .stackName("vertx-web-client-4aws-test-dev")
                .build());
        if (!stacks.hasStacks()) {
            throw new IllegalStateException(
                    "Cannot find test stack. Make sure you provisioned stack with task 'deployTestStack'");
        }
        final String testEndpoint = stacks.stacks().get(0).outputs().stream()
                .filter(o -> "ServiceEndpoint".equals(o.outputKey()))
                .findFirst()
                .map(Output::outputValue)
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot find test service endpoint. Make sure test stack is provisioned from provided template"));

        testUrl = new URL(testEndpoint);
        signingOptions = new AwsSigningOptions()
                .setCredentialsProvider(credentialsProvider)
                .setRegion(Region.EU_WEST_1)
                .setClock(Clock.systemUTC());

        final WebClient vanillaClient = WebClient.create(vertx, new WebClientOptions()
                .setDefaultHost(testUrl.getHost())
                .setDefaultPort(443)
                .setSsl(true));
        final WebClient signingClient = AwsSigningWebClient.create(vanillaClient, signingOptions);

        configuredClients.put("echo", vanillaClient);
        configuredClients.put("secureEcho", signingClient);
        notConfiguredClients.put("echo", WebClient.create(vertx));
        notConfiguredClients.put("secureEcho", AwsSigningWebClient.create(vertx, signingOptions));
    }

    static List<Example> getRequests() {
        final List<Example> getExamples = new ArrayList<>();
        configuredClients.forEach((path, client) -> {
            getExamples.add(example(path + " GET request", () ->
                    client.get("/dev/" + path)));
            getExamples.add(example(path + " GET request with method", () ->
                    client.request(HttpMethod.GET, "/dev/" + path)));
            getExamples.add(example(path + " GET request by method, server address, path", () ->
                    client.request(HttpMethod.GET, new SocketAddressImpl(443, testUrl.getHost()), "/dev/" + path)));
        });

        notConfiguredClients.forEach((path, client) -> {
            getExamples.add(example(path + " GET request by absolute path", () ->
                    client.getAbs(testUrl.toString() + "/" + path)));
            getExamples.add(example(path + " GET request by host, path", () ->
                    client.get(testUrl.getHost(), "/dev/" + path)));
            getExamples.add(example(path + " GET request with port, host, path", () ->
                    client.get(80, testUrl.getHost(), "/dev/" + path)));

            getExamples.add(example(path + " GET request by absolute path with method", () ->
                    client.requestAbs(HttpMethod.GET, testUrl.toString() + "/" + path)));
            getExamples.add(example(path + " GET request by absolute path with method, server address", () ->
                    client.requestAbs(HttpMethod.GET, new SocketAddressImpl(443, testUrl.getHost()),
                            testUrl.toString() + "/" + path)));
            getExamples.add(example(path + " GET request by method, host, path", () ->
                    client.request(HttpMethod.GET, testUrl.getHost(), "/dev/" + path)));
            getExamples.add(example(path + " GET request by method, port, host, path", () ->
                    client.request(HttpMethod.GET, 80, testUrl.getHost(), "/dev/" + path)));
            getExamples.add(example(path + " GET request by method and request options, ssl off", () ->
                    client.request(HttpMethod.GET, new RequestOptions()
                            .setHost(testUrl.getHost())
                            .setURI("/dev/" + path))));
            getExamples.add(example(path + " GET request by method and request options, ssl on", () ->
                    client.request(HttpMethod.GET, new RequestOptions()
                            .setHost(testUrl.getHost())
                            .setSsl(true)
                            .setPort(443)
                            .setURI("/dev/" + path))));
            getExamples.add(example(path + " GET request by method and request options, absolute url", () ->
                    client.request(HttpMethod.GET, new RequestOptions()
                            .setAbsoluteURI(testUrl.toString() + "/" + path)
                            .setSsl(true))));
            getExamples.add(example(path + "GET request by method, server address, host, path", () ->
                    client.request(HttpMethod.GET, new SocketAddressImpl(80, testUrl.getHost()), testUrl.getHost(),
                            "/dev/" + path)));
            getExamples.add(example(path + "GET request by method, server address, post, host, path", () ->
                    client.request(HttpMethod.GET, new SocketAddressImpl(80, testUrl.getHost()), 80, testUrl.getHost(),
                            "/dev/" + path)));
            getExamples.add(example(path + "GET request by method, server address, request options", () ->
                    client.request(HttpMethod.GET, new SocketAddressImpl(443, testUrl.getHost()), new RequestOptions()
                            .setHost(testUrl.getHost())
                            .setPort(443)
                            .setSsl(true)
                            .setURI("/dev/" + path))));
        });
        return getExamples;
    }

    @ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER + "[" + ARGUMENTS_PLACEHOLDER + "]")
    @MethodSource("getRequests")
    @Timeout(value = TEST_TIMEOUT_SECS, timeUnit = TimeUnit.SECONDS)
    void webClientCanCallGetMethod(final Example example,
                                   final VertxTestContext testContext) throws InterruptedException {
        example.requestSupplier.get()
                .addQueryParam("testParam", "testValue")
                .putHeader("customHeader", "testHeaderValue")
                .send(testContext.succeeding(response -> {
                    final SoftAssertions soft = new SoftAssertions();
                    soft.assertThat(response.statusCode()).as("status code").isEqualTo(200);
                    soft.check(() -> assertThatJson(response.bodyAsString()).as("response body")
                            .isEqualTo("{"
                                    + "queryParameters: {testParam: 'testValue'}, "
                                    + "headers: {customHeader: 'testHeaderValue'}, "
                                    + "method: 'GET'"
                                    + "}"));
                    soft.assertAll();
                    testContext.completeNow();
                }));
        awaitCompletion(testContext);
    }

    static List<Example> postRequests() {
        final List<Example> postExamples = new ArrayList<>();
        configuredClients.forEach((path, client) -> {
            postExamples.add(example(path + " POST request", () ->
                    client.post("/dev/" + path)));
            postExamples.add(example(path + " POST request by host, path", () ->
                    client.post(testUrl.getHost(), "/dev/" + path)));
            postExamples.add(example(path + " POST request with port, host, path", () ->
                    client.post(443, testUrl.getHost(), "/dev/" + path)));
        });
        notConfiguredClients.forEach((path, client) ->
                postExamples.add(example(path + " POST request by absolute path", () ->
                        client.postAbs(testUrl.toString() + "/" + path)))
        );
        return postExamples;
    }

    @ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER + "[" + ARGUMENTS_PLACEHOLDER + "]")
    @MethodSource("postRequests")
    @Timeout(value = TEST_TIMEOUT_SECS, timeUnit = TimeUnit.SECONDS)
    void webClientCanCallPostSendingBuffer(final Example example,
                                           final VertxTestContext testContext) throws InterruptedException {
        final Buffer buffer = Buffer.buffer("Hello from a test");
        example.requestSupplier.get()
                .putHeader("customHeader", "testHeaderValue")
                .sendBuffer(buffer, testContext.succeeding(response -> {
                    final SoftAssertions soft = new SoftAssertions();
                    soft.assertThat(response.statusCode()).as("status code").isEqualTo(200);
                    soft.check(() -> assertThatJson(response.bodyAsString()).as("response body")
                            .isEqualTo("{"
                                    + "headers: {customHeader: 'testHeaderValue'},"
                                    + "body: 'Hello from a test', "
                                    + "method: 'POST'"
                                    + "}"));
                    soft.assertAll();
                    testContext.completeNow();
                }));
        awaitCompletion(testContext);
    }

    @ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER + "[" + ARGUMENTS_PLACEHOLDER + "]")
    @MethodSource("postRequests")
    @Timeout(value = TEST_TIMEOUT_SECS, timeUnit = TimeUnit.SECONDS)
    void webClientCanCallPostSendingFormAsBuffer(final Example example,
                                           final VertxTestContext testContext) throws InterruptedException {
        final Buffer buffer = Buffer.buffer("formParameter1=formParameterValue1&formParameter2=formParameterValue2");
        example.requestSupplier.get()
                .putHeader("customHeader", "testHeaderValue")
                .putHeader("content-type", "application/x-www-form-urlencoded")
                .sendBuffer(buffer, testContext.succeeding(response -> {
                    final SoftAssertions soft = new SoftAssertions();
                    soft.assertThat(response.statusCode()).as("status code").isEqualTo(200);
                    soft.check(() -> assertThatJson(response.bodyAsString()).as("response body")
                            .isEqualTo("{"
                                    + "headers: {customHeader: 'testHeaderValue'},"
                                    + "body: {"
                                    + "  formParameter1: 'formParameterValue1',"
                                    + "  formParameter2: 'formParameterValue2'"
                                    + "}, "
                                    + "method: 'POST'"
                                    + "}"));
                    soft.assertAll();
                    testContext.completeNow();
                }));
        awaitCompletion(testContext);
    }

    @ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER + "[" + ARGUMENTS_PLACEHOLDER + "]")
    @MethodSource("postRequests")
    @Timeout(value = TEST_TIMEOUT_SECS, timeUnit = TimeUnit.SECONDS)
    void webClientCanCallPostSendingJsonObject(final Example example,
                                               final VertxTestContext testContext) throws InterruptedException {
        final JsonObject jsonObject = new JsonObject()
                .put("paramKey", "paramValue")
                .put("paramArrayKey", new JsonArray().add("arrayValue1").add("arrayValue2"));
        example.requestSupplier.get()
                .putHeader("customHeader", "testHeaderValue")
                .sendJsonObject(jsonObject, testContext.succeeding(response -> {
                    final SoftAssertions soft = new SoftAssertions();
                    soft.assertThat(response.statusCode()).as("status code").isEqualTo(200);
                    soft.check(() -> assertThatJson(response.bodyAsString()).as("response body")
                            .isEqualTo("{"
                                    + "headers: {customHeader: 'testHeaderValue'},"
                                    + "body: {"
                                    + " paramKey: 'paramValue',"
                                    + " paramArrayKey: ['arrayValue1', 'arrayValue2']"
                                    + "}, "
                                    + "method: 'POST'"
                                    + "}"));
                    soft.assertAll();
                    testContext.completeNow();
                }));
        awaitCompletion(testContext);
    }

    @ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER + "[" + ARGUMENTS_PLACEHOLDER + "]")
    @MethodSource("postRequests")
    @Timeout(value = TEST_TIMEOUT_SECS, timeUnit = TimeUnit.SECONDS)
    void webClientCanCallPostSendingJsonCollection(final Example example,
                                                   final VertxTestContext testContext) throws InterruptedException {
        final Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put("paramKey", "paramValue");
        jsonObject.put("paramArrayKey", Arrays.asList("arrayValue1", "arrayValue2"));
        example.requestSupplier.get()
                .putHeader("customHeader", "testHeaderValue")
                .sendJson(jsonObject, testContext.succeeding(response -> {
                    final SoftAssertions soft = new SoftAssertions();
                    soft.assertThat(response.statusCode()).as("status code").isEqualTo(200);
                    soft.check(() -> assertThatJson(response.bodyAsString()).as("response body")
                            .isEqualTo("{"
                                    + "headers: {customHeader: 'testHeaderValue'},"
                                    + "body: {"
                                    + " paramKey: 'paramValue',"
                                    + " paramArrayKey: ['arrayValue1', 'arrayValue2']"
                                    + "}, "
                                    + "method: 'POST'"
                                    + "}"));
                    soft.assertAll();
                    testContext.completeNow();
                }));
        awaitCompletion(testContext);
    }

    public static class TestCustomClass {
        public String paramKey;
        public List<String> paramArrayKey;
    }

    @ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER + "[" + ARGUMENTS_PLACEHOLDER + "]")
    @MethodSource("postRequests")
    @Timeout(value = TEST_TIMEOUT_SECS, timeUnit = TimeUnit.SECONDS)
    void webClientCanCallPostSendingJsonCustomClass(final Example example,
                                                    final VertxTestContext testContext) throws InterruptedException {
        final TestCustomClass jsonObject = new TestCustomClass();
        jsonObject.paramKey = "paramValue";
        jsonObject.paramArrayKey = Arrays.asList("arrayValue1", "arrayValue2");
        example.requestSupplier.get()
                .putHeader("customHeader", "testHeaderValue")
                .sendJson(jsonObject, testContext.succeeding(response -> {
                    final SoftAssertions soft = new SoftAssertions();
                    soft.assertThat(response.statusCode()).as("status code").isEqualTo(200);
                    soft.check(() -> assertThatJson(response.bodyAsString()).as("response body")
                            .isEqualTo("{"
                                    + "headers: {customHeader: 'testHeaderValue'},"
                                    + "body: {"
                                    + " paramKey: 'paramValue',"
                                    + " paramArrayKey: ['arrayValue1', 'arrayValue2']"
                                    + "}, "
                                    + "method: 'POST'"
                                    + "}"));
                    soft.assertAll();
                    testContext.completeNow();
                }));
        awaitCompletion(testContext);
    }

    @ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER + "[" + ARGUMENTS_PLACEHOLDER + "]")
    @MethodSource("postRequests")
    @Timeout(value = TEST_TIMEOUT_SECS, timeUnit = TimeUnit.SECONDS)
    void webClientCanCallPostSendingMultiValueForm(final Example example,
                                                   final VertxTestContext testContext) throws InterruptedException {
        final MultiMap multiValueMap = MultiMap.caseInsensitiveMultiMap();
        multiValueMap.set((CharSequence) "paramA", Arrays.asList("valueA1", "valueA2"));
        multiValueMap.set((CharSequence) "paramB", "valueB");
        example.requestSupplier.get()
                .sendForm(multiValueMap, testContext.succeeding(response -> {
                    final SoftAssertions soft = new SoftAssertions();
                    soft.assertThat(response.statusCode()).as("status code").isEqualTo(200);
                    soft.check(() -> assertThatJson(response.bodyAsString()).as("response body")
                            .isEqualTo("{"
                                    + "body: {"
                                    + " paramA: ['valueA1', 'valueA2'],"
                                    + " paramB: 'valueB'"
                                    + "}, "
                                    + "method: 'POST'"
                                    + "}"));
                    soft.assertAll();
                    testContext.completeNow();
                }));
        awaitCompletion(testContext);
    }

    @ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER + "[" + ARGUMENTS_PLACEHOLDER + "]")
    @MethodSource("postRequests")
    @Timeout(value = TEST_TIMEOUT_SECS, timeUnit = TimeUnit.SECONDS)
    void webClientCanCallPostSendingFormEncoded(final Example example,
                                                final VertxTestContext testContext) throws InterruptedException {
        final MultiMap multiValueMap = MultiMap.caseInsensitiveMultiMap();
        multiValueMap.set((CharSequence) "=", "%&");
        example.requestSupplier.get()
                .sendForm(multiValueMap, testContext.succeeding(response -> {
                    final SoftAssertions soft = new SoftAssertions();
                    soft.assertThat(response.statusCode()).as("status code").isEqualTo(200);
                    soft.check(() -> assertThatJson(response.bodyAsString()).as("response body")
                            .isEqualTo("{"
                                    + "body: {"
                                    + " '=': '%&'"
                                    + "}, "
                                    + "method: 'POST'"
                                    + "}"));
                    soft.assertAll();
                    testContext.completeNow();
                }));
        awaitCompletion(testContext);
    }

    @Test
    @Timeout(value = TEST_TIMEOUT_SECS, timeUnit = TimeUnit.SECONDS)
    void webClientCanSendRequestWithApiKey(final Vertx vertx, final VertxTestContext testContext)
            throws InterruptedException {
        final AwsSigningWebClient client = AwsSigningWebClient.create(vertx, new WebClientOptions()
                        .setDefaultHost(testUrl.getHost())
                        .setDefaultPort(443)
                        .setSsl(true), signingOptions.copy().setApiKey("piper-at-the-gates-of-dawn"));
        client.get("/dev/privateEcho")
                .addQueryParam("testParam", "testValue")
                .putHeader("customHeader", "testHeaderValue")
                .send(testContext.succeeding(response -> {
                    final SoftAssertions soft = new SoftAssertions();
                    soft.assertThat(response.statusCode()).as("status code").isEqualTo(200);
                    soft.check(() -> assertThatJson(response.bodyAsString()).as("response body")
                            .isEqualTo("{"
                                    + "queryParameters: {testParam: 'testValue'}, "
                                    + "headers: {customHeader: 'testHeaderValue'}, "
                                    + "method: 'GET'"
                                    + "}"));
                    soft.assertAll();
                    testContext.completeNow();
                }));
        awaitCompletion(testContext);
    }


    static List<Example> putRequests() {
        final List<Example> putExamples = new ArrayList<>();
        configuredClients.forEach((path, client) -> {
            putExamples.add(example(path + " PUT request", () ->
                    client.put("/dev/" + path)));
            putExamples.add(example(path + " PUT request by host, path", () ->
                    client.put(testUrl.getHost(), "/dev/" + path)));
            putExamples.add(example(path + " PUT request with port, host, path", () ->
                    client.put(443, testUrl.getHost(), "/dev/" + path)));
        });
        notConfiguredClients.forEach((path, client) ->
                putExamples.add(example(path + " PUT request by absolute path", () ->
                        client.putAbs(testUrl.toString() + "/" + path)))
        );
        return putExamples;
    }

    @ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER + "[" + ARGUMENTS_PLACEHOLDER + "]")
    @MethodSource("putRequests")
    @Timeout(value = TEST_TIMEOUT_SECS, timeUnit = TimeUnit.SECONDS)
    void webClientCanCallPutSendingBuffer(final Example example,
                                          final VertxTestContext testContext) throws InterruptedException {
        final Buffer buffer = Buffer.buffer("Hello from a test");
        example.requestSupplier.get()
                .putHeader("customHeader", "testHeaderValue")
                .sendBuffer(buffer, testContext.succeeding(response -> {
                    final SoftAssertions soft = new SoftAssertions();
                    soft.assertThat(response.statusCode()).as("status code").isEqualTo(200);
                    soft.check(() -> assertThatJson(response.bodyAsString()).as("response body")
                            .isEqualTo("{"
                                    + "headers: {customHeader: 'testHeaderValue'},"
                                    + "body: 'Hello from a test', "
                                    + "method: 'PUT'"
                                    + "}"));
                    soft.assertAll();
                    testContext.completeNow();
                }));
        awaitCompletion(testContext);
    }

    static List<Example> patchRequests() {
        final List<Example> patchExamples = new ArrayList<>();
        configuredClients.forEach((path, client) -> {
            patchExamples.add(example(path + " PATCH request", () ->
                    client.patch("/dev/" + path)));
            patchExamples.add(example(path + " PATCH request by host, path", () ->
                    client.patch(testUrl.getHost(), "/dev/" + path)));
            patchExamples.add(example(path + " PATCH request with port, host, path", () ->
                    client.patch(443, testUrl.getHost(), "/dev/" + path)));
        });
        notConfiguredClients.forEach((path, client) ->
                patchExamples.add(example(path + " PATCH request by absolute path", () ->
                        client.patchAbs(testUrl.toString() + "/" + path)))
        );
        return patchExamples;
    }

    @ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER + "[" + ARGUMENTS_PLACEHOLDER + "]")
    @MethodSource("patchRequests")
    @Timeout(value = TEST_TIMEOUT_SECS, timeUnit = TimeUnit.SECONDS)
    void webClientCanCallPatchSendingBuffer(final Example example,
                                          final VertxTestContext testContext) throws InterruptedException {
        final Buffer buffer = Buffer.buffer("Hello from a test");
        example.requestSupplier.get()
                .putHeader("customHeader", "testHeaderValue")
                .sendBuffer(buffer, testContext.succeeding(response -> {
                    final SoftAssertions soft = new SoftAssertions();
                    soft.assertThat(response.statusCode()).as("status code").isEqualTo(200);
                    soft.check(() -> assertThatJson(response.bodyAsString()).as("response body")
                            .isEqualTo("{"
                                    + "headers: {customHeader: 'testHeaderValue'},"
                                    + "body: 'Hello from a test', "
                                    + "method: 'PATCH'"
                                    + "}"));
                    soft.assertAll();
                    testContext.completeNow();
                }));
        awaitCompletion(testContext);
    }

    static List<Example> deleteRequests() {
        final List<Example> deleteExamples = new ArrayList<>();
        configuredClients.forEach((path, client) -> {
            deleteExamples.add(example(path + " DELETE request", () ->
                    client.delete("/dev/" + path)));
            deleteExamples.add(example(path + " DELETE request by host, path", () ->
                    client.delete(testUrl.getHost(), "/dev/" + path)));
            deleteExamples.add(example(path + " DELETE request with port, host, path", () ->
                    client.delete(443, testUrl.getHost(), "/dev/" + path)));
        });
        notConfiguredClients.forEach((path, client) ->
                deleteExamples.add(example(path + " DELETE request by absolute path", () ->
                        client.deleteAbs(testUrl.toString() + "/" + path)))
        );
        return deleteExamples;
    }

    @ParameterizedTest(name = DISPLAY_NAME_PLACEHOLDER + "[" + ARGUMENTS_PLACEHOLDER + "]")
    @MethodSource("deleteRequests")
    @Timeout(value = TEST_TIMEOUT_SECS, timeUnit = TimeUnit.SECONDS)
    void webClientCanCallDeleteSendingBuffer(final Example example,
                                             final VertxTestContext testContext) throws InterruptedException {
        final Buffer buffer = Buffer.buffer("Hello from a test");
        example.requestSupplier.get()
                .putHeader("customHeader", "testHeaderValue")
                .sendBuffer(buffer, testContext.succeeding(response -> {
                    final SoftAssertions soft = new SoftAssertions();
                    soft.assertThat(response.statusCode()).as("status code").isEqualTo(200);
                    soft.check(() -> assertThatJson(response.bodyAsString()).as("response body")
                            .isEqualTo("{"
                                    + "headers: {customHeader: 'testHeaderValue'},"
                                    + "body: 'Hello from a test', "
                                    + "method: 'DELETE'"
                                    + "}"));
                    soft.assertAll();
                    testContext.completeNow();
                }));
        awaitCompletion(testContext);
    }

    private static void awaitCompletion(final VertxTestContext testContext) throws InterruptedException {
        testContext.awaitCompletion(TEST_TIMEOUT_SECS, TimeUnit.SECONDS);
    }

    private static JsonAssert assertThatJson(final String s) {
        return JsonAssertions.assertThatJson(s).when(Option.IGNORING_EXTRA_FIELDS);
    }

    private static Example example(final String description,
                                   final Supplier<HttpRequest<Buffer>> requestSupplier) {
        return new Example(description, requestSupplier);
    }

    private static class Example {
        private final String description;
        private final Supplier<HttpRequest<Buffer>> requestSupplier;

        public Example(final String description,
                       final Supplier<HttpRequest<Buffer>> requestSupplier) {
            this.description = description;
            this.requestSupplier = requestSupplier;
        }

        @Override
        public String toString() {
            return description;
        }
    }

}
