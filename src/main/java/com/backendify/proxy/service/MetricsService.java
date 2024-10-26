package com.backendify.proxy.service;

import com.timgroup.statsd.StatsDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final StatsDClient statsDClient;

    @Autowired
    public MetricsService(StatsDClient statsDClient) {
        this.statsDClient = statsDClient;
    }

    public void incrementRequestCount() {
        statsDClient.incrementCounter("metric.1");
    }

    public void incrementCompanyV1ResponseCount() {
        statsDClient.incrementCounter("metric.2");
    }

    public void incrementCompanyV2ResponseCount() {
        statsDClient.incrementCounter("metric.3");
    }

    public void incrementUnexpectedContentTypeCount() {
        statsDClient.incrementCounter("metric.4");
    }

    public void incrementBackendErrorCount() {
        statsDClient.incrementCounter("metric.5");
    }
}

