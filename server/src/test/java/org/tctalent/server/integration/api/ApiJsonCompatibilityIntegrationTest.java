/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.integration.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.integration.helper.TestDataFactory;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidatePropertyDefinition;
import org.tctalent.server.model.db.CandidatePropertyType;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidatePropertyDefinitionRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;

/**
 * Golden master test of JSON exchanged with the REST API: authentication (including TOTP), admin
 * endpoints, errors, pagination and the Spring Data REST /api/hal endpoints.
 * <p>
 * Requests go through the real security filter chain, controllers, Jackson message converters and
 * database. Volatile values (JWTs, generated ids and current timestamps) are normalized, keeping
 * their formats. The expected exchanges (api/api-json-exchanges.txt) were recorded with
 * Spring Boot 3.5.16 (Jackson 2) before the Spring Boot 4 upgrade - TC-1189. Set the environment
 * variable TC_API_EXCHANGES_OUT to a file path to write the actual exchanges instead.
 * </p>
 */

@SpringBootTest
@Transactional
class ApiJsonCompatibilityIntegrationTest extends BaseDBIntegrationTest {

    private static final String EXPECTED = "api/api-json-exchanges.txt";

    /** The password hashed by TestDataFactory.TEST_PASSWORD_HASH */
    private static final String PASSWORD = "password";

    private static final Pattern DATE_TIME = Pattern.compile(
        "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2})?(\\.\\d+)?(Z|[+-]\\d{2}:\\d{2})?");

    @MockitoBean
    private CandidateRedisCache candidateRedisCache;

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private UserRepository userRepository;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private CandidatePropertyDefinitionRepository candidatePropertyDefinitionRepository;

    private MockMvc mockMvc;

    /** Generated ids, and their labels used in the normalized output */
    private final Map<Long, String> ids = new LinkedHashMap<>();

    private final StringBuilder exchanges = new StringBuilder();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();
    }

    @Test
    @DisplayName("REST API JSON is the same as before the Spring Boot 4 upgrade")
    void apiJsonExchanges() throws Exception {
        User admin = user("tc1189_api_admin", Role.systemadmin, null);
        String mfaSecret = new DefaultSecretGenerator().generate();
        user("tc1189_api_mfa", Role.admin, mfaSecret);
        Candidate candidate = candidate(admin);
        CandidatePropertyDefinition definition = new CandidatePropertyDefinition();
        definition.setName("tc1189Property");
        definition.setLabel("TC-1189 property");
        definition.setDefinition(null);
        definition.setType(CandidatePropertyType.JSON);
        definition = candidatePropertyDefinitionRepository.saveAndFlush(definition);
        ids.put(definition.getId(), "<property.id>");

        //Authentication
        String loginResponse = exchange("login",
            post("/api/admin/auth/login", login("tc1189_api_admin", PASSWORD, null)), null);
        String jwt = extractJwt(loginResponse);
        exchange("login with wrong password",
            post("/api/admin/auth/login", login("tc1189_api_admin", "wrong", null)), null);
        exchange("login with unknown user",
            post("/api/admin/auth/login", login("tc1189_api_nobody", PASSWORD, null)), null);
        exchange("mfa login without code",
            post("/api/admin/auth/login", login("tc1189_api_mfa", PASSWORD, null)), null);
        exchange("mfa login with wrong code",
            post("/api/admin/auth/login", login("tc1189_api_mfa", PASSWORD, "000000")), null);
        exchange("mfa login with valid code",
            post("/api/admin/auth/login", login("tc1189_api_mfa", PASSWORD, totp(mfaSecret))),
            null);
        exchange("admin endpoint without jwt",
            MockMvcRequestBuilders.get("/api/admin/user/" + admin.getId()), null);

        //Admin endpoints
        exchange("get user", MockMvcRequestBuilders.get("/api/admin/user/" + admin.getId()), jwt);
        exchange("get unknown user", MockMvcRequestBuilders.get("/api/admin/user/999999999"), jwt);
        exchange("search users paged", post("/api/admin/user/search-paged",
            "{\"keyword\": \"tc1189_api\", \"pageNumber\": 0, \"pageSize\": 1,"
                + " \"sortFields\": [\"username\"], \"sortDirection\": \"ASC\"}"), jwt);
        //Only one invalid field, so that the reported error is deterministic
        exchange("validation error",
            post("/api/admin/language-level", "{\"status\": \"active\", \"level\": 1}"), jwt);
        exchange("malformed json",
            post("/api/admin/user/search-paged", "{\"keyword\": "), jwt);
        exchange("wrongly typed json value",
            post("/api/admin/user/search-paged", "{\"pageNumber\": \"abc\"}"), jwt);
        exchange("get candidate",
            MockMvcRequestBuilders.get("/api/admin/candidate/" + candidate.getId()), jwt);
        exchange("get candidate extended", MockMvcRequestBuilders.get(
            "/api/admin/candidate/number/" + candidate.getCandidateNumber()), jwt);

        //Spring Data REST
        exchange("hal root", MockMvcRequestBuilders.get("/api/hal"), jwt);
        exchange("hal collection",
            MockMvcRequestBuilders.get("/api/hal/candidate-property-definitions?size=2"), jwt);
        //As sent by the admin portal (Angular HttpClient)
        exchange("hal collection requested by angular", MockMvcRequestBuilders
            .get("/api/hal/candidate-property-definitions?page=0&size=20")
            .header("Accept", "application/json, text/plain, */*"), jwt);
        exchange("hal item", MockMvcRequestBuilders.get(
            "/api/hal/candidate-property-definitions/" + definition.getId()), jwt);
        exchange("hal write not exported",
            post("/api/hal/candidate-property-definitions", "{\"name\": \"x\"}"), jwt);

        String out = System.getenv("TC_API_EXCHANGES_OUT");
        if (out != null) {
            Files.writeString(Path.of(out), exchanges.toString());
            return;
        }
        String expected = new ClassPathResource(EXPECTED).getContentAsString(StandardCharsets.UTF_8);
        assertEquals(expected, exchanges.toString());
    }

    private String exchange(String name, MockHttpServletRequestBuilder request, String jwt)
        throws Exception {
        if (jwt != null) {
            request.header("Authorization", "Bearer " + jwt);
        }
        MvcResult result = mockMvc.perform(request).andReturn();
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        String contentType = result.getResponse().getContentType();
        exchanges.append("### ").append(name).append('\n')
            .append(result.getResponse().getStatus()).append(' ')
            .append(contentType == null ? "-" : contentType).append('\n')
            .append(normalize(body)).append("\n\n");
        return body;
    }

    private static MockHttpServletRequestBuilder post(String path, String json) {
        return MockMvcRequestBuilders.post(path)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json);
    }

    private static String login(String username, String password, String totpToken) {
        return "{\"username\": \"" + username + "\", \"password\": \"" + password + "\""
            + (totpToken == null ? "" : ", \"totpToken\": \"" + totpToken + "\"") + "}";
    }

    private static String totp(String secret) throws Exception {
        return new DefaultCodeGenerator().generate(secret,
            Math.floorDiv(Instant.now().getEpochSecond(), 30));
    }

    private static String extractJwt(String loginResponse) {
        Matcher m = Pattern.compile("\"accessToken\"\\s*:\\s*\"([^\"]+)\"").matcher(loginResponse);
        if (!m.find()) {
            throw new IllegalStateException("No accessToken in " + loginResponse);
        }
        return m.group(1);
    }

    private User user(String username, Role role, String mfaSecret) {
        User user = TestDataFactory.createUser(null);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setFirstName("Api");
        user.setLastName("Tester");
        user.setRole(role);
        user.setUsingMfa(mfaSecret != null);
        user.setMfaSecret(mfaSecret);
        user = userRepository.saveAndFlush(user);
        ids.put(user.getId(), "<" + username + ".id>");
        return user;
    }

    private Candidate candidate(User createdBy) {
        User candidateUser = user("tc1189_api_candidate", Role.user, null);
        Candidate candidate = new Candidate();
        candidate.setUser(candidateUser);
        candidate.setCandidateNumber("TC1189API1");
        candidate.setPhone("+61 400 000 000");
        candidate.setWhatsapp("+61 400 000 001");
        candidate.setContactConsentPartners(true);
        candidate.setContactConsentRegistration(false);
        candidate.setStatus(CandidateStatus.active);
        candidate.setUnhcrStatus(UnhcrStatus.RegisteredAsylum);
        candidate.setGender(Gender.female);
        candidate.setDob(LocalDate.of(1990, 5, 17));
        candidate.setLinkedInLink("https://linkedin.example/tc1189");
        candidate.setWorkAbroadNotes("Notes with \"quotes\", unicode café and\na new line");
        candidate.setCreatedBy(createdBy);
        candidate = candidateRepository.saveAndFlush(candidate);
        candidateUser.setCandidate(candidate);
        userRepository.saveAndFlush(candidateUser);
        ids.put(candidate.getId(), "<candidate.id>");
        return candidate;
    }

    /** Replaces JWTs, generated ids and timestamps, keeping the format of the timestamps. */
    private String normalize(String body) {
        String s = body.replaceAll("eyJ[\\w-]+\\.[\\w-]+\\.[\\w-]+", "<jwt>");
        //Error responses contain the current time in epoch milliseconds
        s = s.replaceAll("\"timestamp\":\\d+", "\"timestamp\":<epochMillis>");
        //Ids are replaced where they appear as an "id" value or in a URL path
        for (Map.Entry<Long, String> id : ids.entrySet()) {
            s = s.replaceAll("(\"id\"\\s*:\\s*)" + id.getKey() + "(?!\\d)",
                "$1" + Matcher.quoteReplacement(id.getValue()));
            s = s.replaceAll("/" + id.getKey() + "(?![\\d])",
                "/" + Matcher.quoteReplacement(id.getValue()));
        }
        Matcher m = DATE_TIME.matcher(s);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String shape = "dddd-dd-ddTdd:dd" + (m.group(1) == null ? "" : ":dd")
                + (m.group(2) == null ? "" : ".fraction")
                + (m.group(3) == null ? "" : "+hh:mm");
            m.appendReplacement(sb, shape);
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
