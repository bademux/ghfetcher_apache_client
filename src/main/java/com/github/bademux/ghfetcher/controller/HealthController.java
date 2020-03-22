package com.github.bademux.ghfetcher.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * save a bit on startup by not enabling Actuator
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    ResponseEntity<?> health() {
        return ResponseEntity.ok().build();
    }

}
