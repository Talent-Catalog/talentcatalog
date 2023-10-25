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
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.tctalent.server.security.JwtAuthenticationEntryPoint;
import org.tctalent.server.security.JwtAuthenticationFilter;
import org.tctalent.server.security.LanguageFilter;
import org.tctalent.server.security.TcAuthenticationProvider;
import org.tctalent.server.security.TcPasswordEncoder;
import org.tctalent.server.security.TcUserDetailsService;
import org.tctalent.server.security.JwtTokenProvider;

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
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private Environment env;

    @Autowired
    private TcUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            //Default is to use a Bean called corsConfigurationSource - defined
            //below.
            .cors(withDefaults())
            .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .authorizeRequests()
                .antMatchers("/backend/jobseeker").permitAll()
                .antMatchers("/api/portal/auth").permitAll()
                .antMatchers("/api/portal/auth/**").permitAll()
                .antMatchers("/api/portal/branding").permitAll()
                .antMatchers("/api/portal/user/reset-password-email").permitAll()
                .antMatchers("/api/portal/user/check-token").permitAll()
                .antMatchers("/api/portal/user/reset-password").permitAll()
                .antMatchers("/api/portal/language/system/**").permitAll()
                .antMatchers("/api/portal/language/translations/**").permitAll()
                .antMatchers("/api/portal/**").hasAnyRole("USER")
                .antMatchers("/api/admin/auth").permitAll()
                .antMatchers("/api/admin/auth/**").permitAll()
                .antMatchers("/api/admin/branding").permitAll()
                .antMatchers("/").permitAll()
                .antMatchers("/published/**").permitAll()

                // DELETE: DELETE SAVE SEARCHES
                .antMatchers(HttpMethod.DELETE, "/api/admin/saved-search/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // DELETE: DELETE LIST
                .antMatchers(HttpMethod.DELETE, "/api/admin/saved-list/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // DELETE: DELETE ATTACHMENT
                .antMatchers(HttpMethod.DELETE, "/api/admin/candidate-attachment/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                // DELETE: DELETE EDUCATION
                .antMatchers(HttpMethod.DELETE, "/api/admin/candidate-education/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                // DELETE: DELETE CANDIDATE EXAM (INTAKE INTERVIEW)
                .antMatchers(HttpMethod.DELETE, "/api/admin/candidate-exam/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                // DELETE: DELETE CANDIDATE CITIZENSHIP (INTAKE INTERVIEW)
                .antMatchers(HttpMethod.DELETE, "/api/admin/candidate-citizenship/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                // DELETE: DELETE CANDIDATE DEPENDANT (INTAKE INTERVIEW)
                .antMatchers(HttpMethod.DELETE, "/api/admin/candidate-dependant/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                // DELETE: DELETE CANDIDATE JOB EXPERIENCE
                .antMatchers(HttpMethod.DELETE, "/api/admin/candidate-job-experience/*").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN")

                // DELETE: DELETE CANDIDATE LANGUAGE
                .antMatchers(HttpMethod.DELETE, "/api/admin/candidate-language/*").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN")

                // DELETE: DELETE USER (ADDED AUTHORISATION ON SERVER FOR SOURCE PARTNER ADMINS)
                .antMatchers(HttpMethod.DELETE, "/api/admin/user/*").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN")

                // ADMIN ONLY RESTRICTIONS
                    // All OTHER DELETE end points
                .antMatchers(HttpMethod.DELETE, "/api/admin/**/*").hasAnyRole("SYSTEMADMIN", "ADMIN")
                    // Migrate database
                .antMatchers("/api/admin/system/migrate").hasAnyRole("SYSTEMADMIN", "ADMIN")

                    // UPDATE/EDIT SETTINGS
                .antMatchers(HttpMethod.PUT,
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
                .antMatchers(HttpMethod.POST,
                        "/api/admin/country",
                        "/api/admin/nationality",
                        "/api/admin/language",
                        "/api/admin/language-level",
                        "/api/admin/occupation",
                        "/api/admin/education-level",
                        "/api/admin/education-major").hasAnyRole("SYSTEMADMIN", "ADMIN")

                // CREATE/UPDATE USERS
                .antMatchers(HttpMethod.PUT, "/api/admin/user/*").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "READONLY")
                .antMatchers(HttpMethod.POST, "/api/admin/user/*").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "READONLY")

                // SEE CANDIDATE FILE ATTACHMENTS. ADMIN/SOURCE PARTNER ADMIN ALLOWED. READ ONLY has access BUT has the data restricted in the DTO based on role.
                .antMatchers(HttpMethod.POST, "/api/admin/candidate-attachment/search").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "READONLY")

                // POST: REQUEST INFOGRAPHICS
                .antMatchers(HttpMethod.POST, "/api/admin/candidate/stat/all").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                /*
                 * SAVED SEARCH ENDPOINTS
                */
                // POST: CREATE SAVED SEARCHES
                .antMatchers(HttpMethod.POST, "/api/admin/saved-search").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE SAVED SEARCHES
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-search/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE CONTEXT NOTES
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-search/context/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // GET: LOAD SAVE SEARCHES
                .antMatchers(HttpMethod.GET, "/api/admin/saved-search/*/load").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: SELECT CANDIDATE SAVED SEARCHES
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-search/select-candidate/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // GET: SELECTION COUNT SAVED SEARCHES
                .antMatchers(HttpMethod.GET, "/api/admin/saved-search/get-selection-count/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: SAVE SELECTION SAVED SEARCHES
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-search/save-selection/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE STATUSES
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-search/update-selected-statuses/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                // PUT: CLEAR SELECTION SAVED SEARCHES
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-search/clear-selection/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: EXPORT SAVE SELECTION SAVED SEARCHES
                .antMatchers(HttpMethod.POST, "/api/admin/saved-search-candidate/*/export/csv").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                /*
                 * SEARCH ENDPOINTS
                 */
                // POST: ALL SEARCHES
                .antMatchers(HttpMethod.POST, "/api/admin/**/search").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: ALL PAGED SEARCHES
                .antMatchers(HttpMethod.POST, "/api/admin/**/search-paged").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: SEARCH BY NUMBER/NAME
                .antMatchers(HttpMethod.POST, "/api/admin/candidate/findbynumberorname").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: SEARCH BY EMAIL
                .antMatchers(HttpMethod.POST, "/api/admin/candidate/findbyemail").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: SEARCH BY PHONE
                .antMatchers(HttpMethod.POST, "/api/admin/candidate/findbyphone").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                /*
                 * LIST ENDPOINTS
                 */
                // POST: CREATE LIST
                .antMatchers(HttpMethod.POST, "/api/admin/saved-list").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: MERGE CANDIDATE INTO LIST (ADD BY NAME/NUMBER)
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-list-candidate/*/merge").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: REMOVE CANDIDATE FROM LIST
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-list-candidate/*/remove").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE SF
                .antMatchers(HttpMethod.PUT, "/api/admin/sf/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE SAVED LIST
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-list/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE CONTEXT NOTES
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-list/context/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: ADD SHARED LIST
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-list/shared-add/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: REMOVE SHARED LIST
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-list/shared-remove/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: COPY LIST
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-list/copy/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: EXPORT LIST
                .antMatchers(HttpMethod.POST, "/api/admin/saved-list-candidate/*/export/csv").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: VIEW TRANSLATIONS
                .antMatchers(HttpMethod.POST, "/api/admin/translation/*").hasAnyRole( "SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")


                /*
                 * CANDIDATE INTAKE ENDPOINTS
                 */
                // GET (EXC. READ ONLY)
                .antMatchers(HttpMethod.GET, "/api/admin/candidate/*/intake").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT (EXC. READ ONLY)
                .antMatchers(HttpMethod.PUT, "/api/admin/candidate/*/intake").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                /*
                 * JOB INTAKE ENDPOINTS
                 */
                // GET (EXC. READ ONLY)
                .antMatchers(HttpMethod.GET, "/api/admin/job/*/intake").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT (EXC. READ ONLY)
                .antMatchers(HttpMethod.PUT, "/api/admin/job/*/intake").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")


                // ALL OTHER END POINTS
                    // POST (EXC. READ ONLY)
                .antMatchers(HttpMethod.POST, "/api/admin/**").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                    // PUT (EXC. READ ONLY)
                .antMatchers(HttpMethod.PUT, "/api/admin/**").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED")

                    // GET
                .antMatchers(HttpMethod.GET, "/api/admin/**").hasAnyRole("SYSTEMADMIN", "ADMIN", "PARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                .and()
            .csrf().disable()


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
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(userAuthenticationProvider());
    }

    private TcAuthenticationProvider userAuthenticationProvider() {
        TcAuthenticationProvider tcAuthenticationProvider = new TcAuthenticationProvider(userDetailsService);
        tcAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return tcAuthenticationProvider;
    }

}
