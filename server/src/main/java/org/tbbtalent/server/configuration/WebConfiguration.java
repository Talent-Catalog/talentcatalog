/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * This defines some redirections and also where the Angular jars
 * (admin-portal.jar and candidate-portal.jar) are served from on the server.
 * <p/>
 * Note that in development (rather than when running in production) the Angular code is
 * not served up from this Spring server, but rather from "ng serve" servers - one for
 * candidate-portal and one for admin-portal.
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfiguration.class);

    /**
     * We need to redirect for two reasons:
     * <ul>
     *   <li>
     *     When people type tbbtalent.org in a browser we want to assume that they are a
     *     candidate (front end user) rather than a TBB admin (back end user).
     *     This means we want them to redirect to tbbtalent.org/candidate-portal/
     *     (rather than tbbtalent.org/admin-portal/)
     *   </li>
     *   <li>
     *     Rather than going direct to the tbbtalent.org website, we would prefer that they go
     *     to a nicely formatted landing page, which are much easier to create and format on our
     *     talentbeyondboundaries.org website (built using Squarespace). So we redirect to
     *     appropriate talentbeyondboundaries.org pages.
     *     Those pages will eventually lead the user to a tbbtalent.org/candidate-portal/ link.
     *   </li>
     * </ul>
     * @param registry Used to add some redirect controllers.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController(
            "/us", "https://www.talentbeyondboundaries.org/talentcatalog/us-afghan");
    }

    /**
     * This defines the location of the compiled Angular for the candidate (front end,
     * candidate-portal) code and admin (back end, admin-portal) code.
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

            log.info("Adding UI Bundle: " + uiBundle.url + " => " + uiBundle.module);

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
