package org.tbbtalent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfiguration.class);

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/candidate-portal/");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        UIBundle[] uiBundles = new UIBundle[]{
                new UIBundle("candidate-portal", "candidate-portal"),
                new UIBundle("admin-portal", "admin-portal")
        };


        for (UIBundle uiBundle : uiBundles) {

            log.info("Adding UI Bundle: " + uiBundle.url + " => " + uiBundle.module);

            registry.addResourceHandler("/" + uiBundle.url + "/*.*")
                    .setCachePeriod(0)
                    .addResourceLocations("classpath:/ui-bundle/" + uiBundle.module + "/")
                    .resourceChain(true)
                    .addResolver(new PathResourceResolver() {
                        protected Resource getResource(String resourcePath, Resource location) throws IOException {
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
                        protected Resource getResource(String resourcePath, Resource location) throws IOException {
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
