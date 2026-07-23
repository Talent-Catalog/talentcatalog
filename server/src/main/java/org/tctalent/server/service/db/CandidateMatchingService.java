/*
 * Copyright (c) 2026 Talent Catalog.
 */
package org.tctalent.server.service.db;

import java.util.List;
import org.tctalent.server.repository.db.matching.CandidateMatchingResult;
import org.tctalent.server.request.candidate.matching.CandidateMatchingRequest;

public interface CandidateMatchingService {
    List<CandidateMatchingResult> match(CandidateMatchingRequest request);
}
