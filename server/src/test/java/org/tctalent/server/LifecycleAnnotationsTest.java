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

package org.tctalent.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;

/**
 * Spring Framework 6 still calls methods annotated with the legacy javax.annotation lifecycle
 * annotations, but Spring Framework 7 (Spring Boot 4) silently ignores them - so a bean's
 * initialization (or clean up) would just stop happening.
 * <p/>
 * This test fails if any of our classes use them. Use the jakarta.annotation equivalents.
 */
class LifecycleAnnotationsTest {

    private static final List<String> LEGACY_LIFECYCLE_ANNOTATIONS = List.of(
        "javax.annotation.PostConstruct",
        "javax.annotation.PreDestroy");

    @Test
    @DisplayName("no methods use the javax.annotation lifecycle annotations")
    void noLegacyJavaxLifecycleAnnotations() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        //Reads class files without loading the classes
        MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resolver);

        List<String> offenders = new ArrayList<>();
        for (Resource resource : resolver.getResources("classpath*:org/tctalent/server/**/*.class")) {
            AnnotationMetadata metadata =
                readerFactory.getMetadataReader(resource).getAnnotationMetadata();
            for (String annotation : LEGACY_LIFECYCLE_ANNOTATIONS) {
                for (MethodMetadata method : metadata.getAnnotatedMethods(annotation)) {
                    offenders.add(metadata.getClassName() + "." + method.getMethodName()
                        + " is annotated with " + annotation);
                }
            }
        }

        assertThat(offenders).isEmpty();
    }
}
