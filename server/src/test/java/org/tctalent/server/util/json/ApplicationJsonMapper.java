/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.util.json;

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.json.JsonMapper;

/**
 * Creates a JsonMapper configured by Spring Boot in the same way as the application's JsonMapper,
 * for unit tests which do not start a Spring context.
 */
public final class ApplicationJsonMapper {

    private ApplicationJsonMapper() {
    }

    public static JsonMapper create() {
        AtomicReference<JsonMapper> mapper = new AtomicReference<>();
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            //Same as application.yml
            .withPropertyValues("spring.jackson.use-jackson2-defaults=true")
            .run(context -> mapper.set(context.getBean(JsonMapper.class)));
        return mapper.get();
    }
}
