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

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.tctalent.server.logging.LogBuilder;

/**
 * This defines some redirections and also where the Angular jars
 * (admin-portal.jar and candidate-portal.jar) are served from on the server.
 * <p/>
 * Note that in development (rather than when running in production) the Angular code is
 * not served up from this Spring server, but rather from "ng serve" servers - one for
 * candidate-portal and one for admin-portal.
 */
@Configuration
@Slf4j
public class WebConfiguration implements WebMvcConfigurer {

    /**
     * This defines the location of the compiled Angular for the candidate (front end,
     * candidate-portal) code and admin (back end, admin-portal) code.
     * <p/>
     * See for example Chapter 4 of https://www.baeldung.com/spring-mvc-static-resources
     * <p/>
     * Each compiled Angular project (ie each "portal") contains an index.html which loads all the
     * Javascript files and kicks off Angular. This method associates the various url's
     * - admin-portal, candidate-portal etc - with their corresponding directories.
     * The browser will load the index.html, start Angular and away we go.
     * @param registry Used to add resource handlers
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {

        UIBundle[] uiBundles = new UIBundle[]{
                new UIBundle("candidate-portal", "candidate-portal"),
                new UIBundle("admin-portal", "admin-portal"),
                new UIBundle("public-portal", "public-portal")
        };

        for (UIBundle uiBundle : uiBundles) {

            LogBuilder.builder(log)
                .action("WebConfiguration")
                .message("Adding UI Bundle: " + uiBundle.url + " => " + uiBundle.module)
                .logInfo();

            registry.addResourceHandler("/" + uiBundle.url + "/*.*")
                    .setCachePeriod(0)
                    .addResourceLocations("classpath:/ui-bundle/" + uiBundle.module + "/")
                    .resourceChain(true)
                    .addResolver(new PathResourceResolver() {
                        protected Resource getResource(
                            @NonNull String resourcePath, @NonNull Resource location) {
                            return new ClassPathResource("/ui-bundle/" + uiBundle.module + "/" + resourcePath);
                        }
                    });

            registry.addResourceHandler("/" + uiBundle.url + "/assets/**")
                    .setCachePeriod(0)
                    .addResourceLocations("classpath:/ui-bundle/" + uiBundle.module + "/assets/")
                    .resourceChain(true);

            registry.addResourceHandler("/" + uiBundle.url, "/" + uiBundle.url + "/", "/" + uiBundle.url + "/**")
                    .setCachePeriod(0)
                    .addResourceLocations("classpath:/ui-bundle/" + uiBundle.module + "/index.html")
                    .resourceChain(true)
                    .addResolver(new PathResourceResolver() {
                        protected Resource getResource(
                            @NonNull String resourcePath, @NonNull Resource location) {
                            return location.exists() && location.isReadable() ? location : null;
                        }
                    });
        }
    }


    public static final class UIBundle {
        public String url;
        public String module;

        public UIBundle(String url, String module) {
            this.url = url;
            this.module = module;
        }
    }
}
