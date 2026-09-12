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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tctalent.server.configuration.properties.VectorEmbeddingModelProperties;
import org.tctalent.server.logging.LogBuilder;
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

@Slf4j
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
    public Page<CandidateReadDto> matchAsSinglePage(SearchCandidateRequest request) {
        List<CandidateReadDto> candidates = match(request);
        int nCandidates = candidates.size();

        int pageSize = nCandidates == 0 ? 1 : nCandidates;
        PageRequest singlePage = PageRequest.of(0, pageSize, Sort.unsorted());
        return new PageImpl<>(candidates, singlePage, nCandidates);
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
        final Double requestedWeight = request.getLexicalWeight();
        double lexicalWeight = requestedWeight == null ? 0.5 : requestedWeight;

        /* **** LEXICAL SEARCH **** */
        //Capture any additional keyword filtering. This will be ANDed into the lexical search
        //to provide additional filtering on the standard text search for extracted skills.
        //Captured before request.setSimpleQueryString below overwrites it.
        final String extraFilteringKeywords = request.getSimpleQueryString();

        //For lexical match, the query string combines the extracted skills with any
        //additional keyword filtering.
        final String filteredSkillsQuery = computeFilteredSkillsQuery(
            skillsQueryString, extraFilteringKeywords);

        //A lexicalWeight > 0 means the lexical rank contributes to the final ranking.
        //If there is no lexical query text, ts_rank scores every candidate 0 and the
        //"ranking" would degenerate into an arbitrary id order. Not finding any skill
        //keywords in free-text requirements is a normal outcome, not a user error, so
        //fall back to a purely semantic match rather than rejecting the request - but
        //log it since it silently overrides an explicitly requested lexicalWeight.
        if (lexicalWeight > 0 && !StringUtils.hasText(filteredSkillsQuery)) {
            LogBuilder.builder(log)
                .action("CandidateBestNMatching")
                .message("No lexical search terms found - falling back from lexicalWeight="
                    + lexicalWeight + " to 0 (pure semantic match)")
                .logWarn();
            lexicalWeight = 0;
        }

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
        request.setSortDirection(Direction.DESC);

        /* **** SEMANTIC SEARCH **** */
        //Note that this same constraint is built into the SQL generated for lexical search.
        String constraintJoinsAndWhereSql = savedSearchService.extractJoinAndWhereSQL(request);

        //Modify the request to use the combined lexical query string computed above, ready
        //for generating the lexical search SQL below.
        request.setSimpleQueryString(filteredSkillsQuery);

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

    /**
     * Computes a filtered skills query by ANDing the skillsQueryString with the
     * extraFilteringKeywords if they are both not empty.
     * @param skillsQueryString The skills query string.
     * @param extraFilteringKeywords The extra filtering keywords.
     * @return The filtered skills query.
     */
    private String computeFilteredSkillsQuery(
        String skillsQueryString, String extraFilteringKeywords) {
        String query;
        if (StringUtils.hasText(skillsQueryString) && StringUtils.hasText(extraFilteringKeywords)) {
            query = "(" + skillsQueryString.strip() + ") + (" + extraFilteringKeywords.strip() + ")";
        } else if (StringUtils.hasText(skillsQueryString)) {
            query = skillsQueryString.strip();
        } else if (StringUtils.hasText(extraFilteringKeywords)) {
            query = extraFilteringKeywords.strip();
        } else {
            query = "";
        }
        return query;
    }

    private List<IdAndScore> convertResults(List<CandidateBestNMatchingResult> results) {
        return results.stream()
            .map(result ->
                new IdAndScore(result.getCandidateId(), result.getRrfScore()))
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
