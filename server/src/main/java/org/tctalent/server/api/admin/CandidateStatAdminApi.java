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

package org.tctalent.server.api.admin;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.StatReport;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.request.candidate.stat.CandidateStatsRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.CandidateStatsService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/candidate/stat")
@RequiredArgsConstructor
@Slf4j
public class CandidateStatAdminApi {

    private final CandidateService candidateService;
    private final CandidateStatsService candidateStatsService;
    private final CountryRepository countryRepository;
    private final SavedListService savedListService;
    private final SavedSearchService savedSearchService;
    private final AuthService authService;

    /**
     * Runs queries to generate all our supported reports.
     * @param request Defines the data that will be reported on
     * @return A number of named reports. Each report is a serialized
     * {@link StatReport}
     * @throws NoSuchObjectException if there is no saved list with the id
     * specified in the request.
     */
    @PostMapping("all")
    public List<Map<String, Object>> getAllStats(
            @RequestBody CandidateStatsRequest request)
            throws NoSuchObjectException {

        List<StatReport> statReports;

        boolean runOldStats = request.getRunOldStats() != null && request.getRunOldStats();
        if (runOldStats) {
            statReports = getAllStatsByOldMethod(request);
        } else {
            statReports = getAllStatsByNewMethod(request);
        }

        //Construct the dto - just a list of all individual report dtos
        List<Map<String, Object>> dto = new ArrayList<>();
        for (StatReport statReport: statReports) {
            dto.add(statDto().buildReport(statReport));
        }

        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .listId(request.getListId())
            .searchId(request.getSearchId())
            .action("Get all stats")
            .message("Returning all stats")
            .logInfo();

        return dto;
    }

    /**
     * Returns the
     */
    @GetMapping("names")
    public List<Map<String, Object>> getAllStatNames() {
        String title;
        String chartType;
        List<StatReport> statReports = new ArrayList<>();

        title = "Gender";
        chartType = "bar";
        statReports.add(new StatReport(title, null, chartType));

        title = "Registrations";
        chartType = "bar";
        statReports.add(new StatReport(title, null, chartType));
        statReports.add(new StatReport(title + " (by occupations)", null, chartType));

        title = "Birth years";
        chartType = "bar";
        statReports.add(new StatReport(title, null, chartType));
        statReports.add(new StatReport(title + " (male)", null, chartType));
        statReports.add(new StatReport(title + " (female)", null, chartType));

        title = "LinkedIn";
        chartType = "bar";
        statReports.add(new StatReport(title + " links", null, chartType));
        statReports.add(new StatReport(title + " links by candidate registration date", null, chartType));

        title = "UNHCR";
        chartType = "bar";
        statReports.add(new StatReport(title + " Registered", null, chartType));
        statReports.add(new StatReport(title + " Status", null, chartType));

        title = "Referrers";
        chartType = "bar";
        statReports.add(new StatReport(title, null, chartType));
        statReports.add(new StatReport(title + " (male)", null, chartType));
        statReports.add(new StatReport(title + " (female)", null, chartType));

        title = "Nationalities";
        statReports.add(new StatReport(title, null, null));
        statReports.add(new StatReport(title + " (male)", null, null));
        statReports.add(new StatReport(title + " (female)", null, null));
        statReports.add(new StatReport(title + " (Jordan)", null, null));
        statReports.add(new StatReport(title + " (Lebanon)", null, null));

        title = "Source Countries";
        statReports.add(new StatReport(title, null, null));
        statReports.add(new StatReport(title + " (male)", null, null));
        statReports.add(new StatReport(title + " (female)", null, null));

        title = "Statuses";
        statReports.add(new StatReport(title, null, null));
        statReports.add(new StatReport(title + " (male)", null, null));
        statReports.add(new StatReport(title + " (female)", null, null));
        statReports.add(new StatReport(title + " (Jordan)", null, null));
        statReports.add(new StatReport(title + " (Lebanon)", null, null));

        title = "Occupations";
        statReports.add(new StatReport(title, null, null));
        statReports.add(new StatReport(title + " (male)", null, null));
        statReports.add(new StatReport(title + " (female)", null, null));

        title = "Most Common Occupations";
        statReports.add(new StatReport(title, null, null));
        statReports.add(new StatReport(title + " (male)", null, null));
        statReports.add(new StatReport(title + " (female)", null, null));

        title = "Max Education Level";
        statReports.add(new StatReport(title, null, null));
        statReports.add(new StatReport(title + " (male)", null, null));
        statReports.add(new StatReport(title + " (female)", null, null));

        title = "Languages";
        statReports.add(new StatReport(title, null, null));
        statReports.add(new StatReport(title + " (male)", null, null));
        statReports.add(new StatReport(title + " (female)", null, null));

        title = "Survey";
        statReports.add(new StatReport(title, null, null));
        statReports.add(new StatReport(title + " (Jordan)", null, null));
        statReports.add(new StatReport(title + " (Lebanon)", null, null));
        statReports.add(new StatReport(title + " (male)", null, null));
        statReports.add(new StatReport(title + " (female)", null, null));

        title = "Spoken English Language Level";
        statReports.add(new StatReport(title, null, null));
        statReports.add(new StatReport(title + " (male)", null, null));
        statReports.add(new StatReport(title + " (female)", null, null));

        title = "Spoken French Language Level";
        statReports.add(new StatReport(title, null, null));
        statReports.add(new StatReport(title + " (male)", null, null));
        statReports.add(new StatReport(title + " (female)", null, null));

        //Construct the dto - just a list of all individual report dtos
        List<Map<String, Object>> dto = new ArrayList<>();
        for (StatReport statReport: statReports) {
            dto.add(statDto().buildReport(statReport));
        }

        return dto;
    }

    @NonNull
    private List<StatReport> getAllStatsByNewMethod(CandidateStatsRequest request) {
        //Pick up any source country restrictions based on current user
        List<Long> sourceCountryIds = getDefaultSourceCountryIds();

        //Default any null dates
        convertDateRangeDefaults(request);

        List<StatReport> statReports;

        if (request.getListId() != null) {
//            LIST
            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .listId(request.getListId())
                .action("Get all stats")
                .message("Getting all stats for list with id: " + request.getListId())
                .logInfo();

            //Get candidates from list
            SavedList list = savedListService.get(request.getListId());
            Set<Candidate> candidates = list.getCandidates();

            Set<Long> candidateIds = new HashSet<>();
            for (Candidate candidate : candidates) {
                candidateIds.add(candidate.getId());
            }

            convertDateRangeDefaults(request);

            //Report based on set of candidates or date range
            statReports = createNewReports(request.getDateFrom(), request.getDateTo(),
                candidateIds, sourceCountryIds, null, request.getStatNames());
        } else {
            final Long searchId = request.getSearchId();
            if (searchId == null) {
                //No list and no search - this will report on all data
                statReports = createNewReports(request.getDateFrom(), request.getDateTo(),
                    null, sourceCountryIds, null, request.getStatNames());
            } else {

                // SEARCH
                if (savedSearchService.includesElasticSearch(searchId)) {
                    //SEARCH containing Elastic Search

                    //Stats on searches which contain an elastic search can only be constrained
                    //by a set of candidate ids (because SQL Subquery constraints can only be used
                    //to constrain Postgres - not Elastic search).

                    //Run the search and collect the candidateIds.
                    //Warning that this call clears the JPA persistence context - see its JavaDoc
                    //Also note that the following call will throw an InvalidRequestException if
                    //the number of candidates returned by the search is greater than 32,000.
                    //Stats cannot be gathered from searches containing an Elastic search which
                    //return more than that number of candidates.
                    Set<Long> candidateIds = savedSearchService.searchCandidates(searchId);

                    statReports = createNewReports(request.getDateFrom(), request.getDateTo(),
                        candidateIds, sourceCountryIds, null, request.getStatNames());

                } else {
                    //SEARCH just containing Postgres SQL (no Elastic search)

                    SearchCandidateRequest searchRequest =
                        savedSearchService.loadSavedSearch(searchId);
                    String sql = searchRequest.extractSQL(true);
                    String constraint = "candidate.id in (" + sql + ")";

                    statReports = createNewReports(request.getDateFrom(), request.getDateTo(),
                        null, sourceCountryIds, constraint, request.getStatNames());
                }
            }
        }
        return statReports;
    }

    private List<StatReport> createNewReports(
            LocalDate dateFrom,
            LocalDate dateTo,
            Set<Long> candidateIds,
            List<Long> sourceCountryIds,
            String constraint,
            List<String> statNames) {

        String title;
        String chartType;

        List<StatReport> statReports = new ArrayList<>();


        title = "Gender";
        if (statNames.contains(title)) {
            chartType = "bar";
            statReports.add(new StatReport(title,
                    candidateStatsService.computeGenderStats(
                            dateFrom, dateTo, candidateIds, sourceCountryIds, constraint), chartType));
        }

        if (statNames.contains(title)) {
            title = "Registrations";
            chartType = "bar";
            statReports.add(new StatReport(title,
                    candidateStatsService.computeRegistrationStats(
                            dateFrom, dateTo, candidateIds, sourceCountryIds, constraint), chartType));
            statReports.add(new StatReport(title + " (by occupations)",
                    candidateStatsService.computeRegistrationOccupationStats(
                            dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        }

        //todo what to do about titles that are additions? Is there a better way to loop through the titles?
        if (statNames.contains(title)) {
            title = "Birth years";
            chartType = "bar";
            statReports.add(new StatReport(title,
                    candidateStatsService.computeBirthYearStats(
                            null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint),
                    chartType));
            statReports.add(new StatReport(title + " (male)",
                    candidateStatsService.computeBirthYearStats(
                            Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint),
                    chartType));
            statReports.add(new StatReport(title + " (female)",
                    candidateStatsService.computeBirthYearStats(
                            Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint), chartType));
        }


        title = "LinkedIn";
        chartType = "bar";
        statReports.add(new StatReport(title + " links",
            candidateStatsService.computeLinkedInExistsStats(
                dateFrom, dateTo, candidateIds, sourceCountryIds, constraint), chartType));
        statReports.add(new StatReport(title + " links by candidate registration date",
            candidateStatsService.computeLinkedInStats(
                dateFrom, dateTo, candidateIds, sourceCountryIds, constraint), chartType));

        title = "UNHCR";
        chartType = "bar";
        statReports.add(new StatReport(title + " Registered",
            candidateStatsService.computeUnhcrRegisteredStats(
                dateFrom, dateTo, candidateIds, sourceCountryIds, constraint), chartType));
        statReports.add(new StatReport(title + " Status",
            candidateStatsService.computeUnhcrStatusStats(
                dateFrom, dateTo, candidateIds, sourceCountryIds, constraint), chartType));

        title = "Referrers";
        chartType = "bar";
        statReports.add(new StatReport(title,
            candidateStatsService.computeReferrerStats(
                null, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint), chartType));
        statReports.add(new StatReport(title + " (male)",
            candidateStatsService.computeReferrerStats(
                Gender.male, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint), chartType));
        statReports.add(new StatReport(title + " (female)",
            candidateStatsService.computeReferrerStats(
                Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint), chartType));

        title = "Nationalities";
        statReports.add(new StatReport(title,
            candidateStatsService.computeNationalityStats(
                null, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (male)",
            candidateStatsService.computeNationalityStats(
                Gender.male, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (female)",
            candidateStatsService.computeNationalityStats(
                Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (Jordan)",
            candidateStatsService.computeNationalityStats(
                null, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (Lebanon)",
            candidateStatsService.computeNationalityStats(
                null, "lebanon", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));

        title = "Source Countries";
        statReports.add(new StatReport(title,
            candidateStatsService.computeSourceCountryStats(
                null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (male)",
            candidateStatsService.computeSourceCountryStats(
                Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (female)",
            candidateStatsService.computeSourceCountryStats(
                Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));

        title = "Statuses";
        statReports.add(new StatReport(title,
            candidateStatsService.computeStatusStats(
                null, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (male)",
            candidateStatsService.computeStatusStats(
                Gender.male, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (female)",
            candidateStatsService.computeStatusStats(
                Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (Jordan)",
            candidateStatsService.computeStatusStats(
                null, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (Lebanon)",
            candidateStatsService.computeStatusStats(
                null, "lebanon", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));

        title = "Occupations";
        statReports.add(new StatReport(title,
            candidateStatsService.computeOccupationStats(
                null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (male)",
            candidateStatsService.computeOccupationStats(
                Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (female)",
            candidateStatsService.computeOccupationStats(
                Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));

        title = "Most Common Occupations";
        statReports.add(new StatReport(title,
            candidateStatsService.computeMostCommonOccupationStats(
                null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (male)",
            candidateStatsService.computeMostCommonOccupationStats(
                Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (female)",
            candidateStatsService.computeMostCommonOccupationStats(
                Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));

        title = "Max Education Level";
        statReports.add(new StatReport(title,
            candidateStatsService.computeMaxEducationStats(
                null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (male)",
            candidateStatsService.computeMaxEducationStats(
                Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (female)",
            candidateStatsService.computeMaxEducationStats(
                Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));

        title = "Languages";
        statReports.add(new StatReport(title,
            candidateStatsService.computeLanguageStats(
                null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (male)",
            candidateStatsService.computeLanguageStats(
                Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (female)",
            candidateStatsService.computeLanguageStats(
                Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));

        title = "Survey";
        statReports.add(new StatReport(title,
            candidateStatsService.computeSurveyStats(
                null, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (Jordan)",
            candidateStatsService.computeSurveyStats(
                null, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (Lebanon)",
            candidateStatsService.computeSurveyStats(
                null, "lebanon", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (male)",
            candidateStatsService.computeSurveyStats(
                Gender.male, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (female)",
            candidateStatsService.computeSurveyStats(
                Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));

        addSpokenLanguageLevelStatNewReports(
            "English", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint, statReports);
        addSpokenLanguageLevelStatNewReports(
            "French", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint, statReports);

        return statReports;
    }

    private List<StatReport> getAllStatsByOldMethod(CandidateStatsRequest request) {

        //Pick up any source country restrictions based on current user
        List<Long> sourceCountryIds = getDefaultSourceCountryIds();

        //Check whether the requested data to report on is from a set of candidates
        Set<Long> candidateIds = null;
        if (request.getListId() != null) {

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .listId(request.getListId())
                .action("Get all stats")
                .message("Getting all stats for list with id: " + request.getListId())
                .logInfo();

            //Get candidates from list
            SavedList list = savedListService.get(request.getListId());
            Set<Candidate> candidates = list.getCandidates();

            candidateIds = new HashSet<>();
            for (Candidate candidate : candidates) {
                candidateIds.add(candidate.getId());
            }

        } else if (request.getSearchId() != null) {

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .searchId(request.getSearchId())
                .action("Get all stats")
                .message("Getting all stats for search with id: " + request.getSearchId())
                .logInfo();

            //Get candidates from search
            candidateIds = savedSearchService.searchCandidates(request.getSearchId());

            //Warning that the above call clears the JPA persistence context - see its JavaDoc
        }

        convertDateRangeDefaults(request);

        //Report based on set of candidates or date range
        List<StatReport> statReports;
        if (candidateIds != null) {
            statReports = createOldReports(request.getDateFrom(), request.getDateTo(),  candidateIds, sourceCountryIds);
        } else {
            statReports = createOldReports(request.getDateFrom(), request.getDateTo(), sourceCountryIds);
        }
        return statReports;
    }

    /**
     * Convert null dates to default values.
     */
    private void convertDateRangeDefaults(CandidateStatsRequest request){
        if (request.getDateFrom() == null) {
            request.setDateFrom(LocalDate.parse("2000-01-01"));
        }

        if(request.getDateTo() == null) {
            request.setDateTo(LocalDate.now());
        }
    }

    private List<StatReport> createOldReports(
            LocalDate dateFrom,
            LocalDate dateTo,
            List<Long> sourceCountryIds ) {
        String title;
        String chartType;

        List<StatReport> statReports = new ArrayList<>();

        title = "Gender";
        chartType = "bar";
        statReports.add(new StatReport(title,
            candidateService.computeGenderStats(dateFrom, dateTo, sourceCountryIds), chartType));

        title = "Registrations";
        chartType = "bar";
        statReports.add(new StatReport(title,
            candidateService.computeRegistrationStats(dateFrom, dateTo, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " (by occupations)",
            candidateService.computeRegistrationOccupationStats(dateFrom, dateTo, sourceCountryIds)));

        title = "Birth years";
        chartType = "bar";
        statReports.add(new StatReport(title,
            candidateService.computeBirthYearStats(null, dateFrom, dateTo, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeBirthYearStats(Gender.male, dateFrom, dateTo, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeBirthYearStats(Gender.female, dateFrom, dateTo, sourceCountryIds), chartType));

        title = "LinkedIn";
        chartType = "bar";
        statReports.add(new StatReport(title + " links",
            candidateService.computeLinkedInExistsStats(dateFrom, dateTo, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " links by candidate registration date",
            candidateService.computeLinkedInStats(dateFrom, dateTo, sourceCountryIds), chartType));

        title = "UNHCR";
        chartType = "bar";
        statReports.add(new StatReport(title + " Registered",
            candidateService.computeUnhcrRegisteredStats(dateFrom, dateTo, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " Status",
            candidateService.computeUnhcrStatusStats(dateFrom, dateTo, sourceCountryIds), chartType));

        title = "Nationalities by Country";
        statReports.add(new StatReport(title,
            candidateService.computeNationalityStats(null, null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeNationalityStats(Gender.male, null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeNationalityStats(Gender.female, null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Jordan)",
            candidateService.computeNationalityStats(null, "jordan", dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Lebanon)",
            candidateService.computeNationalityStats(null, "lebanon", dateFrom, dateTo, sourceCountryIds)));

        title = "Source Countries";
        statReports.add(new StatReport(title,
            candidateService.computeSourceCountryStats(null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeSourceCountryStats(Gender.male, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeSourceCountryStats(Gender.female, dateFrom, dateTo, sourceCountryIds)));

        title = "Statuses";
        statReports.add(new StatReport(title,
            candidateService.computeStatusStats(null, null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeStatusStats(Gender.male, null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeStatusStats(Gender.female, null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Jordan)",
            candidateService.computeStatusStats(null, "jordan", dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Lebanon)",
            candidateService.computeStatusStats(null, "lebanon", dateFrom, dateTo, sourceCountryIds)));

        title = "Occupations";
        statReports.add(new StatReport(title,
            candidateService.computeOccupationStats(null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeOccupationStats(Gender.male, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeOccupationStats(Gender.female, dateFrom, dateTo, sourceCountryIds)));

        title = "Most Common Occupations";
        statReports.add(new StatReport(title,
            candidateService.computeMostCommonOccupationStats(null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeMostCommonOccupationStats(Gender.male, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeMostCommonOccupationStats(Gender.female, dateFrom, dateTo, sourceCountryIds)));

        title = "Max Education Level";
        statReports.add(new StatReport(title,
            candidateService.computeMaxEducationStats(null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeMaxEducationStats(Gender.male, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeMaxEducationStats(Gender.female, dateFrom, dateTo, sourceCountryIds)));

        title = "Languages";
        statReports.add(new StatReport(title,
            candidateService.computeLanguageStats(null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeLanguageStats(Gender.male, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeLanguageStats(Gender.female, dateFrom, dateTo, sourceCountryIds)));

        title = "Referrers";
        chartType = "bar";
        statReports.add(new StatReport(title,
            candidateService.computeReferrerStats(null, null, dateFrom, dateTo, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeReferrerStats(Gender.male, null, dateFrom, dateTo, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeReferrerStats(Gender.female, null, dateFrom, dateTo, sourceCountryIds), chartType));

        title = "Survey";
        statReports.add(new StatReport(title,
            candidateService.computeSurveyStats(null, null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Jordan)",
            candidateService.computeSurveyStats(null, "jordan", dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Lebanon)",
            candidateService.computeSurveyStats(null, "lebanon", dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeSurveyStats(Gender.male, null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeSurveyStats(Gender.female, null, dateFrom, dateTo, sourceCountryIds)));

        addSpokenLanguageLevelStatOldReports("English", dateFrom, dateTo, sourceCountryIds, statReports);
        addSpokenLanguageLevelStatOldReports("French", dateFrom, dateTo, sourceCountryIds, statReports);

        return statReports;
    }

    private void addSpokenLanguageLevelStatOldReports(String language, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds,
        List<StatReport> statReports) {
        String title = "Spoken " + language + " Language Level";
        statReports.add(new StatReport(title,
            candidateService.computeSpokenLanguageLevelStats(null, language, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeSpokenLanguageLevelStats(Gender.male, language, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeSpokenLanguageLevelStats(Gender.female, language, dateFrom, dateTo, sourceCountryIds)));
    }

    private List<StatReport> createOldReports(
        LocalDate dateFrom,
        LocalDate dateTo,
        Set<Long> candidateIds,
        List<Long> sourceCountryIds) {

        String title;
        String chartType;

        List<StatReport> statReports = new ArrayList<>();
        chartType = "bar";
        statReports.add(new StatReport("Gender",
            candidateService.computeGenderStats(dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));

        title = "Registrations";
        chartType = "bar";
        statReports.add(new StatReport(title,
            candidateService.computeRegistrationStats(dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " (by occupations)",
            candidateService.computeRegistrationOccupationStats(dateFrom, dateTo, candidateIds, sourceCountryIds)));

        title = "Birth years";
        chartType = "bar";
        statReports.add(new StatReport(title,
            candidateService.computeBirthYearStats(null, dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeBirthYearStats(Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeBirthYearStats(Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));

        title = "LinkedIn";
        chartType = "bar";
        statReports.add(new StatReport(title + " links",
            candidateService.computeLinkedInExistsStats(dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " links by candidate registration date",
            candidateService.computeLinkedInStats(dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));

        title = "UNHCR";
        chartType = "bar";
        statReports.add(new StatReport(title + " Registered",
            candidateService.computeUnhcrRegisteredStats(dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " Status",
            candidateService.computeUnhcrStatusStats(dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));

        title = "Referrers";
        chartType = "bar";
        statReports.add(new StatReport(title,
            candidateService.computeReferrerStats(null, null, dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeReferrerStats(Gender.male, null, dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeReferrerStats(Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));

        title = "Nationalities";
        statReports.add(new StatReport(title,
            candidateService.computeNationalityStats(null, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeNationalityStats(Gender.male, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeNationalityStats(Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Jordan)",
            candidateService.computeNationalityStats(null, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Lebanon)",
            candidateService.computeNationalityStats(null, "lebanon", dateFrom, dateTo, candidateIds, sourceCountryIds)));

        title = "Source Countries";
        statReports.add(new StatReport(title,
            candidateService.computeSourceCountryStats(null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeSourceCountryStats(Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeSourceCountryStats(Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds)));

        title = "Statuses";
        statReports.add(new StatReport(title,
            candidateService.computeStatusStats(null, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeStatusStats(Gender.male, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeStatusStats(Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Jordan)",
            candidateService.computeStatusStats(null, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Lebanon)",
            candidateService.computeStatusStats(null, "lebanon", dateFrom, dateTo, candidateIds, sourceCountryIds)));

        title = "Occupations";
        statReports.add(new StatReport(title,
            candidateService.computeOccupationStats(null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeOccupationStats(Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeOccupationStats(Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds)));

        title = "Most Common Occupations";
        statReports.add(new StatReport(title,
            candidateService.computeMostCommonOccupationStats(null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeMostCommonOccupationStats(Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeMostCommonOccupationStats(Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds)));

        title = "Max Education Level";
        statReports.add(new StatReport(title,
            candidateService.computeMaxEducationStats(null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeMaxEducationStats(Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeMaxEducationStats(Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds)));

        title = "Languages";
        statReports.add(new StatReport(title,
            candidateService.computeLanguageStats(null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeLanguageStats(Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeLanguageStats(Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds)));

        title = "Survey";
        statReports.add(new StatReport(title,
            candidateService.computeSurveyStats(null, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Jordan)",
            candidateService.computeSurveyStats(null, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Lebanon)",
            candidateService.computeSurveyStats(null, "lebanon", dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeSurveyStats(Gender.male, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeSurveyStats(Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));

        addSpokenLanguageLevelStatOldReports("English", dateFrom, dateTo, candidateIds, sourceCountryIds, statReports);
        addSpokenLanguageLevelStatOldReports("French", dateFrom, dateTo, candidateIds, sourceCountryIds, statReports);

        return statReports;
    }

    private void addSpokenLanguageLevelStatNewReports(
        String language, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds,
        List<Long> sourceCountryIds, String constraint,
        List<StatReport> statReports) {
        String title = "Spoken " + language + " Language Level";
        statReports.add(new StatReport(title,
            candidateStatsService.computeSpokenLanguageLevelStats(
                null, language, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (male)",
            candidateStatsService.computeSpokenLanguageLevelStats(
                Gender.male, language, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
        statReports.add(new StatReport(title + " (female)",
            candidateStatsService.computeSpokenLanguageLevelStats(
                Gender.female, language, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint)));
    }

    private void addSpokenLanguageLevelStatOldReports(String language, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds,
        List<StatReport> statReports) {
        String title = "Spoken " + language + " Language Level";
        statReports.add(new StatReport(title,
            candidateService.computeSpokenLanguageLevelStats(null, language, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeSpokenLanguageLevelStats(Gender.male, language, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeSpokenLanguageLevelStats(Gender.female, language, dateFrom, dateTo, candidateIds, sourceCountryIds)));
    }

    /**
     * Get logged-in user’s source country Ids, defaulting to all countries if empty
     */
    private List<Long> getDefaultSourceCountryIds(){
        User user = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        List<Long> listOfCountryIds;

        if(CollectionUtils.isEmpty(user.getSourceCountries())){
            listOfCountryIds = countryRepository.findAll().stream()
                    .map(Country::getId)
                    .collect(Collectors.toList());
        } else {
            listOfCountryIds = user.getSourceCountries().stream()
                    .map(Country::getId)
                    .collect(Collectors.toList());
        }

        return listOfCountryIds;
    }

    private DtoBuilder statDto() {
        return new DtoBuilder()
                .add("label")
                .add("value");
    }

}
