/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.tctalent.server.security.JwtAuthenticationEntryPoint;
import org.tctalent.server.security.JwtAuthenticationFilter;
import org.tctalent.server.security.JwtTokenProvider;
import org.tctalent.server.security.LanguageFilter;
import org.tctalent.server.security.TcAuthenticationProvider;
import org.tctalent.server.security.TcPasswordEncoder;
import org.tctalent.server.security.TcUserDetailsService;

/**
 * Talent Catalog security configuration.
 * <p/>
 * See https://docs.spring.io/spring-security/site/docs/3.2.0.RC2/reference/htmlsingle/#jc
 * also
 * https://www.marcobehler.com/guides/spring-security
 * <p/>
 * Summary of TBB Talent Catalog security:
 *
 * <ul>
 *     <li>
 *         We manage our own users and passwords, stored in the users table of the database
 *     </li>
 *     <li>
 *         At login we issue JSON Web Tokens (JWTs) which appear on each HTTP request in the
 *         Authorization header as a Bearer token.
 *         See https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Authorization
 *         and https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication#authentication_schemes.
 *         This is OAUTH 2.0 with us acting as the Authorization server -
 *         see https://www.marcobehler.com/guides/spring-security-oauth2
 *     </li>
 * </ul>
 * The following classes support the above:
 * <ul>
 *     <li>
 *         {@link JwtTokenProvider} generates JWT tokens
 *     </li>
 *     <li>
 *         {@link JwtAuthenticationFilter} is a filter which checks incoming JWT tokens, validating
 *         them.
 *     </li>
 *     <li>
 *         {@link TcUserDetailsService} implements UserDetailsService providing access to the user
 *         table in our database
 *     </li>
 *     <li>
 *         {@link TcAuthenticationProvider} implements AuthenticationProvider passing in our
 *         wired in instance of the above TcUserDetailsService, and the PasswordEncoder defined
 *         below in {@link #passwordEncoder()}.
 *     </li>
 * </ul>
 */
@Configuration
@EnableWebSecurity(debug = false)
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = false)
public class SecurityConfiguration {

    @Autowired
    private Environment env;

    @Autowired
    private TcUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            //Default is to use a Bean called corsConfigurationSource - defined
            //below.
            .cors(withDefaults())
            .csrf(CsrfConfigurer::disable)
            .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .authorizeRequests()
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
                .requestMatchers("/api/admin/user/reset-password-email").permitAll()
                .requestMatchers("/api/admin/user/check-token").permitAll()
                .requestMatchers("/api/admin/user/reset-password").permitAll()
                .requestMatchers("/").permitAll()
                .requestMatchers("/published/**").permitAll()

                .requestMatchers("/websocket","/websocket/**").permitAll()
                .requestMatchers("/app/**","/app/**/**").permitAll()
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

                // ADMIN ONLY RESTRICTIONS
                    // All OTHER DELETE end points
                .requestMatchers(HttpMethod.DELETE, "/api/admin/**/*").hasAnyRole("SYSTEMADMIN", "ADMIN")
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

                /*
                 * SAVED SEARCH ENDPOINTS
                */
                // POST: CREATE SAVED SEARCHES
                .requestMatchers(HttpMethod.POST, "/api/admin/saved-search").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE SAVED SEARCHES
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-search/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

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

                // POST: EXPORT SAVE SELECTION SAVED SEARCHES
                .requestMatchers(HttpMethod.POST, "/api/admin/saved-search-candidate/*/export/csv").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                /*
                 * SEARCH ENDPOINTS
                 */
                // POST: ALL SEARCHES
                .requestMatchers(HttpMethod.POST, getApiAdminSearchEndPoints()).hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: ALL PAGED SEARCHES
                .requestMatchers(HttpMethod.POST, getAdminApiSearchPagedEndPoints()).hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: SEARCH BY NUMBER/NAME
                .requestMatchers(HttpMethod.POST, "/api/admin/candidate/findbynumberorname").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: SEARCH BY EMAIL
                .requestMatchers(HttpMethod.POST, "/api/admin/candidate/findbyemail").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: SEARCH BY PHONE
                .requestMatchers(HttpMethod.POST, "/api/admin/candidate/findbyphone").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // CHAT - include USER but exclude READONLY
                .requestMatchers(HttpMethod.GET, "/api/admin/chat/**").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "USER")
                .requestMatchers(HttpMethod.POST, "/api/admin/chat/**").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "USER")
                .requestMatchers(HttpMethod.PUT, "/api/admin/chat/**").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "USER")
                .requestMatchers(HttpMethod.GET, "/api/admin/chat-post/**").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "USER", "READONLY")
                .requestMatchers(HttpMethod.POST, "/api/admin/chat-post/**").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "USER")

                // CHAT REACTIONS - include USER but exclude READONLY
                .requestMatchers(HttpMethod.POST, "/api/admin/reaction/**").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "USER")
                .requestMatchers(HttpMethod.PUT, "/api/admin/reaction/**").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "USER")

                /*
                 * LIST ENDPOINTS
                 */
                // POST: CREATE LIST
                .requestMatchers(HttpMethod.POST, "/api/admin/saved-list").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: MERGE CANDIDATE INTO LIST (ADD BY NAME/NUMBER)
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-list-candidate/*/merge").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: REMOVE CANDIDATE FROM LIST
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-list-candidate/*/remove").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE SF
                .requestMatchers(HttpMethod.PUT, "/api/admin/sf/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE SAVED LIST
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-list/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE CONTEXT NOTES
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-list/context/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: ADD SHARED LIST
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-list/shared-add/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: REMOVE SHARED LIST
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-list/shared-remove/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: COPY LIST
                .requestMatchers(HttpMethod.PUT, "/api/admin/saved-list/copy/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: EXPORT LIST
                .requestMatchers(HttpMethod.POST, "/api/admin/saved-list-candidate/*/export/csv").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: VIEW TRANSLATIONS
                .requestMatchers(HttpMethod.POST, "/api/admin/translation/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")


                /*
                 * CANDIDATE INTAKE ENDPOINTS
                 */
                // GET (EXC. READ ONLY)
                .requestMatchers(HttpMethod.GET, "/api/admin/candidate/*/intake").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT (EXC. READ ONLY)
                .requestMatchers(HttpMethod.PUT, "/api/admin/candidate/*/intake").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

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
                .requestMatchers(HttpMethod.GET, "/api/admin/**").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                .and()

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

        ;

        // Add the JWT security filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(languageFilter(), UsernamePasswordAuthenticationFilter.class);

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
        String urls = env.getProperty("tbb.cors.urls");
        List<String> corsUrls = new ArrayList<>();
        if (StringUtils.isNotBlank(urls)) {
            Collections.addAll(corsUrls, urls.split(","));
        }
        configuration.setAllowedOrigins(corsUrls);
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public LanguageFilter languageFilter() {
        return new LanguageFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new TcPasswordEncoder();
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    private TcAuthenticationProvider userAuthenticationProvider() {
        TcAuthenticationProvider tcAuthenticationProvider = new TcAuthenticationProvider(userDetailsService);
        tcAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return tcAuthenticationProvider;
    }

    /**
     * This method replaces existing AntMatcher which is non-default. (any directory wildcard)
     * .requestMatchers(HttpMethod .POST, /api/admin/* * /search)
     *
     * @return list of urls.
     */
    private String[] getApiAdminSearchEndPoints() {
        return new String[] {
            "/api/admin/partner/search",
            "/api/admin/candidate-vis-job/{id}/search",
            "/api/admin/candidate-skill/search",
            "/api/admin/saved-list/search",
            "/api/admin/chat/search",
            "/api/admin/country/search",
            "/api/admin/help-linked/search",
            "/api/admin/candidate-exam/{id}/search",
            "/api/admin/industry/search",
            "/api/admin/candidate-job-experience/search",
            "/api/admin/education-major/search",
            "/api/admin/task-assignment/search",
            "/api/admin/opp/search",
            "/api/admin/candidate-dependent/{id}/search",
            "/api/admin/task/search",
            "/api/admin/language/search",
            "/api/admin/job/search",
            "/api/admin/candidate-visa-check/{id}/search",
            "/api/admin/chat-post/{id}/search",
            "/api/admin/candidate-destination/{id}/search",
            "/api/admin/candidate-citizenship/{id}/search",
            "/api/admin/candidate-note/search",
            "/api/admin/candidate-attachment/search",
            "/api/admin/occupation/search",
            "/api/admin/saved-list-candidate/{id}/search",
            "/api/admin/education-level/search",
            "/api/admin/candidate/search",
            "/api/admin/language-level/search"
        };
    }

    // .requestMatchers(HttpMethod.POST, "/api/admin/**/search-paged")
    private String[] getAdminApiSearchPagedEndPoints() {
        return new String[] {
            "/api/admin/saved-list/search-paged",
            "/api/admin/job/search-paged",
            "/api/admin/task/search-paged",
            "/api/admin/opp/search-paged",
            "/api/admin/help-link/search-paged",
            "/api/admin/country/search-paged",
            "/api/admin/task-assignment/search-paged",
            "/api/admin/job/search-paged",
            "/api/admin/chat/search-paged",
            "/api/admin/partner/search-paged",
            "/api/admin/chat-post/{id}/search-paged",
            "/api/admin/candidate-vis-job/{id}/search-paged",
            "/api/admin/candidate-exam/{id}/search-paged",
            "/api/admin/candidate-visa-check/{id}/search-paged",
            "/api/admin/candidate-dependent/{id}/search-paged",
            "/api/admin/candidate-destination/{id}/search-paged",
            "/api/admin/candidate-citizenship/{id}/search-paged",
            "/api/admin/candidate-attachment/search-paged",
            "/api/admin/saved-list-candidate/{id}/search-paged",
            "/api/admin/candidate-saved-list/{id}/search-paged",
            "/api/admin/user/search-paged"
        };
    }
}