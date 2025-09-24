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

import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Based on https://springframework.guru/how-to-configure-multiple-data-sources-in-a-spring-boot-application/
 *
 * @author John Cameron
 */
@Configuration
@EnableJpaRepositories(
        basePackages = {
            "org.tctalent.server.repository.db",
            "org.tctalent.server.casi.domain.persistence"
        },
        entityManagerFactoryRef = "dbEntityManagerFactory",
        transactionManagerRef= "dbTransactionManager"
)
public class DatabaseConfiguration {
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dbDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.configuration")
    public DataSource dbDataSource() {
        return dbDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Primary
    @Bean(name = "dbEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean
    dbEntityManagerFactory(EntityManagerFactoryBuilder builder) {

        //Note that this is necessary because with multiple data sources the
        //default naming strategies are not picked up.
        //If you don't do this database fields like first_name no longer map to
        //entity names like firstName.
        //See https://stackoverflow.com/questions/40509395/cant-set-jpa-naming-strategy-after-configuring-multiple-data-sources-spring-1
        Map<String, String> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.physical_naming_strategy", CamelCaseToUnderscoresNamingStrategy.class.getName());
        jpaProperties.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());

        return builder
                .dataSource(dbDataSource())
                .packages(
                        "org.tctalent.server.service.db",
                        "org.tctalent.server.model.db",
                        "org.tctalent.server.candidateservices.domain.persistence"
                )
                .properties(jpaProperties)
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager dbTransactionManager(
            final @Qualifier("dbEntityManagerFactory")
                    LocalContainerEntityManagerFactoryBean
                    dbEntityManagerFactory) {
        return new JpaTransactionManager(dbEntityManagerFactory.getObject());
    }

}
