
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

package org.tctalent.server.service.db.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.TermsId;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.model.db.TermsType;
import org.tctalent.server.service.db.TermsInfoService;

@Service
@Slf4j
@RequiredArgsConstructor
public class TermsInfoServiceImpl implements TermsInfoService {

    //TODO JC Get rid of repository and auto create TermsInfo at start up in constructor
    //TODO JC (or on demand) driven by skeleton entries for each terms - corresponding to
    //TODO JC a new enum with all terms. TermInfos are stored in a Map by enum value.
    //TODO JC Maybe only load html from resource on demand.

    @Override
    @NonNull
    public TermsInfo get(TermsId id) throws NoSuchObjectException {
        //TODO JC Implement get
        throw new UnsupportedOperationException("get not implemented");
    }

    @Override
    @NonNull
    public TermsInfo getCurrentByType(TermsType termsType) throws NoSuchObjectException {
        //TODO JC Implement getCurrentByType
        throw new UnsupportedOperationException("getCurrentByType not implemented");
    }
}
