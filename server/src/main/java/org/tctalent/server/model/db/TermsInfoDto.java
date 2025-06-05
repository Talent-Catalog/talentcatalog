
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

package org.tctalent.server.model.db;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Defines the {@link TermsInfo} fields that get sent to browser.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class TermsInfoDto {

    /**
     * Unique id of terms
     */
    private TermsId id;

    /**
     * HTML content of the terms.
     */
    private String content;
}
