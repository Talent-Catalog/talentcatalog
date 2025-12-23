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
 * For some DTO's you don't want to annotate every single field.
 * The default is to treat the field like a simple SqlColumn (ie no 1-1 or 1-many mapping)
 * using the name of the DTO field to deduce the name of the corresponding database field
 * (by converting from camel case to snake case if needed).
 *
 * @author John Cameron
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SqlDefaults {
    /**
     * If true, unannotated (non-static, non-transient) fields are treated as scalar DB columns
     * using camelToSnakeCase(fieldName).
     * If false, only fields explicitly annotated are mapped.
     */
    boolean mapUnannotatedColumns() default false;
}
