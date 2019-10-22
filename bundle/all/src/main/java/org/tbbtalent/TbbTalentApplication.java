package org.tbbtalent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.RedirectViewControllerRegistration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class TbbTalentApplication extends WebMvcConfigurerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(TbbTalentApplication.class, args);
    }

   @Override
    public void addViewControllers (ViewControllerRegistry registry) {
        RedirectViewControllerRegistration r =
                registry.addRedirectViewController("/", "/candidate-portal");
    }
   
}
