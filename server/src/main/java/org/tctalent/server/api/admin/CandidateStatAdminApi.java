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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.DataRow;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.Stat;
import org.tctalent.server.model.db.StatReport;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.request.candidate.stat.CandidateStatsRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateStatsService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/candidate/stat")
@RequiredArgsConstructor
@Slf4j
public class CandidateStatAdminApi {

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

        List<StatReport> statReports = getAllStatsByNewMethod(request);

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
                candidateIds, sourceCountryIds, null, request.getSelectedStats());
        } else {
            final Long searchId = request.getSearchId();
            if (searchId == null) {
                //No list and no search - this will report on all data
                statReports = createNewReports(request.getDateFrom(), request.getDateTo(),
                    null, sourceCountryIds, null, request.getSelectedStats());
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
                        candidateIds, sourceCountryIds, null, request.getSelectedStats());

                } else {
                    //SEARCH just containing Postgres SQL (no Elastic search)

                    SearchCandidateRequest searchRequest =
                        savedSearchService.loadSavedSearch(searchId);
                    String sql = searchRequest.extractFetchSQL();
                    String constraint = "candidate.id in (" + sql + ")";

                    statReports = createNewReports(request.getDateFrom(), request.getDateTo(),
                        null, sourceCountryIds, constraint, request.getSelectedStats());
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
            List<Stat> selectedStats) {

        List<StatReport> statReports = new ArrayList<>();

        for (Stat stat : selectedStats) {
            List<DataRow> computedStats =
                switch (stat) {
                    case gender -> candidateStatsService.computeGenderStats(
                            dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case registrations -> candidateStatsService.computeRegistrationStats(
                            dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case registrationsOccupations -> candidateStatsService.computeRegistrationOccupationStats(
                            dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case birthYears -> candidateStatsService.computeBirthYearStats(
                            null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case birthYearsMale -> candidateStatsService.computeBirthYearStats(
                            Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case birthYearsFemale -> candidateStatsService.computeBirthYearStats(
                            Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case linkedin -> candidateStatsService.computeLinkedInExistsStats(
                            dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case linkedinRegistration -> candidateStatsService.computeLinkedInStats(
                            dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case unhcrRegistered -> candidateStatsService.computeUnhcrRegisteredStats(
                            dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case unhcrStatus -> candidateStatsService.computeUnhcrStatusStats(
                            dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case referrers -> candidateStatsService.computeReferrerStats(
                            null, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case referrersMale -> candidateStatsService.computeReferrerStats(
                            Gender.male, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case referrersFemale -> candidateStatsService.computeReferrerStats(
                            Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case nationalities -> candidateStatsService.computeNationalityStats(
                            null, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case nationalitiesMale -> candidateStatsService.computeNationalityStats(
                            Gender.male, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case nationalitiesFemale -> candidateStatsService.computeNationalityStats(
                            Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case nationalitiesJordan -> candidateStatsService.computeNationalityStats(
                            null, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case nationalitiesLebanon -> candidateStatsService.computeNationalityStats(
                            null, "lebanon", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case sourceCountries -> candidateStatsService.computeSourceCountryStats(
                            null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case sourceCountriesMale -> candidateStatsService.computeSourceCountryStats(
                            Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case sourceCountriesFemale -> candidateStatsService.computeSourceCountryStats(
                            Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case statuses -> candidateStatsService.computeStatusStats(
                            null, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case statusesMale -> candidateStatsService.computeStatusStats(
                            Gender.male, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case statusesFemale -> candidateStatsService.computeStatusStats(
                            Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case statusesJordan -> candidateStatsService.computeStatusStats(
                            null, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case statusesLebanon -> candidateStatsService.computeStatusStats(
                            null, "lebanon", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case occupations -> candidateStatsService.computeOccupationStats(
                            null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case occupationsMale -> candidateStatsService.computeOccupationStats(
                            Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case occupationsFemale -> candidateStatsService.computeOccupationStats(
                            Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case occupationsCommon -> candidateStatsService.computeMostCommonOccupationStats(
                            null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case occupationsCommonMale -> candidateStatsService.computeMostCommonOccupationStats(
                            Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case occupationsCommonFemale -> candidateStatsService.computeMostCommonOccupationStats(
                            Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case maxEducation -> candidateStatsService.computeMaxEducationStats(
                            null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case maxEducationMale -> candidateStatsService.computeMaxEducationStats(
                            Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case maxEducationFemale -> candidateStatsService.computeMaxEducationStats(
                            Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case languages -> candidateStatsService.computeLanguageStats(
                            null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case languagesMale -> candidateStatsService.computeLanguageStats(
                            Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case languagesFemale -> candidateStatsService.computeLanguageStats(
                            Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case survey -> candidateStatsService.computeSurveyStats(
                            null, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case surveyJordan -> candidateStatsService.computeSurveyStats(
                            null, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case surveyLebanon -> candidateStatsService.computeSurveyStats(
                            null, "lebanon", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case surveyMale -> candidateStatsService.computeSurveyStats(
                            Gender.male, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case surveyFemale -> candidateStatsService.computeSurveyStats(
                            Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case spokenEnglish -> candidateStatsService.computeSpokenLanguageLevelStats(
                            null, "English", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case spokenEnglishMale -> candidateStatsService.computeSpokenLanguageLevelStats(
                            Gender.male, "English", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case spokenEnglishFemale -> candidateStatsService.computeSpokenLanguageLevelStats(
                            Gender.female, "English", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case spokenFrench -> candidateStatsService.computeSpokenLanguageLevelStats(
                            null, "French", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case spokenFrenchMale -> candidateStatsService.computeSpokenLanguageLevelStats(
                            Gender.male, "French", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                    case spokenFrenchFemale -> candidateStatsService.computeSpokenLanguageLevelStats(
                            Gender.female, "French", dateFrom, dateTo, candidateIds, sourceCountryIds, constraint);
                };
            statReports.add(new StatReport(stat.getDisplayName(), computedStats, stat.getChartType()));
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
