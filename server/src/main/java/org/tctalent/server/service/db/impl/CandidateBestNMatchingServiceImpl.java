/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
package org.tctalent.server.service.db.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
import org.tctalent.server.service.db.CandidateDtoFetchService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.SkillsService;
import org.tctalent.server.service.embedding.TcVectorEmbeddingService;
import org.tctalent.server.service.embedding.dto.EmbeddingError;
import org.tctalent.server.service.embedding.dto.EmbeddingInputType;
import org.tctalent.server.service.embedding.dto.EmbeddingResult;
import org.tctalent.server.util.textExtract.IdAndScore;

@Service
@RequiredArgsConstructor
public class CandidateBestNMatchingServiceImpl implements CandidateBestNMatchingService {
    private final CandidateBestNMatchingRepository candidateBestNMatchingRepository;
    private final CandidateDtoFetchService candidateDtoFetchService;
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
        return candidateBestNMatchingRepository.match(
            request, lexicalCandidateScoresSql, constraintJoinsAndWhereSql);
    }

    @Override
    public List<CandidateReadDto> match(SearchCandidateRequest request) {

        final String requirements = request.getRequirements();
        if (!StringUtils.hasText(requirements)) {
            throw new UnsupportedOperationException("Requirements must be specified");
        }

        //Extract skills from description
        List<SkillName> skillNames =
            skillsService.extractSkillNames(requirements, "en");
        //Construct the simpleQueryString by concatenating the skills separated by space.
        //If a skill contains spaces, quote in ""
        String skillsQueryString = skillNames.stream()
            .map(SkillName::getName)
            .map(s -> s.contains(" ") ? "\"" + s + "\"" : s)
            .collect(Collectors.joining(" "));

        String modelKey = request.getModelKey();
        if (!StringUtils.hasText(modelKey)) {
            modelKey = embeddingProperties.getDefaultEmbeddingModelKey();
        }

        EmbeddingResult embeddingResult = tcVectorEmbeddingService.generateEmbedding(
            modelKey, null, requirements, EmbeddingInputType.QUERY);

        if (embeddingResult.getError() != null) {
            final EmbeddingError error = embeddingResult.getError();
            throw new RuntimeException(error.getMessage()); //TODO JC
        }

        final List<Double> embedding = embeddingResult.getEmbedding();

        int n = request.getPageSize();
        double lexicalWeight = request.getLexicalScoreProportion();
        CandidateBestNMatchingRequest matchingRequest = CandidateBestNMatchingRequest.builder()
            .simpleQueryString(skillsQueryString)
            .queryEmbedding(embedding)
            .lexicalWeight(lexicalWeight)
            .resultLimit(n)
            .candidateLimit(n*2)
            .semanticPoolSize(n*10)
            .build();

        //In this hybrid matching we take over the simpleQueryString text search query - ignoring
        //anything that has been entered there.
        //The semantic match does not perform a text search.
        //The Lexical match is defined by the skills extracted from the requirementsDescription.

        //Force sort by score.
        request.setSortFields(new String[]{"match_score"});

        /* **** SEMANTIC SEARCH **** */
        //Ignore any text search constraints for the semantic match.
        request.setSimpleQueryString(null);
        String constraintJoinsAndWhereSql = savedSearchService.extractJoinAndWhereSQL(request);

        /* **** LEXICAL SEARCH **** */
        //For lexical match, modify the request to use the query string with the extracted skills.
        request.setSimpleQueryString(skillsQueryString);

        //Generate the SQL for a standard Saved Search based on the skills extracted into
        //the above standard keyword search field.
        String lexicalCandidateScoresSql = savedSearchService.extractUserSearchSql(request);

        /* **** COMBINED SEARCH **** */
        //This will execute SQL combining the standard keyword text search defined by
        //the lexicalCandidateScoresSql together with embedding vector matching, with any
        //additional constraints by standard saved search fields.
        //Then it will combine the two lots of results into a single combined ranking.
        List<CandidateBestNMatchingResult> results = candidateBestNMatchingRepository.match(
            matchingRequest, lexicalCandidateScoresSql, constraintJoinsAndWhereSql);

        //Convert the results to IdAndScore's.
        List<IdAndScore> idAndScores = convertResults(results);
        return candidateDtoFetchService.fetchAndSetScores(idAndScores);
    }

    private List<IdAndScore> convertResults(List<CandidateBestNMatchingResult> results) {
        return results.stream()
            .map(result ->
                new IdAndScore(result.getCandidateId(), result.getRrfScore()))
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
