/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.configuration.properties;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for candidate JSON cache.
 * <p>
 * Properties are prefixed with {@code tc.cache.candidate}.
 *
 * @author sadatmalik
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "tc.cache.candidate")
public class CandidateCacheProperties {

    /**
     * TTL applied to candidate JSON cache entries in Redis.
     * A non-positive value (e.g. 0) disables expiry.
     */
    private Duration ttl = Duration.ofDays(7);
}
