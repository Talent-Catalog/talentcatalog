/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.configuration;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.tbbtalent.server.security.CandidateUserDetailsService;
import org.tbbtalent.server.security.JwtAuthenticationEntryPoint;
import org.tbbtalent.server.security.JwtAuthenticationFilter;
import org.tbbtalent.server.security.LanguageFilter;
import org.tbbtalent.server.security.TbbAuthenticationProvider;

import static org.springframework.security.config.Customizer.withDefaults;

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
    private CandidateUserDetailsService userDetailsService;

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
                .antMatchers("/api/portal/user/reset-password-email").permitAll()
                .antMatchers("/api/portal/user/check-token").permitAll()
                .antMatchers("/api/portal/user/reset-password").permitAll()
                .antMatchers("/api/portal/language/system/**").permitAll()
                .antMatchers("/api/portal/language/translations/**").permitAll()
                .antMatchers("/api/portal/**").hasAnyRole("USER")
                .antMatchers("/api/admin/auth").permitAll()
                .antMatchers("/api/admin/auth/**").permitAll()

                // DELETE: DELETE SAVE SEARCHES
                .antMatchers(HttpMethod.DELETE, "/api/admin/saved-search/*").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // DELETE: DELETE LIST
                .antMatchers(HttpMethod.DELETE, "/api/admin/saved-list/*").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // DELETE: DELETE ATTACHMENT
                .antMatchers(HttpMethod.DELETE, "/api/admin/candidate-attachment/*").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED")

                // ADMIN ONLY RESTRICTIONS
                    // All OTHER DELETE end points
                .antMatchers(HttpMethod.DELETE, "/api/admin/**/*").hasRole("ADMIN")
                    // Migrate database
                .antMatchers("/api/admin/system/migrate").hasAnyRole("ADMIN")

                    // UPDATE/EDIT general settings
                .antMatchers(HttpMethod.PUT,
                        "/api/admin/user/*",
                        "/api/admin/country/*",
                        "/api/admin/nationality/*",
                        "/api/admin/language/*",
                        "/api/admin/language-level/*",
                        "/api/admin/occupation/*",
                        "/api/admin/education-level/*",
                        "/api/admin/education-major/*",
                        "/api/admin/translation/*",
                        "/api/admin/translation/file/*").hasRole("ADMIN")
                    // CREATE general settings
                .antMatchers(HttpMethod.POST,
                        "/api/admin/user",
                        "/api/admin/country",
                        "/api/admin/nationality",
                        "/api/admin/language",
                        "/api/admin/language-level",
                        "/api/admin/occupation",
                        "/api/admin/education-level",
                        "/api/admin/education-major").hasRole("ADMIN")

                // SEE CANDIDATE FILE ATTACHMENTS. ADMIN/SOURCE PARTNER ADMIN ALLOWED. READ ONLY has access BUT has the data restricted in the DTO based on role.
                .antMatchers(HttpMethod.POST, "/api/admin/candidate-attachment/search").hasAnyRole("ADMIN", "SOURCEPARTNERADMIN", "READONLY")

                // POST: REQUEST INFOGRAPHICS
                .antMatchers(HttpMethod.POST, "/api/admin/candidate/stat/all").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                /*
                 * SAVED SEARCH ENDPOINTS
                */
                // POST: CREATE SAVED SEARCHES
                .antMatchers(HttpMethod.POST, "/api/admin/saved-search").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE SAVED SEARCHES
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-search/*").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE CONTEXT NOTES
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-search/context/*").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // GET: LOAD SAVE SEARCHES
                .antMatchers(HttpMethod.GET, "/api/admin/saved-search/*/load").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: SELECT CANDIDATE SAVED SEARCHES
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-search/select-candidate/*").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: SAVE SELECTION SAVED SEARCHES
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-search/save-selection/*").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: CLEAR SELECTION SAVED SEARCHES
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-search/clear-selection/*").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: EXPORT SAVE SELECTION SAVED SEARCHES
                .antMatchers(HttpMethod.POST, "/api/admin/saved-search-candidate/*/export/csv").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                /*
                 * SEARCH ENDPOINTS
                 */
                // POST: ALL SEARCHES
                .antMatchers(HttpMethod.POST, "/api/admin/**/search").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: ALL PAGED SEARCHES
                .antMatchers(HttpMethod.POST, "/api/admin/**/search-paged").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: SEARCH BY NUMBER/NAME
                .antMatchers(HttpMethod.POST, "/api/admin/candidate/findbynumberorname").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: SEARCH BY EMAIL
                .antMatchers(HttpMethod.POST, "/api/admin/candidate/findbyemail").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: SEARCH BY PHONE
                .antMatchers(HttpMethod.POST, "/api/admin/candidate/findbyphone").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                /*
                 * LIST ENDPOINTS
                 */
                // POST: CREATE LIST
                .antMatchers(HttpMethod.POST, "/api/admin/saved-list").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: MERGE CANDIDATE INTO LIST (ADD BY NAME/NUMBER)
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-list-candidate/*/merge").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: REMOVE CANDIDATE FROM LIST
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-list-candidate/*/remove").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE SAVED LIST
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-list/*").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: UPDATE CONTEXT NOTES
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-list/context/*").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: ADD SHARED LIST
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-list/shared-add/*").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: REMOVE SHARED LIST
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-list/shared-remove/*").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT: COPY LIST
                .antMatchers(HttpMethod.PUT, "/api/admin/saved-list/copy/*").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: EXPORT LIST
                .antMatchers(HttpMethod.POST, "/api/admin/saved-list-candidate/*/export/csv").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // POST: VIEW TRANSLATIONS
                .antMatchers(HttpMethod.POST, "/api/admin/translation/*").hasAnyRole( "ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")


                /*
                 * INTAKE ENDPOINTS
                 */
                // PUT (EXC. READ ONLY)
                .antMatchers(HttpMethod.GET, "/api/admin/candidate/*/intake").hasAnyRole("ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                // PUT (EXC. READ ONLY)
                .antMatchers(HttpMethod.PUT, "/api/admin/candidate/*/intake").hasAnyRole("ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED")


                // ALL OTHER END POINTS
                    // POST (EXC. READ ONLY)
                .antMatchers(HttpMethod.POST, "/api/admin/**").hasAnyRole("ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED")

                    // PUT (EXC. READ ONLY)
                .antMatchers(HttpMethod.PUT, "/api/admin/**").hasAnyRole("ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED")

                    // GET
                .antMatchers(HttpMethod.GET, "/api/admin/**").hasAnyRole("ADMIN", "SOURCEPARTNERADMIN", "SEMILIMITED", "LIMITED", "READONLY")

                .and()
            .csrf().disable()
            .requiresChannel().requestMatchers( r -> r.getHeader("X-Forwarded-Proto") != null).requiresSecure()

        ;

        // Add the JWT security filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(languageFilter(), UsernamePasswordAuthenticationFilter.class);
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
        return new BCryptPasswordEncoder();
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

    private TbbAuthenticationProvider userAuthenticationProvider() {
        TbbAuthenticationProvider tbbAuthenticationProvider = new TbbAuthenticationProvider(userDetailsService);
        tbbAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return tbbAuthenticationProvider;
    }

}
