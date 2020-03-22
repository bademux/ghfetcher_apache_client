package com.github.bademux.ghfetcher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class SystemConfig {

    @Bean
    public static Clock clock() {
        return Clock.systemUTC();
    }

}