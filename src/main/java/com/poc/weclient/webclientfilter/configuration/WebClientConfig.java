package com.poc.weclient.webclientfilter.configuration;

import com.poc.weclient.webclientfilter.configuration.filter.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class WebClientConfig {
    @Bean
    public WebClient webClient(RequestInterceptor requestInterceptor) {
        return WebClient.builder()
                .baseUrl("http://localhost:2799")
                .filter(requestInterceptor)
                .build();
    }
}
