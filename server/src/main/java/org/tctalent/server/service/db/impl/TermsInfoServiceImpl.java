// Copyright 2009 Cameron Edge Pty Ltd. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Cameron Edge Pty Ltd is strictly prohibited.

package org.tctalent.server.service.db.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.model.db.TermsType;
import org.tctalent.server.repository.db.TermsInfoRepository;
import org.tctalent.server.service.db.TermsInfoService;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TermsInfoServiceImpl implements TermsInfoService {

    private final TermsInfoRepository termsInfoRepository;

    @Override
    @NonNull
    public TermsInfo getCurrentByType(TermsType termsType) {
        return termsInfoRepository.findFirstByTypeOrderByCreatedDateDesc(termsType)
            .orElseThrow(() -> new NoSuchObjectException(TermsInfo.class, termsType.name()));
    }
}
