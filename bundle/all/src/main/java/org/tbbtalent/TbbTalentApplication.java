package org.tbbtalent;

import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.RedirectViewControllerRegistration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class TbbTalentApplication extends WebMvcConfigurerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(TbbTalentApplication.class, args);
    }

//    @Bean
//    @ConditionalOnProperty(name="flyway.repair", havingValue="true")
//    public FlywayMigrationStrategy fixFlyway() {
//        return new FlywayMigrationStrategy() {
//            @Override
//            public void migrate(Flyway flyway) {
//                try {
//                    System.out.println("************* Starting flyway repair ***********************");
//                    flyway.repair();
//                    System.out.println("************* Finished flyway repair ***********************");
//                    flyway.migrate();
//                } catch (Exception e) {
//                    System.out.println("ERROR: unable to repair flyway");
//                    e.printStackTrace();
//                }
//            }
//        };
//    }

    @Override
    public void addViewControllers (ViewControllerRegistry registry) {
        RedirectViewControllerRegistration r =
                registry.addRedirectViewController("/", "/candidate");
    }
   
}
