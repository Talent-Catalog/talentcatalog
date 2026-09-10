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

package org.tctalent.server.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

/**
 * Configuration class for setting up the Thymeleaf template engine for rendering terms content
 * with per-counterparty variable substitution.
 *
 * @author sadatmalik
 */
@Configuration
public class TermsConfiguration {

    /**
     * Thymeleaf engine for rendering terms content with per-counterparty variable substitution.
     *
     * We use StringTemplateResolver here (instead of ClassLoaderTemplateResolver used by
     * EmailConfiguration/PdfConfiguration) because TermsInfo content is already loaded as an in-memory
     * String by TermsInfoServiceImpl. This keeps the implementation compatible with future
     * TermsInfo table storage.
     */
    @Bean
    public TemplateEngine termsTemplateEngine() {
        final SpringTemplateEngine engine = new SpringTemplateEngine();
        final StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
        return engine;
    }
}
