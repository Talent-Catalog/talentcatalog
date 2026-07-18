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

import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring Data JPA auditing configuration for candidate/domain entities.
 * <p>
 * {@link EnableJpaAuditing} turns on auditing support so entities annotated with
 * {@code @CreatedBy}, {@code @CreatedDate}, {@code @LastModifiedBy}, and
 * {@code @LastModifiedDate} can be auto-populated by {@code AuditingEntityListener}.
 * Classes that want to use auditing should be annotated with
 * <p>
 * {@code @EntityListeners(AuditingEntityListener.class)}.
 * <p>
 * The {@code auditorAwareRef} points to {@code auditorProvider}
 * ({@link org.tctalent.server.security.SpringSecurityAuditorAware}) to resolve the current
 * auditor user, and {@code dateTimeProviderRef} uses {@link #offsetDateTimeProvider()} to supply
 * {@link OffsetDateTime} timestamps for created/updated dates.
 *
 * @author sadatmalik
 */
@Configuration
@EnableJpaAuditing(
    auditorAwareRef = "auditorProvider",
    dateTimeProviderRef = "offsetDateTimeProvider"
)
public class JpaAuditingConfig {

    @Bean
    public DateTimeProvider offsetDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
