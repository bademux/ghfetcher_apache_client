package com.github.bademux.ghfetcher.utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Properties;

public abstract class WiremockConfig {

    @Bean
    public abstract WireMockServer wireMock();

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties(WireMockServer wireMock) {
        PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
        Properties properties = new Properties();
        properties.setProperty("githubClient.rootUri", wireMock.baseUrl());
        pspc.setProperties(properties);
        return pspc;
    }
}
