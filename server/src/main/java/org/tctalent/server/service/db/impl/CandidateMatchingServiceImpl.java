/*
 * Copyright (c) 2026 Talent Catalog.
 */
package org.tctalent.server.service.db.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tctalent.server.repository.db.matching.CandidateMatchingRepository;
import org.tctalent.server.repository.db.matching.CandidateMatchingResult;
import org.tctalent.server.request.candidate.matching.CandidateMatchingRequest;
import org.tctalent.server.service.db.CandidateMatchingService;

@Service
@RequiredArgsConstructor
public class CandidateMatchingServiceImpl implements CandidateMatchingService {
    private final CandidateMatchingRepository candidateMatchingRepository;

    @Override
    public List<CandidateMatchingResult> match(CandidateMatchingRequest request) {
        return candidateMatchingRepository.match(request);
    }
}
