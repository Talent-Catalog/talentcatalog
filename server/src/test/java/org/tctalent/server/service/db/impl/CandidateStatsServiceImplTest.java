/*
 * Copyright (c) 2024 Talent Catalog.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.DataRow;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CandidateSpecification;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.CandidateStatsService;

@Tag("skip-test-in-gradle-build")
@SpringBootTest
class CandidateStatsServiceImplTest {
    @Autowired
    private CandidateStatsService candidateStatsService;
    @Autowired
    private CandidateService candidateService;
    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private CountryRepository countryRepository;

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List <Long> sourceCountryIds;

    @BeforeEach
    void setUp() {
        dateFrom = LocalDate.parse("2016-01-01");
        dateTo = null;

        //Default null dates
        if (dateFrom == null) {
            dateFrom = LocalDate.parse("2000-01-01");
        }
        if (dateTo == null) {
            dateTo = LocalDate.now();
        }

        sourceCountryIds = countryRepository.findAll().stream()
            .map(Country::getId)
            .collect(Collectors.toList());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("Compare old and new ways of doing birth year stats")
    void compareOldAndNewBirthYearStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setGender(Gender.female);
        request.setMinAge(30);


        //New way of running stats is to restrict data over which stats are run by adding a
        //constraint that the candidates should belong to the search query extracted from the
        //search request.
        String sql = request.extractFetchSQL();

        String constraint = "candidate.id in (" + sql + ")";
        final List<DataRow> rows =
            candidateStatsService.computeBirthYearStats(
            null, dateFrom, dateTo, null, sourceCountryIds,
            constraint);

        //Old way of running stats is to restrict data by running the search corresponding to the
        //search request, then extract the ids of all returned candidates and then perform the stats
        //restricting it to candidates whose id's appear in that set of ids.
        //(Problem with this approach is that Postgres has a limit on the number of ids that can
        //appear in a query).
        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent =
        candidateService.computeBirthYearStats(
            null, dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    @Test
    @DisplayName("Compare old and new ways of doing max education level stats")
    void compareOldAndNewMaxEducationLevelStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setMinAge(30);

        String sql = request.extractFetchSQL();

        String constraint = "candidate.id in (" + sql + ")";
        final List<DataRow> rows =
            candidateStatsService.computeMaxEducationStats(
                Gender.female, dateFrom, dateTo, null, sourceCountryIds,
                constraint);

        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent =
            candidateService.computeMaxEducationStats(
                Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    @Test
    @DisplayName("Compare old and new ways of doing most common occupation stats")
    void compareOldAndNewMostCommonOccupationStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setMinAge(30);

        String sql = request.extractFetchSQL();

        String constraint = "candidate.id in (" + sql + ")";
        final List<DataRow> rows =
            candidateStatsService.computeMostCommonOccupationStats(
                Gender.female, dateFrom, dateTo, null, sourceCountryIds,
                constraint);

        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent =
            candidateService.computeMostCommonOccupationStats(
                Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    @Test
    @DisplayName("Compare old and new ways of doing nationality stats")
    void compareOldAndNewNationalityStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setMinAge(30);

        String sql = request.extractFetchSQL();

        String constraint = "candidate.id in (" + sql + ")";
        List<DataRow> rows;
        rows = candidateStatsService.computeNationalityStats(
            Gender.female, null, dateFrom, dateTo, null, sourceCountryIds,
            constraint);

        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent;
        rowsCurrent = candidateService.computeNationalityStats(
            Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);

        //Run again limiting by source country
        rows = candidateStatsService.computeNationalityStats(
            Gender.female, "jordan", dateFrom, dateTo, null, sourceCountryIds,
            constraint);
        rowsCurrent = candidateService.computeNationalityStats(
            Gender.female, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);

        //Run again for all genders in source country
        rows = candidateStatsService.computeNationalityStats(
            null, "jordan", dateFrom, dateTo, null, sourceCountryIds,
            constraint);
        rowsCurrent = candidateService.computeNationalityStats(
            null, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    @Test
    @DisplayName("Compare old and new ways of doing status stats")
    void compareOldAndNewStatusStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setMinAge(30);

        String sql = request.extractFetchSQL();

        String constraint = "candidate.id in (" + sql + ")";
        List<DataRow> rows;
        rows = candidateStatsService.computeStatusStats(
            Gender.female, null, dateFrom, dateTo, null, sourceCountryIds,
            constraint);

        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent;
        rowsCurrent = candidateService.computeStatusStats(
            Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);

        //Run again limiting by source country
        rows = candidateStatsService.computeStatusStats(
            Gender.female, "jordan", dateFrom, dateTo, null, sourceCountryIds,
            constraint);
        rowsCurrent = candidateService.computeStatusStats(
            Gender.female, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);

        //Run again for all genders in source country
        rows = candidateStatsService.computeStatusStats(
            null, "jordan", dateFrom, dateTo, null, sourceCountryIds,
            constraint);
        rowsCurrent = candidateService.computeStatusStats(
            null, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    @Test
    @DisplayName("Compare old and new ways of doing survey stats")
    void compareOldAndNewSurveyStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setMinAge(30);

        String sql = request.extractFetchSQL();

        String constraint = "candidate.id in (" + sql + ")";
        List<DataRow> rows;
        rows = candidateStatsService.computeSurveyStats(
            Gender.female, null, dateFrom, dateTo, null, sourceCountryIds,
            constraint);

        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent;
        rowsCurrent = candidateService.computeSurveyStats(
            Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);

        //Run again limiting by source country
        rows = candidateStatsService.computeSurveyStats(
            Gender.female, "jordan", dateFrom, dateTo, null, sourceCountryIds,
            constraint);
        rowsCurrent = candidateService.computeSurveyStats(
            Gender.female, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);

        //Run again for all genders in source country
        rows = candidateStatsService.computeSurveyStats(
            null, "jordan", dateFrom, dateTo, null, sourceCountryIds,
            constraint);
        rowsCurrent = candidateService.computeSurveyStats(
            null, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    @Test
    @DisplayName("Compare old and new ways of doing occupation stats")
    void compareOldAndNewOccupationStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setMinAge(30);

        String sql = request.extractFetchSQL();

        String constraint = "candidate.id in (" + sql + ")";
        final List<DataRow> rows =
            candidateStatsService.computeOccupationStats(
                Gender.female, dateFrom, dateTo, null, sourceCountryIds,
                constraint);

        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent =
            candidateService.computeOccupationStats(
                Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    @Test
    @DisplayName("Compare old and new ways of doing referrer stats")
    void compareOldAndNewReferrerStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setMinAge(30);

        String sql = request.extractFetchSQL();

        String constraint = "candidate.id in (" + sql + ")";
        List<DataRow> rows;
        rows = candidateStatsService.computeReferrerStats(
            Gender.female, null, dateFrom, dateTo, null, sourceCountryIds,
            constraint);

        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent;
        rowsCurrent = candidateService.computeReferrerStats(
            Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    @Test
    @DisplayName("Compare old and new ways of doing source country stats")
    void compareOldAndNewSourceCountryStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setMinAge(30);

        String sql = request.extractFetchSQL();

        String constraint = "candidate.id in (" + sql + ")";
        List<DataRow> rows;
        rows = candidateStatsService.computeSourceCountryStats(
            Gender.female, dateFrom, dateTo, null, sourceCountryIds,
            constraint);

        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent;
        rowsCurrent = candidateService.computeSourceCountryStats(
            Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    @Test
    @DisplayName("Compare old and new ways of doing spoken language stats")
    void compareOldAndNewSpokenLanguageStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setMinAge(60);
        request.setGender(Gender.female);

        String sql = request.extractFetchSQL();

        String constraint = "candidate.id in (" + sql + ")";
        List<DataRow> rows;
        rows = candidateStatsService.computeSpokenLanguageLevelStats(
            Gender.female, "English", dateFrom, dateTo, null, sourceCountryIds,
            constraint);

        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent;
        rowsCurrent = candidateService.computeSpokenLanguageLevelStats(
            Gender.female, "English", dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    @Test
    @DisplayName("Compare old and new ways of doing gender stats")
    void compareOldAndNewGenderStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setMinAge(3);

        String sql = request.extractFetchSQL();
        String constraintPredicate = "candidate.id in (" + sql + ")";
        final List<DataRow> rows =
            candidateStatsService.computeGenderStats(
             dateFrom, dateTo, null, sourceCountryIds,
            constraintPredicate);

        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent =
        candidateService.computeGenderStats(
             dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    @Test
    @DisplayName("Compare old and new ways of doing language stats")
    void compareOldAndNewLanguageStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setGender(Gender.female);
        request.setMinAge(3);

        String sql = request.extractFetchSQL();
        String constraintPredicate = "candidate.id in (" + sql + ")";
        final List<DataRow> rows =
            candidateStatsService.computeLanguageStats(
             null, dateFrom, dateTo, null, sourceCountryIds,
            constraintPredicate);

        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent =
        candidateService.computeLanguageStats(
             null, dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    @Test
    @DisplayName("Compare old and new ways of doing LinkedIn exists stats")
    void compareOldAndNewLinkedInExistsStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setMinAge(3);

        String sql = request.extractFetchSQL();
        String constraintPredicate = "candidate.id in (" + sql + ")";
        final List<DataRow> rows =
            candidateStatsService.computeLinkedInExistsStats(
                dateFrom, dateTo, null, sourceCountryIds,
                constraintPredicate);

        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent =
            candidateService.computeLinkedInExistsStats(
                dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    @Test
    @DisplayName("Compare old and new ways of doing LinkedIn stats")
    void compareOldAndNewLinkedInStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setMinAge(3);

        String sql = request.extractFetchSQL();
        String constraintPredicate = "candidate.id in (" + sql + ")";
        final List<DataRow> rows =
            candidateStatsService.computeLinkedInStats(
                dateFrom, dateTo, null, sourceCountryIds,
                constraintPredicate);

        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent =
            candidateService.computeLinkedInStats(
                dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    @Test
    @DisplayName("Compare old and new ways of doing Registration stats")
    void compareOldAndNewRegistrationStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setMinAge(3);

        String sql = request.extractFetchSQL();
        String constraintPredicate = "candidate.id in (" + sql + ")";
        final List<DataRow> rows =
            candidateStatsService.computeRegistrationStats(
                dateFrom, dateTo, null, sourceCountryIds,
                constraintPredicate);

        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent =
            candidateService.computeRegistrationStats(
                dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    @Test
    @DisplayName("Compare old and new ways of doing Registration Occupataion stats")
    void compareOldAndNewRegistrationOccupationStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setMinAge(3);

        String sql = request.extractFetchSQL();
        String constraintPredicate = "candidate.id in (" + sql + ")";
        final List<DataRow> rows =
            candidateStatsService.computeRegistrationOccupationStats(
                dateFrom, dateTo, null, sourceCountryIds,
                constraintPredicate);

        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent =
            candidateService.computeRegistrationOccupationStats(
                dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    @Test
    @DisplayName("Compare old and new ways of doing unhcr registered stats")
    void compareOldAndNewUNHCRRegisteredStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setMinAge(3);

        String sql = request.extractFetchSQL();
        String constraintPredicate = "candidate.id in (" + sql + ")";
        final List<DataRow> rows =
            candidateStatsService.computeUnhcrRegisteredStats(
             dateFrom, dateTo, null, sourceCountryIds,
            constraintPredicate);

        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent =
        candidateService.computeUnhcrRegisteredStats(
             dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    @Test
    @DisplayName("Compare old and new ways of doing unhcr_status stats")
    void compareOldAndNewUNHCRStatusStats() {

        //Set up search on which stats will be run
        SearchCandidateRequest request = new SearchCandidateRequest();
        request.setMinAge(3);

        String sql = request.extractFetchSQL();
        String constraintPredicate = "candidate.id in (" + sql + ")";
        final List<DataRow> rows =
            candidateStatsService.computeUnhcrStatusStats(
             dateFrom, dateTo, null, sourceCountryIds,
            constraintPredicate);

        Specification<Candidate> query = CandidateSpecification
            .buildSearchQuery(request, null, null);
        List<Candidate> candidates = candidateRepository.findAll(query);
        Set<Long> candidateIds =
            candidates.stream().map(Candidate::getId).collect(Collectors.toSet());

        List<DataRow> rowsCurrent =
        candidateService.computeUnhcrStatusStats(
             dateFrom, dateTo, candidateIds, sourceCountryIds);

        //The results of running stats both ways should be identical.
        compareResults(rowsCurrent, rows);
    }

    private void compareResults(List<DataRow> rowsCurrent, List<DataRow> rows) {

        assertNotNull(rows);
        assertNotNull(rowsCurrent);

        //The number of collected stats should be the same.
        assertEquals(rowsCurrent.size(), rows.size());

        //And each individual data point should be the same.
        for (int i = 0; i < rows.size(); i++) {
            assertEquals(rows.get(i).getLabel(), rowsCurrent.get(i).getLabel());
            assertEquals(rows.get(i).getValue(), rowsCurrent.get(i).getValue());
        }
    }
}
