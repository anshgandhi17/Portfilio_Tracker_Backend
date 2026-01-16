package com.ansh.portfilio_tracker.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for creating RestTemplate bean.
 * RestTemplate is used for making HTTP requests to external APIs (e.g., Finnhub).
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a RestTemplate bean that can be injected into services.
     *
     * @return RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
