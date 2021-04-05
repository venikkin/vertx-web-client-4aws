package com.venikkin.vertx.ext.web.client.aws;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.time.Clock;

public class AwsSigningOptions {

    private AwsCredentialsProvider credentialsProvider;
    private Region region;
    private Clock clock;
    private Integer timeOffset;
    private String apiKey;
    // alternative service name had not supported yet
    private String serviceName = "execute-api";

    public AwsCredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public AwsSigningOptions setCredentialsProvider(final AwsCredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    public Region getRegion() {
        return region;
    }

    public AwsSigningOptions setRegion(final Region region) {
        this.region = region;
        return this;
    }

    public Clock getClock() {
        return clock;
    }

    public AwsSigningOptions setClock(final Clock clock) {
        this.clock = clock;
        return this;
    }

    public Integer getTimeOffset() {
        return timeOffset;
    }

    public AwsSigningOptions setTimeOffset(final Integer timeOffset) {
        this.timeOffset = timeOffset;
        return this;
    }

    public AwsSigningOptions setApiKey(final String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getServiceName() {
        return serviceName;
    }

    public AwsSigningOptions setServiceName(final String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public AwsSigningOptions copy() {
        return new AwsSigningOptions()
                .setApiKey(apiKey)
                .setClock(clock)
                .setRegion(region)
                .setCredentialsProvider(credentialsProvider)
                .setTimeOffset(timeOffset)
                .setServiceName(serviceName);
    }
}
