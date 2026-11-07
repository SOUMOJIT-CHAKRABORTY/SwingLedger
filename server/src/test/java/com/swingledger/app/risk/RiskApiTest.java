package com.swingledger.app.risk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class RiskApiTest {
    @Autowired WebApplicationContext context;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void calculatesPublicRequestThroughSecurityAndController() throws Exception {
        mvc.perform(post("/api/v1/risk/calculate").contentType("application/json")
                .content("""
                {"capital":15000,"riskPercent":1,"entry":200,"stopLoss":194,"target":224}
                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(25))
                .andExpect(jsonPath("$.positionValue").value(5000))
                .andExpect(jsonPath("$.plannedLoss").value(150))
                .andExpect(jsonPath("$.riskBudget").value(150))
                .andExpect(jsonPath("$.rewardToRiskRatio").value(4));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "null", "{", "", "{\"capital\":\"bad\"}",
            "{\"capital\":15000,\"riskPercent\":1,\"entry\":200,\"stopLoss\":200,\"target\":224}"})
    void returnsSafeReadableValidationErrors(String body) throws Exception {
        mvc.perform(post("/api/v1/risk/calculate").contentType("application/json").content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").isNotEmpty()).andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void exposesHealthAndDeniesUnimplementedPrivateEndpoints() throws Exception {
        mvc.perform(get("/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/api/v1/portfolios")).andExpect(status().isUnauthorized());
    }
}
