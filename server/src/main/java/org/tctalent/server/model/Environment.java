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

package org.tctalent.server.model;

/*
 * Sets the options for checking which server environment is running - inject the 'environment' variable from application.yml using Spring's @Value annotation (https://www.baeldung.com/spring-boot-properties-env-variables#1-inject-the-value-with-value)
 * The resulting string can then be used for comparison with these enum elements by using the Enum.name() method
 */

public enum Environment {
    local, staging, prod
}
