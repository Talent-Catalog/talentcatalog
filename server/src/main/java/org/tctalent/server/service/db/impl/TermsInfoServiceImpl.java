
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

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

    /**
     * Stores all TermsInfo instances indexed by id
     */
    private Map<String, TermsInfo> termsInfoMap;

    /**
     * Initialized at start up with all known TermsInfo's.
     */
    private final TermsInfo[] termsInfos;

    public TermsInfoServiceImpl() {
        termsInfos = new TermsInfo[] {
            new TermsInfo(
                "CandidatePrivacyPolicyV1",
                "/terms/GDPRPrivacyPolicy-20250604.html",
                TermsType.CANDIDATE_PRIVACY_POLICY,
                LocalDate.of(2025, Month.JUNE, 5)
            ),
        };
    }

    /**
     * Normally only called once after the constructor (@PostConstruct annotation) with predefined
     * TermsInfo[].
     * (Best to avoid running logic in the constructor especially logic that can throw
     * exceptions, hence use of @PostConstruct).
     */
    @PostConstruct
    private void configure() {
        initialize(termsInfos);
    }

    /**
     * Package private allows it to be set up with different TermsInfo for testing purposes.
     */
    void initialize(TermsInfo[] termsInfos) {
        termsInfoMap = new HashMap<>();
        for (TermsInfo termsInfo : termsInfos) {
            addTermsInfo(termsInfo);
        }
    }

    String getContentFromResource(String resourcePath) {
        String content;
        try (InputStream resourceAsStream = this.getClass().getResourceAsStream(resourcePath)) {
            if (resourceAsStream == null) {
                content = null;
            } else {
                try {
                    content = new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    content = null;
                }
            }
        } catch (IOException e) {
            content = null;
        }
        return content;
    }

    private void addTermsInfo(@NonNull TermsInfo termsInfo) {
        //Check that resource path exists
        final String content = getContentFromResource(termsInfo.getPathToContent());
        if (content == null) {
            throw new RuntimeException("No content found for pathToContent of TermsInfo id: " + termsInfo.getId());
        }
        //Set the content retrieved from the resource file.
        termsInfo.setContent(content);
        TermsInfo previous = termsInfoMap.put(termsInfo.getId(), termsInfo);
        if (previous != null) {
            throw new RuntimeException("Duplicate terms info id: " + termsInfo.getId());
        }
    }

    @Override
    @NonNull
    public TermsInfo get(String id) throws NoSuchObjectException {
        final TermsInfo termsInfo = termsInfoMap.get(id);
        if (termsInfo == null) {
            throw new NoSuchObjectException(TermsInfo.class, id);
        }
        return termsInfo;
    }

    @Override
    @NonNull
    public TermsInfo getCurrentByType(TermsType termsType) throws NoSuchObjectException {
        //TODO JC Implement getCurrentByType
        throw new UnsupportedOperationException("getCurrentByType not implemented");
    }
}
