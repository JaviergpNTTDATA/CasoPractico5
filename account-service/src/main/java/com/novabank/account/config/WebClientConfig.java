package com.novabank.account.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /**
     * IMPORTANTE:
     * Para que Spring Cloud LoadBalancer intercepte URLs tipo http://client-service/...
     * la anotación @LoadBalanced debe aplicarse al WebClient.Builder, no al WebClient final.
     *
     * Si se anota el WebClient, el ExchangeFilterFunction de load-balancer no se instala
     * y WebClient intenta resolver "client-service" vía DNS -> UnknownHostException.
     */
    @Bean
    @LoadBalanced
    WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
