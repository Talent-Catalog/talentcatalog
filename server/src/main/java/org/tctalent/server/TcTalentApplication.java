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

package org.tctalent.server;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring start up.
 * <p/>
 * See also SystemAdminConfiguration which is run at startup.
 * <p/>
 * See also classes implementing InitializingBean. They are automatically run at startup.
 * <p/>
 * See also FlywayMigrationInitializer
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
@EnableAsync
public class TcTalentApplication {

    public static void main(String[] args) {
        SpringApplication.run(TcTalentApplication.class, args);
    }

    /**
     * This is only created if flyway.repair = true in the application.yml config.
     * <p/>
     * It runs a Flyway repair before doing the Flyway processing - ie calling "migrate".
     * <p/>
     * {@link org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer} will
     * run the migrate method of a FlywayMigrationStrategy if one exists, otherwise it will
     * just run Flyway.migrate.
     * @return A FlywayMigrationStrategy bean
     */
   @Bean
   @ConditionalOnProperty(name="flyway.repair", havingValue="true")
   public FlywayMigrationStrategy fixFlyway() {
       return new FlywayMigrationStrategy() {
           @Override
           public void migrate(Flyway flyway) {
               try {
                   System.out.println("************* Starting flyway repair ***********************");
                   flyway.repair();
                   System.out.println("************* Finished flyway repair ***********************");
                   flyway.migrate();
               } catch (Exception e) {
                   System.out.println("ERROR: unable to repair flyway");
                   e.printStackTrace();
               }
           }
       };
   }

}
