package com.swingledger.app.auth;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockHttpSession;
import java.util.UUID;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AccountApiTest {
    @Autowired WebApplicationContext context;
    MockMvc mvc;
    @BeforeEach void setup(){mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();}
    @Test void registrationLoginLogoutAndCsrf() throws Exception {
        String email=UUID.randomUUID()+"@test.local";
        String body="{\"name\":\"Test\",\"email\":\""+email+"\",\"password\":\"long-password-123\"}";
        mvc.perform(post("/api/v1/auth/register").contentType("application/json").content(body)).andExpect(status().isForbidden());
        var response=mvc.perform(post("/api/v1/auth/register").with(csrf()).contentType("application/json").content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.password_hash").doesNotExist()).andReturn();
        var session=(MockHttpSession)response.getRequest().getSession();
        mvc.perform(get("/api/v1/auth/me").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email));
        mvc.perform(post("/api/v1/auth/register").with(csrf()).contentType("application/json").content(body)).andExpect(status().isConflict());
        mvc.perform(post("/api/v1/auth/logout").with(csrf()).session(session)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/portfolios")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType("application/json").content(body.replace("long-password-123","wrong"))).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType("application/json").content(body)).andExpect(status().isOk());
    }
}
