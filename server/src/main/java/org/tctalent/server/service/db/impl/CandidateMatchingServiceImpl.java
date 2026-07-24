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
        String lexicalCandidateScoresSql = """
select distinct candidate.id as candidate_id,
ts_rank(candidate.ts_text,to_tsquery('english','welder')) as lexical_score
from candidate left join users on candidate.user_id = users.id
where candidate.ts_text @@ to_tsquery('english','welder')
  and candidate.status in ('active','incomplete','ineligibleReview','pending','unreachable')
  and candidate.id not in (select candidate_id from candidate_saved_list where saved_list_id = 71)
  and users.partner_id in (10002,1,4,7,10003,5,6,8,3,10004)
order by lexical_score DESC,candidate.id DESC;
            """;

        String constraintJoinsAndWhereSql = """
left join candidate_occupation on candidate.id = candidate_occupation.candidate_id
WHERE candidate_occupation.occupation_id in (:occupationId)
            """;
        //TODO JC Compute the special sql
        return candidateMatchingRepository.match(
            request, lexicalCandidateScoresSql, constraintJoinsAndWhereSql);
    }
}
