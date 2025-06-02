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

package org.tctalent.server.service.db;

import org.springframework.lang.NonNull;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.model.db.TermsType;

public interface TermsInfoService {

    /**
     * Get the TermsInfo with the given id.
     * @param termsInfoId ID of TermsInfo to get
     * @return TermsInfo
     * @throws NoSuchObjectException if there is no TermsInfo with this id.
     */
    @NonNull
    TermsInfo get(long termsInfoId) throws NoSuchObjectException;

    /**
     * Get the TermsInfo of the most recent terms of the given type
     * @param termsType Type of terms
     * @return TermsInfo
     * @throws NoSuchObjectException if there are no terms of that type
     */
    @NonNull
    TermsInfo getCurrentByType(TermsType termsType) throws NoSuchObjectException;
}
