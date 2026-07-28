/*
 * Copyright (c) 2026 Talent Catalog.
 */
package org.tctalent.server.service.db.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tctalent.server.repository.db.matching.CandidateBestNMatchingRepository;
import org.tctalent.server.repository.db.matching.CandidateBestNMatchingResult;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.request.candidate.matching.CandidateBestNMatchingRequest;
import org.tctalent.server.service.db.CandidateBestNMatchingService;

@Service
@RequiredArgsConstructor
public class CandidateBestNMatchingServiceImpl implements CandidateBestNMatchingService {
    private final CandidateBestNMatchingRepository candidateBestNMatchingRepository;

    @Override
    public List<CandidateBestNMatchingResult> match(CandidateBestNMatchingRequest request) {
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
        return candidateBestNMatchingRepository.match(
            request, lexicalCandidateScoresSql, constraintJoinsAndWhereSql);
    }

    @Override
    public Page<CandidateReadDto> match(SearchCandidateRequest request) {

        final String requirementsDescription = request.getRequirementsDescription();
        if (!StringUtils.hasText(requirementsDescription)) {
            throw new UnsupportedOperationException("RequirementsDescription must be specified");
        }

        //TODO JC Extract skills from description
        String simpleQueryString = ""; //TODO JC

        //TODO JC Generate embedding from description
        final List<Double> embedding = new ArrayList<>();

        //TODO JC These params are fields taken from the search request.
        int n = 50;
        double lexicalWeight = 0.5;
        CandidateBestNMatchingRequest matchingRequest = CandidateBestNMatchingRequest.builder()
            .simpleQueryString(simpleQueryString)
            .queryEmbedding(embedding)
            .lexicalWeight(lexicalWeight)
            .resultLimit(n)
            .candidateLimit(n*2)
            .semanticPoolSize(n*10)
            .build();

        String lexicalCandidateScoresSql = "";
        String constraintJoinsAndWhereSql = "";

        List<CandidateBestNMatchingResult> results = candidateBestNMatchingRepository.match(
            matchingRequest, lexicalCandidateScoresSql, constraintJoinsAndWhereSql);

        //TODO JC Fetch candidate Dtos

        //TODO JC Populate ranks and explanations

        throw new UnsupportedOperationException("NotImplemented match");
    }
}
