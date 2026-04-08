/*
 * Copyright (c) 2026 Talent Catalog.
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

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tctalent.server.configuration.properties.TcFlywayProperties;

/**
 * Configure TC's FlywayMigrationStrategy to optionally run a repair
 *
 * @author John Cameron
 */
@Configuration
@RequiredArgsConstructor
public class TcFlywayConfig {
    private final TcFlywayProperties tcFlywayProperties;
    
    /**
     * Creates a Flyway strategy which optionally runs a Flyway repair before doing the normal
     * Flyway processing - ie calling "migrate".
     * @return The FlywayMigrationStrategy bean
     */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        System.out.println(
            "************* Creating Flyway Migration Strategy ***********************");
        return new FlywayMigrationStrategy() {
            @Override
            public void migrate(Flyway flyway) {
                if (tcFlywayProperties.isRepair()) {
                    System.out.println(
                        "************* Starting flyway repair ***********************");
                    flyway.repair();
                    System.out.println(
                        "************* Finished flyway repair ***********************");
                }
                flyway.migrate();
            }
        };
    }
    
}
