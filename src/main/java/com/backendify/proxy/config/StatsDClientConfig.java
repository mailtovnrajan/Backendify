package com.backendify.proxy.config;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatsDClientConfig {

    @Bean
    public StatsDClient statsDClient() {
        // Initialize StatsD client with the STATSD_SERVER environment variable
        String statsdServer = System.getenv("STATSD_SERVER");
        System.out.println("STATSD_SERVER:"+ statsdServer);
       return new NonBlockingStatsDClient("", statsdServer, 8125);

    }
}