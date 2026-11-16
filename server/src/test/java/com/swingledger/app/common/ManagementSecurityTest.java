package com.swingledger.app.common;

import java.net.URI;
import java.net.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "management.server.port=0", "management.server.address=127.0.0.1",
    "management.endpoints.web.exposure.include=health,prometheus",
    "management.endpoint.health.probes.enabled=true",
    "management.endpoint.health.group.readiness.include=readinessState,db",
    "management.endpoint.health.show-details=never",
    "management.metrics.tags.application=swingledger",
    "management.metrics.distribution.percentiles-histogram.http.server.requests=true"
})
class ManagementSecurityTest {
    @Value("${local.server.port}") int appPort;
    @Value("${local.management.port}") int managementPort;
    @Autowired WebApplicationContext context;
    @Autowired HealthEndpoint healthEndpoint;
    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> fetch(int port, String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Test void managementMetricsAreAvailableOnlyOnSeparateListener() throws Exception {
        assertNotEquals(appPort, managementPort);
        assertEquals(200, fetch(appPort, "/health").statusCode());
        var metrics = fetch(managementPort, "/actuator/prometheus");
        assertEquals(200, metrics.statusCode());
        assertTrue(metrics.body().contains("jvm_memory_used_bytes"));
        assertTrue(metrics.body().contains("application=\"swingledger\""));
        assertTrue(metrics.body().contains("http_server_requests_seconds_bucket"));
        assertTrue(fetch(appPort, "/actuator/prometheus").statusCode() >= 400);
        assertTrue(fetch(appPort, "/actuator/health").statusCode() >= 400);
        var spoofed = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + appPort + "/actuator/prometheus"))
                .header("X-Forwarded-Port", String.valueOf(managementPort)).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertTrue(spoofed.statusCode() >= 400);
        assertFalse(spoofed.body().contains("jvm_memory_used_bytes"));
    }

    @Test void readinessHidesDependencyDetailsAndOtherEndpointsStayClosed() throws Exception {
        var readiness = fetch(managementPort, "/actuator/health/readiness");
        assertEquals(200, readiness.statusCode());
        assertTrue(readiness.body().contains("UP"));
        assertFalse(readiness.body().contains("components"));
        assertNotNull(healthEndpoint.healthForPath("readiness", "db"));
        assertTrue(fetch(managementPort, "/actuator/env").statusCode() >= 400);
        assertEquals(401, fetch(appPort, "/api/v1/portfolios").statusCode());
    }

    @Test @WithMockUser
    void signedInUsersCannotScrapeOnApplicationPort() throws Exception {
        MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build()
                .perform(get("/actuator/prometheus").with(request -> {
                    request.setLocalPort(appPort);
                    return request;
                })).andExpect(status().isForbidden());
    }
}
