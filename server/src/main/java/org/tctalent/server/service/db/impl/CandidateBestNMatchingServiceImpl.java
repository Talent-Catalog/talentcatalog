/*
 * Copyright (c) 2026 Talent Catalog.
 */
package org.tctalent.server.service.db.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tctalent.server.configuration.properties.VectorEmbeddingModelProperties;
import org.tctalent.server.repository.db.matching.CandidateBestNMatchingRepository;
import org.tctalent.server.repository.db.matching.CandidateBestNMatchingResult;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.request.candidate.matching.CandidateBestNMatchingRequest;
import org.tctalent.server.service.api.SkillName;
import org.tctalent.server.service.db.CandidateBestNMatchingService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.SkillsService;
import org.tctalent.server.service.embedding.TcVectorEmbeddingService;
import org.tctalent.server.service.embedding.dto.EmbeddingError;
import org.tctalent.server.service.embedding.dto.EmbeddingResult;
import org.tctalent.server.service.embedding.dto.EmbeddingsResponse;

@Service
@RequiredArgsConstructor
public class CandidateBestNMatchingServiceImpl implements CandidateBestNMatchingService {
    private final CandidateBestNMatchingRepository candidateBestNMatchingRepository;
    private final SkillsService skillsService;
    private final TcVectorEmbeddingService tcVectorEmbeddingService;
    private final VectorEmbeddingModelProperties embeddingProperties;
    private final SavedSearchService savedSearchService;


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

        //Extract skills from description
        List<SkillName> skillNames =
            skillsService.extractSkillNames(requirementsDescription, "en");
        //Construct the simpleQueryString by concatenating the skills separated by space.
        //If a skill contains spaces, quote in ""
        String simpleQueryString = skillNames.stream()
            .map(SkillName::getName)
            .map(s -> s.contains(" ") ? "\"" + s + "\"" : s)
            .collect(Collectors.joining(" "));

        //TODO JC Need simpler call for getting a single embedding. Returns error or List<Double>
        Map<String, String> sourceTexts = new HashMap<>();
        sourceTexts.put("target", requirementsDescription);
        final EmbeddingsResponse embeddingsResponse = tcVectorEmbeddingService.generateEmbeddings(
            embeddingProperties.getEmbeddingModelKey(), sourceTexts);
        final List<EmbeddingResult> results1 = embeddingsResponse.getResults();
        if (results1.isEmpty()) {
            throw new RuntimeException("Embedding failed - no results"); //TODO JC
        }
        EmbeddingResult embeddingResult = results1.get(0);
        if (embeddingResult.getError() != null) {
            final EmbeddingError error = embeddingResult.getError();
            throw new RuntimeException(error.getMessage()); //TODO JC
        }

        final List<Double> embedding = results1.get(0).getEmbedding();

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

        String constraintJoinsAndWhereSql = savedSearchService.extractJoinAndWhereSQL(request);

        //Modify request to generate the right lexical search.
        //TODO JC Should retain any existing simpleQueryString? Concatenate?
        request.setSimpleQueryString(simpleQueryString);

        //Force sort by score. This means that score will appear in selected fields.
        request.setSortFields(new String[]{"text_match"});

        String lexicalCandidateScoresSql = savedSearchService.extractUserSearchSql(request);

        List<CandidateBestNMatchingResult> results = candidateBestNMatchingRepository.match(
            matchingRequest, lexicalCandidateScoresSql, constraintJoinsAndWhereSql);

        //TODO JC See if I can execute up to here
        //TODO JC Use CandidateDtoFetchService. It fetches DTos from ids, and populates scores

        throw new UnsupportedOperationException("NotImplemented match");
    }
}
