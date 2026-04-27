/*
 * Copyright (c) 2025 Talent Catalog.
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
package org.tctalent.server.repository.db.read.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the database column that a DTO field is associated with (defaulting to the name of
 * the annotated field, converted to snake case.
 *
 * @author John Cameron
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SqlColumn {

    /**
     * Default name is taken from the annotated field name, converted to snake case if needed.
     */
    String name() default "";

    /**
     * Defaults to the name.
     */
    String jsonKey() default "";

    /**
     * <p>
     * Optional SQL transform applied to the resolved column expression.
     * </p>
     *
     * <p>
     * The transform must contain exactly one {@code %s}, which will be replaced
     * with the fully-qualified column reference (e.g. {@code ctask.uploadable_file_types}).
     * </p>
     *
     * <p>
     * Example:
     * </p>
     *
     * <pre>{@code
     * @SqlColumn(transform = "to_jsonb(string_to_array(%s, ','))")
     * private List<String> uploadableFileTypes;
     * }</pre>
     */
    String transform() default "";
}
