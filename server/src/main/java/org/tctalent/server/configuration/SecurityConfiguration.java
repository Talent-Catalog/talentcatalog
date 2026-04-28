/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.configuration;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.security.LanguageFilter;
import org.tctalent.server.security.OAuth2UserAuthenticationConverter;

/**
 * Talent Catalog security configuration.
 * <p/>
 * See <a href="https://docs.spring.io/spring-security/site/docs/3.2.0.RC2/reference/htmlsingle/#jc">...</a>
 * also
 * https://www.marcobehler.com/guides/spring-security
 * <p/>
 * Summary of Talent Catalog security:
 *
 * <ul>
 *     <li>
 *         We authenticate users using an Oauth2 server (eg Keycloak or Cognito).
 *         That server supplies JSON Web Tokens (JWTs) which appear on each HTTP request in the
 *         Authorization header as a Bearer token.
 *         See https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Authorization
 *         and https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication#authentication_schemes.
 *     </li>
 *     <li>
 *         We manage users' details, including their roles, stored in the user's table of the
 *         database. User details are retrieved from the user table by
 *         {@link OAuth2UserAuthenticationConverter}.
 *     </li>
 * </ul>
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true)
public class SecurityConfiguration {

    @Autowired
    private Environment env;

    @Autowired
    private OAuth2UserAuthenticationConverter oAuth2UserAuthenticationConverter;

    /**
     * This filter chain is specifically designed for handling registration and login requests
     * for the portal API. It disables CSRF protection, sets session management to stateless,
     * and requires authentication for all requests. JWT authentication is enabled for this chain.
     * <p>
     * For logins and registrations, new users can be auto-created by the controller from the data
     * in the JWT token.
     * <p>
     * See {@link #filterChain(HttpSecurity)} for the normal filter chain configuration, where
     * if a user cannot be looked up from the JWT token, a 401 Unauthorized response is returned.
     * That logic is handled by {@link OAuth2UserAuthenticationConverter}
     */
    @Bean
    @Order(1)
    SecurityFilterChain registrationSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/portal/auth/register", "/api/portal/auth/login")
            .cors(withDefaults())
            .csrf(CsrfConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated())
            .oauth2ResourceServer(
                oauth2 -> oauth2.jwt(withDefaults()));

        return http.build();
    }

    @Bean
    @Order(2)
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            //Default is to use a Bean called corsConfigurationSource - defined
            //below.
            .cors(withDefaults())
            .csrf(CsrfConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/backend/jobseeker").permitAll()
                .requestMatchers("/api/portal/auth").permitAll()
                .requestMatchers("/api/portal/auth/**").permitAll()
                .requestMatchers("/api/portal/branding").permitAll()
                .requestMatchers("/api/portal/user/reset-password-email").permitAll()
                .requestMatchers("/api/portal/user/check-token").permitAll()
                .requestMatchers("/api/portal/user/reset-password").permitAll()
                .requestMatchers("/api/portal/language/system/**").permitAll()
                .requestMatchers("/api/portal/language/translations/**").permitAll()
                .requestMatchers("/api/portal/**").hasAnyRole("USER")
                .requestMatchers("/api/admin/auth").permitAll()
                .requestMatchers("/api/admin/auth/**").permitAll()
                .requestMatchers("/api/admin/branding").permitAll()
                .requestMatchers("/api/admin/terms-info/**").permitAll()
                .requestMatchers("/api/admin/user/reset-password-email").permitAll()
                .requestMatchers("/api/admin/user/check-token").permitAll()
                .requestMatchers("/api/admin/user/reset-password").permitAll()
                .requestMatchers("/api/admin/user/verify-email/**").permitAll()
                .requestMatchers("/").permitAll()
                .requestMatchers("/files/**").permitAll()
                .requestMatchers("/published/**").permitAll()

                .requestMatchers("/websocket","/websocket/**").permitAll()
                .requestMatchers("/app/**","/app/**").permitAll()
                .requestMatchers("/topic", "/topic/**").permitAll()
                .requestMatchers("/status**", "/status/**").permitAll()


                // DELETE: DELETE SAVE SEARCHES
                .requestMatchers(HttpMethod.DELETE, "/api/admin/saved-search/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // DELETE: DELETE LIST
                .requestMatchers(HttpMethod.DELETE, "/api/admin/saved-list/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // DELETE: DELETE ATTACHMENT
                .requestMatchers(HttpMethod.DELETE, "/api/admin/candidate-attachment/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                // DELETE: DELETE EDUCATION
                .requestMatchers(HttpMethod.DELETE, "/api/admin/candidate-education/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                // DELETE: DELETE CANDIDATE EXAM (INTAKE INTERVIEW)
                .requestMatchers(HttpMethod.DELETE, "/api/admin/candidate-exam/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                // DELETE: DELETE CANDIDATE CITIZENSHIP (INTAKE INTERVIEW)
                .requestMatchers(HttpMethod.DELETE, "/api/admin/candidate-citizenship/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                // DELETE: DELETE CANDIDATE DEPENDANT (INTAKE INTERVIEW)
                .requestMatchers(HttpMethod.DELETE, "/api/admin/candidate-dependant/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                // DELETE: DELETE CANDIDATE JOB EXPERIENCE
                .requestMatchers(HttpMethod.DELETE, "/api/admin/candidate-job-experience/*").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN")

                // DELETE: DELETE CANDIDATE LANGUAGE
                .requestMatchers(HttpMethod.DELETE, "/api/admin/candidate-language/*").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN")

                // DELETE: DELETE USER (ADDED AUTHORISATION ON SERVER FOR SOURCE PARTNER ADMINS)
                .requestMatchers(HttpMethod.DELETE, "/api/admin/user/*").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN")

                // DELETE: DELETE VISA CHECK
                .requestMatchers(HttpMethod.DELETE, "/api/admin/candidate-visa-check/*").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN")

                // DELETE: DELETE VISA JOB CHECK
                .requestMatchers(HttpMethod.DELETE, "/api/admin/candidate-visa-job/*").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN")

                // DELETE: DELETE CHAT POST LINK PREVIEW
                .requestMatchers(HttpMethod.DELETE, "/api/admin/link-preview/*").permitAll()

                // ADMIN ONLY RESTRICTIONS
                    // All OTHER DELETE end points
                .requestMatchers(antMatcher(HttpMethod.DELETE, "/api/admin/**/*")).hasAnyRole("SYSTEMADMIN", "ADMIN")
                    // Migrate database
                .requestMatchers("/api/admin/system/migrate").hasAnyRole("SYSTEMADMIN", "ADMIN")

                    // UPDATE/EDIT SETTINGS
                .requestMatchers(HttpMethod.PUT,
                        "/api/admin/country/*",
                        "/api/admin/nationality/*",
                        "/api/admin/language/*",
                        "/api/admin/language-level/*",
                        "/api/admin/occupation/*",
                        "/api/admin/education-level/*",
                        "/api/admin/education-major/*",
                        "/api/admin/translation/*",
                        "/api/admin/translation/file/*").hasAnyRole("SYSTEMADMIN", "ADMIN")
                    // CREATE GENERAL SETTINGS
                .requestMatchers(HttpMethod.POST,
                        "/api/admin/country",
                        "/api/admin/nationality",
                        "/api/admin/language",
                        "/api/admin/language-level",
                        "/api/admin/occupation",
                        "/api/admin/education-level",
                        "/api/admin/education-major").hasAnyRole("SYSTEMADMIN", "ADMIN")

                // CREATE/UPDATE USERS
                .requestMatchers(HttpMethod.PUT, "/api/admin/user/*").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "READONLY")
                .requestMatchers(HttpMethod.POST, "/api/admin/user/*").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "READONLY")

                // SEE CANDIDATE FILE ATTACHMENTS. ADMIN/SOURCE PARTNER ADMIN ALLOWED. READ ONLY has access BUT has the data restricted in the DTO based on role.
                .requestMatchers(HttpMethod.POST, "/api/admin/candidate-attachment/search").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "READONLY")

                // POST: REQUEST INFOGRAPHICS
                .requestMatchers(HttpMethod.POST, "/api/admin/candidate/stat/all").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: REQUEST PRESET
                .requestMatchers(HttpMethod.POST, "/api/admin/preset/*/guest-token").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                /*
                 * CHECKING CHATS
                 */
                .requestMatchers(HttpMethod.POST, "/api/admin/candidate/check-unread-chats").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")
                .requestMatchers(HttpMethod.POST, "/api/admin/opp/check-unread-chats").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")
                .requestMatchers(HttpMethod.POST, "/api/admin/job/check-unread-chats").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                /*
                 * SAVED SEARCH ENDPOINTS
                */
                // POST: CREATE SAVED SEARCHES
                .requestMatchers(HttpMethod.POST, "/api/admin/saved-search").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE SAVED SEARCHES
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-search/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE SAVED SEARCH DESCRIPTION
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-search/description/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE CONTEXT NOTES
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-search/context/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // GET: LOAD SAVE SEARCHES
                .requestMatchers(HttpMethod.GET, "/api/admin/saved-search/*/load").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: SELECT CANDIDATE SAVED SEARCHES
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-search/select-candidate/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // GET: SELECTION COUNT SAVED SEARCHES
                .requestMatchers(HttpMethod.GET, "/api/admin/saved-search/get-selection-count/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: SAVE SELECTION SAVED SEARCHES
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-search/save-selection/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE STATUSES
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-search/update-selected-statuses/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                // PUT: CLEAR SELECTION SAVED SEARCHES
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-search/clear-selection/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: SELECT COLUMNS SAVED SEARCHES
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-search/displayed-fields/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: EXPORT SAVE SELECTION SAVED SEARCHES
                .requestMatchers(HttpMethod.POST, "/api/admin/saved-search-candidate/*/export/csv").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                /*
                 * SEARCH ENDPOINTS
                 */
                // POST: ALL SEARCHES
                .requestMatchers(new AntPathRequestMatcher("/api/admin/**/search",HttpMethod.POST.name())).hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: ADD TO WATCHER
                .requestMatchers(new AntPathRequestMatcher("/api/admin/saved-search/watcher-add/*",HttpMethod.PUT.name())).hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: REMOVE FROM WATCHER
                .requestMatchers(new AntPathRequestMatcher("/api/admin/saved-search/watcher-remove/*",HttpMethod.PUT.name())).hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: ALL PAGED SEARCHES
                .requestMatchers(new AntPathRequestMatcher("/api/admin/**/search-paged", HttpMethod.POST.name())).hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: SEARCH BY NUMBER/NAME
                .requestMatchers(HttpMethod.POST, "/api/admin/candidate/findbynumberorname").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: SEARCH BY EMAIL
                .requestMatchers(HttpMethod.POST, "/api/admin/candidate/findbyemail").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: SEARCH BY PHONE
                .requestMatchers(HttpMethod.POST, "/api/admin/candidate/findbyphone").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: SEARCH BY EMAIL/PHONE/WHATSAPP
                .requestMatchers(HttpMethod.POST, "/api/admin/candidate/findbyemailphoneorwhatsapp").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: SEARCH BY EXTERNAL ID
                .requestMatchers(HttpMethod.POST, "/api/admin/candidate/findbyexternalid").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: FETCH CANDIDATES WITH CHATS
                .requestMatchers(HttpMethod.POST, "/api/admin/candidate/fetch-candidates-with-chat").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // CHAT - include USER but exclude READONLY
                .requestMatchers(HttpMethod.GET, "/api/admin/chat/**").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "USER", "READONLY")
                .requestMatchers(HttpMethod.POST, "/api/admin/chat/get-or-create").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "USER", "READONLY")
                .requestMatchers(HttpMethod.POST, "/api/admin/chat/**").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "USER")
                .requestMatchers(HttpMethod.POST, "/api/admin/chat/**").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "USER")
                .requestMatchers(HttpMethod.PUT, "/api/admin/chat/**").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "USER")
                .requestMatchers(HttpMethod.GET, "/api/admin/chat-post/**").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "USER", "READONLY")
                .requestMatchers(HttpMethod.POST, "/api/admin/chat-post/**").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "USER")

                // CHAT REACTIONS - include USER but exclude READONLY
                .requestMatchers(HttpMethod.POST, "/api/admin/reaction/**").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "USER")
                .requestMatchers(HttpMethod.PUT, "/api/admin/reaction/**").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "USER")

                // CHAT LINK PREVIEWS - permits all
                .requestMatchers(HttpMethod.POST, "/api/admin/link-preview/**").permitAll()

                /*
                 * LIST ENDPOINTS
                 */
                // POST: CREATE LIST
                .requestMatchers(HttpMethod.POST, "/api/admin/saved-list").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: CREATE LIST 2
                .requestMatchers(HttpMethod.POST, "/api/admin/saved-list-candidate").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: MERGE CANDIDATE INTO LIST (ADD BY NAME/NUMBER)
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-list-candidate/*/merge").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: REMOVE CANDIDATE FROM LIST
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-list-candidate/*/remove").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: SAVE SELECTION FROM LIST
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-list-candidate/*/save-selection").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE SF
                .requestMatchers(HttpMethod.PUT, "/api/admin/sf/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE SAVED LIST
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-list/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: SELECT COLUMNS SAVED LIST
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-list/displayed-fields/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE SAVED LIST DESCRIPTION
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-list/description/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE CONTEXT NOTES
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-list/context/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: COPY LIST
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-list/copy/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: EXPORT LIST
                .requestMatchers(HttpMethod.POST, "/api/admin/saved-list-candidate/*/export/csv").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: SEARCH LISTS BY IDS
                .requestMatchers(new AntPathRequestMatcher("/api/admin/saved-list/search-ids",HttpMethod.POST.name())).hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: VIEW TRANSLATIONS
                .requestMatchers(HttpMethod.POST, "/api/admin/translation/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // GET: MIGRATE TRANSLATIONS BETWEEN DIFFERENT AWS IDENTITIES
                .requestMatchers(HttpMethod.GET, "/api/admin/system/migrate-translations").hasRole("SYSTEMADMIN")

                /*
                 * CANDIDATE INTAKE ENDPOINTS
                 */
                // GET (EXC. READ ONLY)
                .requestMatchers(HttpMethod.GET, "/api/admin/candidate/*/intake").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT (EXC. READ ONLY)
                .requestMatchers(HttpMethod.PUT, "/api/admin/candidate/*/intake").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                /*
                 * CANDIDATE ENDPOINTS
                 */
                // DOWNLOAD CV (admins and read only)
                .requestMatchers(HttpMethod.POST, "/api/admin/candidate/*/cv.pdf").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "READONLY")

                /*
                 * READONLY can star and share things
                 */
                .requestMatchers(HttpMethod.PUT, "/api/admin/job/*/starred").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: SHARE LIST
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-list/shared-add/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UNSHARE LIST
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-list/shared-remove/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: SHARE SEARCH
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-search/shared-add/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UNSHARE SEARCH
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-search/shared-remove/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                /*
                 * JOB INTAKE ENDPOINTS
                 */
                // GET (EXC. READ ONLY)
                .requestMatchers(HttpMethod.GET, "/api/admin/job/*/intake").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT (EXC. READ ONLY)
                .requestMatchers(HttpMethod.PUT, "/api/admin/job/*/intake").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")


                // ALL OTHER END POINTS
                    // POST (EXC. READ ONLY)
                .requestMatchers(HttpMethod.POST, "/api/admin/**").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                    // PUT (EXC. READ ONLY)
                .requestMatchers(HttpMethod.PUT, "/api/admin/**").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                    // GET
                .requestMatchers(HttpMethod.GET, "/api/admin/**")
                .hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2

                .jwt(jwt -> jwt.jwtAuthenticationConverter(oAuth2UserAuthenticationConverter)));

        //Commented out below code because it was causing "Too many redirects" error as described below
        //https://stackoverflow.com/questions/42715718/aws-load-balancer-err-too-many-redirects/52598630#52598630
        //This code was really only needed when the Spring Server was NOT running behind the
        //Amazon Load Balancer. Then it was necessary for the server itself to require HTTPS
        //connections. However, the load balancer also forces HTTPS connections and it seems
        //that having both the load balancer and the server forcing https connections leads
        //to the "Too many redirects" error.
        //
        //See also this post which says that you can also avoid the problem by setting
        //server.forward-headers-strategy=NATIVE
        //https://stackoverflow.com/questions/26655875/spring-boot-redirect-http-to-https/58061590#58061590
        //However, we tested using that setting and the problem persists, so we need to pull out
        //the following code altogether
        // - John Cameron

//Force https in production ie when behind proxy - eg load balancer - but allow HTTP
//when running locally.
//See https://www.lenar.io/force-redirect-http-to-https-in-spring-boot/
//        .requiresChannel().requestMatchers( r -> r.getHeader("X-Forwarded-Proto") != null).requiresSecure()

        // Add the language filter
        http.addFilterAfter(languageFilter(), BearerTokenAuthenticationFilter.class);
        return http.build();
        // See https://docs.spring.io/spring-security/site/docs/5.2.0.RELEASE/reference/html/default-security-headers-2.html#webflux-headers-csp
        // And about allowing Google see https://developers.google.com/web/fundamentals/security/csp/
        // http.headers().contentSecurityPolicy("script-src 'self' https://apis.google.com");
    }

    //See https://docs.spring.io/spring-security/site/docs/current/reference/html5/#cors
    //and https://stackoverflow.com/a/65503296/929968
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        String urls = env.getProperty("tc.cors.urls");
        List<String> corsUrls = new ArrayList<>();
        if (StringUtils.isNotBlank(urls)) {
            Collections.addAll(corsUrls, urls.split(","));
        }
        if (corsUrls.isEmpty()) {
            LogBuilder.builder(log)
                .message("No CORS URLs specified. Defaulting to empty list, which may block cross-origin requests.")
                .logWarn();
        }
        configuration.setAllowedOriginPatterns(corsUrls);
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setMaxAge(3600L); // Cache preflight responses for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public LanguageFilter languageFilter() {
        return new LanguageFilter();
    }
}
