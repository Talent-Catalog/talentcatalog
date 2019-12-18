package org.tbbtalent.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.nio.charset.StandardCharsets;

import static org.thymeleaf.templatemode.TemplateMode.HTML;

@Configuration
public class PdfConfiguration {

    @Bean
    public TemplateEngine pdfTemplateEngine() {
        final TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addTemplateResolver(pdfTemplateResolver());
        templateEngine.addDialect(new Java8TimeDialect());
        return templateEngine;
    }

    private ITemplateResolver pdfTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("pdf/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(HTML);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateResolver.setCacheable(false);
        return templateResolver;
    }
}
