package com.github.bademux.ghfetcher.service;

import com.github.bademux.ghfetcher.client.GithubClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GithubServiceConfig {

    @Bean
    public GithubService githubService(GithubClient githubClient) {
        return new GithubService(githubClient);
    }

}