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

package org.tctalent.server.security;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.tctalent.server.model.db.User;

/**
 * Sets an authenticated user's selected language based on the X-Language
 * header in the HTTP request.
 * <p/>
 * This matches up with the language.interceptor.ts class on the
 * browser (Angular) side.
 */
public class LanguageFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LanguageFilter.class);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String selectedLanguage = request.getHeader("X-Language");
            if (StringUtils.isBlank(selectedLanguage)) {
                selectedLanguage = "en";
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof TcUserDetails) {
                    User user = ((TcUserDetails) principal).getUser();
                    user.setSelectedLanguage(selectedLanguage);
                }
            }

        } catch (Exception ex) {
            logger.error("Could not set user language in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

}
