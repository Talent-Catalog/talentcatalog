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

package org.tctalent.server.security;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.User;

/**
 * Sets an authenticated user's selected language based on the X-Language
 * header in the HTTP request.
 * <p/>
 * This matches up with the language.interceptor.ts class on the
 * browser (Angular) side.
 */
@Slf4j
public class LanguageFilter extends OncePerRequestFilter {

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
            LogBuilder.builder(log)
                .action("doFilterInternal")
                .message("Could not set user language in security context")
                .logError(ex);
        }

        filterChain.doFilter(request, response);
    }

}
