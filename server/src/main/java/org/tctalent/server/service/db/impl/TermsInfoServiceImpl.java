
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

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.model.db.TermsType;
import org.tctalent.server.service.db.TermsInfoService;

@Service
@Slf4j
public class TermsInfoServiceImpl implements TermsInfoService {

    //TODO JC Get rid of TermsID

    //TODO JC Auto create TermsInfo at start up in constructor
    //TODO JC (or on demand) driven by skeleton entries for each terms - corresponding to
    //TODO JC a new enum with all terms. TermInfos are stored in a Map by enum value.
    //TODO JC Maybe only load html from resource on demand.

    private Map<String, TermsInfo> termsInfoMap;

    public TermsInfoServiceImpl() {
        TermsInfo[] termsInfos = new TermsInfo[] {
            new TermsInfo(
                "CandidatePrivacyPolicyV1",
                TermsType.CANDIDATE_PRIVACY_POLICY,
                LocalDate.of(2025, Month.JUNE, 5)
            ),
            new TermsInfo(
                "CandidatePrivacyPolicyV2",
                TermsType.CANDIDATE_PRIVACY_POLICY,
                LocalDate.of(2025, Month.JUNE, 10)
            )
        };

        initialize(termsInfos);
    }

    //TODO JC Use String as id instead of enum.
    //id's extracted from TermsInfo's
    //id can be name of resource file - or that could be default (allowing renaming of resource files).
    //id's probably should be immutable because changing id needs to change all references to it.

    /**
     * Package private for testing purposes - normally only called once from constructor with
     * predefined TermsInfo[].
     * <p/>
     * This allows it to be set up with different TermsInfo for testing purposes.
     */
    void initialize(TermsInfo[] termsInfos) {
        termsInfoMap = new HashMap<>();
        for (TermsInfo termsInfo : termsInfos) {
            addTermsInfo(termsInfo);
        }
    }

    private void addTermsInfo(@NonNull TermsInfo termsInfo) {
        TermsInfo previous = termsInfoMap.put(termsInfo.getId(), termsInfo);
        if (previous != null) {
            throw new RuntimeException("Duplicate terms info id: " + termsInfo.getId());
        }
    }

    @Override
    @NonNull
    public TermsInfo get(String id) throws NoSuchObjectException {
        //TODO JC This could trigger reading of resource
        return termsInfoMap.get(id);
    }

    @Override
    @NonNull
    public TermsInfo getCurrentByType(TermsType termsType) throws NoSuchObjectException {
        //TODO JC Implement getCurrentByType
        throw new UnsupportedOperationException("getCurrentByType not implemented");
    }
}
