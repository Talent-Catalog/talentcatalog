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

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Used to communicate relevant partner info to the public API server.
 *
 * @author John Cameron
 */
@Getter
@Setter
@AllArgsConstructor
public class PublicApiPartnerDto {

    /**
     * Name of partner
     */
    private String name;

    /**
     * Id of partner on TC server
     */
    private long partnerId;

    /**
     * Set of public api authorities
     */
    private Set<PublicApiAuthority> publicApiAuthorities;

    /**
     * Hash of Public API Key
     */
    private String publicApiKeyHash;
}
