package com.swingledger.app.common;

import java.util.Map;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class HealthController {
    private final HealthEndpoint health;

    public HealthController(HealthEndpoint health) {
        this.health = health;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        // Return only status publicly; dependency failures must fail deployment checks too.
        String status = health.health().getStatus().getCode();
        return ResponseEntity.status("UP".equals(status) ? 200 : 503).body(Map.of("status", status));
    }
}
