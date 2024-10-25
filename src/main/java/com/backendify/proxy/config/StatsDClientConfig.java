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
        String statsdServerEnv = System.getenv("STATSD_SERVER");
        String sarr[] = statsdServerEnv.split(":");
        String statsDServer = sarr[0];
        int statsDPort = Integer.parseInt(sarr[1]);
       return new NonBlockingStatsDClient("", statsDServer, statsDPort);
    }
}