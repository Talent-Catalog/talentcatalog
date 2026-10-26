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

package org.tctalent.server.integration.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.servlet.Filter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;
import org.tctalent.server.security.JwtTokenProvider;
import org.tctalent.server.security.TcUserDetails;
import org.tctalent.server.security.TcUserDetailsService;

/**
 * Regression test for HTTP authentication and authorization.
 * <p>
 * Requests for every path pattern in SecurityConfiguration (and a few others) are sent through the
 * real Spring Security filter chain - with no JWT, an invalid JWT, and real JWTs for users of each
 * role - and the security decision is recorded: "permit" (the request reaches the application),
 * or the HTTP status of the rejection (401 or 403). Controllers are not called.
 * </p>
 * <p>
 * The expected decisions (security/http-authorization-matrix.txt) were recorded with Spring Boot
 * 3.5.16 / Spring Security 6.5 before the Spring Boot 4 upgrade - TC-1189. Set the environment
 * variable TC_AUTHORIZATION_MATRIX_OUT to a file path to write the actual decisions instead.
 * </p>
 */
@SpringBootTest
@Transactional
class HttpAuthorizationMatrixIntegrationTest extends BaseDBIntegrationTest {

    private static final String EXPECTED = "security/http-authorization-matrix.txt";

    private static final List<String> ALL_METHODS = List.of("GET", "POST", "PUT", "DELETE");

    /** Paths which are not in SecurityConfiguration, to check the default rules */
    private static final List<String> EXTRA_PATHS = List.of(
        "/api/admin/not-a-real-endpoint", "/api/portal/not-a-real-endpoint", "/api/hal",
        "/api/hal/users", "/unknown", "/status", "/statusabc", "/status/abc", "/websocket/info");

    @MockitoBean
    private CandidateRedisCache candidateRedisCache;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private UserRepository userRepository;
    @Autowired private TcUserDetailsService tcUserDetailsService;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    private final AtomicBoolean reachedApplication = new AtomicBoolean();

    private MockMvc mockMvc;

    @Test
    @DisplayName("every path gets the same security decision for each kind of user as before")
    void authorizationMatrix() throws Exception {
        //The real security filter chain, followed by a filter which records that the request got
        //through instead of passing it on to the controllers.
        Filter recordReachedApplication = (request, response, chain) -> reachedApplication.set(true);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .addFilters(springSecurityFilterChain, recordReachedApplication)
            .build();
        Map<String, String> personas = personaTokens();

        StringBuilder actual = new StringBuilder();
        for (String request : requests()) {
            String[] methodAndPath = request.split(" ", 2);
            StringBuilder line = new StringBuilder(request);
            for (Map.Entry<String, String> persona : personas.entrySet()) {
                line.append(' ').append(persona.getKey()).append('=')
                    .append(decision(methodAndPath[0], methodAndPath[1], persona.getValue()));
            }
            actual.append(line).append('\n');
        }

        String out = System.getenv("TC_AUTHORIZATION_MATRIX_OUT");
        if (out != null) {
            Files.writeString(Path.of(out), actual.toString());
            return;
        }
        String expected = new ClassPathResource(EXPECTED).getContentAsString(StandardCharsets.UTF_8);
        assertEquals(expected, actual.toString());
    }

    /**
     * Returns "permit" if the request passes the security filter chain, otherwise the HTTP status
     * of the response.
     */
    private String decision(String method, String path, String jwt) throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(method, URI.create(path));
        if (jwt != null) {
            request.header("Authorization", "Bearer " + jwt);
        }
        reachedApplication.set(false);
        MvcResult result = mockMvc.perform(request).andReturn();
        return reachedApplication.get() ? "permit" : String.valueOf(result.getResponse().getStatus());
    }

    /** JWTs (or null) for each kind of user, in a fixed order */
    private Map<String, String> personaTokens() {
        Map<String, String> tokens = new LinkedHashMap<>();
        tokens.put("anonymous", null);
        tokens.put("invalidJwt", "not.a.valid.jwt");
        for (Role role : List.of(Role.user, Role.limited, Role.semilimited, Role.partneradmin,
            Role.admin, Role.systemadmin)) {
            tokens.put(role.name(), token(role, false));
        }
        tokens.put("readOnly", token(Role.admin, true));
        return tokens;
    }

    private String token(Role role, boolean readOnly) {
        User user = TestDataFactory.createUser(null);
        String name = "authz_" + role + (readOnly ? "_readonly_" : "_") + UUID.randomUUID();
        user.setUsername(name);
        user.setEmail(name + "@example.com");
        user.setRole(role);
        user.setReadOnly(readOnly);
        userRepository.saveAndFlush(user);

        TcUserDetails userDetails = tcUserDetailsService.loadUserByUsername(name);
        return jwtTokenProvider.generateToken(new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()));
    }

    /**
     * "METHOD path" for concrete examples of each path pattern in SecurityConfiguration, plus the
     * extra paths, sorted.
     */
    private static List<String> requests() throws IOException {
        String config = Files.readString(Path.of(
            "src/main/java/org/tctalent/server/configuration/SecurityConfiguration.java"));
        TreeSet<String> requests = new TreeSet<>();
        Matcher call = Pattern.compile("requestMatchers\\(([^)]*)\\)").matcher(config);
        while (call.find()) {
            Matcher method = Pattern.compile("HttpMethod\\.(\\w+)").matcher(call.group(1));
            List<String> methods = method.find() ? List.of(method.group(1)) : ALL_METHODS;
            Matcher pattern = Pattern.compile("\"([^\"]+)\"").matcher(call.group(1));
            while (pattern.find()) {
                for (String path : examplePaths(pattern.group(1))) {
                    for (String m : methods) {
                        requests.add(m + " " + path);
                    }
                }
            }
        }
        for (String path : EXTRA_PATHS) {
            for (String m : ALL_METHODS) {
                requests.add(m + " " + path);
            }
        }
        return new ArrayList<>(requests);
    }

    private static List<String> examplePaths(String pattern) {
        String p = pattern.replaceAll("\\{[^}]*}", "1");
        List<String> paths = new ArrayList<>();
        if (p.endsWith("/**")) {
            String base = p.substring(0, p.length() - 3);
            paths.add(concrete(base.isEmpty() ? "/" : base));
            paths.add(concrete(base + "/a/b"));
        } else {
            paths.add(concrete(p));
        }
        return paths;
    }

    private static String concrete(String path) {
        return path.replace("**", "x").replace("*", "1");
    }
}
