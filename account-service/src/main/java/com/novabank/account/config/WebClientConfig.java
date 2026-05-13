package com.novabank.account.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /**
     * Important:
     * For Spring Cloud LoadBalancer to intercept URLs like http://client-service/..., the @LoadBalanced annotation must be applied to the WebClient.Builder, not the final WebClient.
     * If the WebClient is annotated, the load-balancer's ExchangeFilterFunction is not installed and WebClient tries to resolve "client-service" via DNS -> UnknownHostException.
     * 
     * In this configuration, the WebClient.Builder is annotated with @LoadBalanced, allowing URLs with serviceId (e.g., http://client-service) to be intercepted and resolved by LoadBalancer.
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
