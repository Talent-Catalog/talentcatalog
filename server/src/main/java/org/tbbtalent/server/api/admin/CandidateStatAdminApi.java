/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.api.admin;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.*;
import org.tbbtalent.server.repository.db.CountryRepository;
import org.tbbtalent.server.request.candidate.stat.CandidateStatsRequest;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.service.db.SavedSearchService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/api/admin/candidate/stat")
public class CandidateStatAdminApi {

    private final CandidateService candidateService;
    private final CountryRepository countryRepository;
    private final SavedListService savedListService;
    private final SavedSearchService savedSearchService;
    private final AuthService authService;

    @Autowired
    public CandidateStatAdminApi(
        CandidateService candidateService,
        CountryRepository countryRepository,
        SavedListService savedListService,
        SavedSearchService savedSearchService,
        AuthService authService) {
        this.candidateService = candidateService;
        this.countryRepository = countryRepository;
        this.savedListService = savedListService;
        this.savedSearchService = savedSearchService;
        this.authService = authService;
    }

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

        //Pick up any source country restrictions based on current user
        List<Long> sourceCountryIds = getDefaultSourceCountryIds();

        //Check whether the requested data to report on is from a set of candidates
        Set<Long> candidateIds = null;
        if (request.getListId() != null) {
            //Get candidates from list
            SavedList list = savedListService.get(request.getListId());
            Set<Candidate> candidates = list.getCandidates();

            candidateIds = new HashSet<>();
            for (Candidate candidate : candidates) {
                candidateIds.add(candidate.getId());
            }

        } else if (request.getSearchId() != null) {
            //Get candidates from search
            candidateIds = savedSearchService.searchCandidates(request.getSearchId());
        }

        convertDateRangeDefaults(request);

        //Report based on set of candidates or date range
        List<StatReport> statReports;
        if (candidateIds != null) {
            statReports = createReports(request.getDateFrom(), request.getDateTo(),  candidateIds, sourceCountryIds);
        } else {
            statReports = createReports(request.getDateFrom(), request.getDateTo(), sourceCountryIds);
        }

        //Construct the dto - just a list of all individual report dtos
        List<Map<String, Object>> dto = new ArrayList<>();
        for (StatReport statReport: statReports) {
            dto.add(statDto().buildReport(statReport));
        }

        return dto;
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

    private List<StatReport> createReports(
            LocalDate dateFrom,
            LocalDate dateTo,
            List<Long> sourceCountryIds ) {
        String language;
        String title;
        String chartType;

        List<StatReport> statReports = new ArrayList<>();

        title = "Gender";
        chartType = "bar";
        statReports.add(new StatReport(title,
                this.candidateService.computeGenderStats(dateFrom, dateTo, sourceCountryIds), chartType));

        title = "Registrations";
        chartType = "bar";
        statReports.add(new StatReport(title,
                this.candidateService.computeRegistrationStats(dateFrom, dateTo, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " (by occupations)",
                this.candidateService.computeRegistrationOccupationStats(dateFrom, dateTo, sourceCountryIds)));

        title = "Birth years";
        chartType = "bar";
        statReports.add(new StatReport(title,
                this.candidateService.computeBirthYearStats(null, dateFrom, dateTo, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeBirthYearStats(Gender.male, dateFrom, dateTo, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeBirthYearStats(Gender.female, dateFrom, dateTo, sourceCountryIds), chartType));

        title = "LinkedIn";
        chartType = "bar";
        statReports.add(new StatReport(title + " links",
            this.candidateService.computeLinkedInExistsStats(dateFrom, dateTo, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " links by candidate registration date",
            this.candidateService.computeLinkedInStats(dateFrom, dateTo, sourceCountryIds), chartType));

        title = "UNHCR";
        chartType = "bar";
        statReports.add(new StatReport(title + "Registered",
            this.candidateService.computeUnhcrRegisteredStats(dateFrom, dateTo, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + "Status",
            this.candidateService.computeUnhcrStatusStats(dateFrom, dateTo, sourceCountryIds), chartType));

        title = "Nationalities by Country";
        statReports.add(new StatReport(title,
                this.candidateService.computeNationalityStats(null, null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeNationalityStats(Gender.male, null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeNationalityStats(Gender.female, null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Jordan)",
                this.candidateService.computeNationalityStats(null, "jordan", dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Lebanon)",
                this.candidateService.computeNationalityStats(null, "lebanon", dateFrom, dateTo, sourceCountryIds)));

        title = "Statuses";
        statReports.add(new StatReport(title,
                this.candidateService.computeStatusStats(null, null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeStatusStats(Gender.male, null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeStatusStats(Gender.female, null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Jordan)",
                this.candidateService.computeStatusStats(null, "jordan", dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Lebanon)",
                this.candidateService.computeStatusStats(null, "lebanon", dateFrom, dateTo, sourceCountryIds)));

        title = "Occupations";
        statReports.add(new StatReport(title + "",
                this.candidateService.computeOccupationStats(null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeOccupationStats(Gender.male, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeOccupationStats(Gender.female, dateFrom, dateTo, sourceCountryIds)));

        title = "Most Common Occupations";
        statReports.add(new StatReport(title + "",
                this.candidateService.computeMostCommonOccupationStats(null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeMostCommonOccupationStats(Gender.male, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeMostCommonOccupationStats(Gender.female, dateFrom, dateTo, sourceCountryIds)));

        title = "Max Education Level";
        statReports.add(new StatReport(title,
                this.candidateService.computeMaxEducationStats(null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeMaxEducationStats(Gender.male, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeMaxEducationStats(Gender.female, dateFrom, dateTo, sourceCountryIds)));

        title = "Languages";
        statReports.add(new StatReport(title,
                this.candidateService.computeLanguageStats(null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeLanguageStats(Gender.male, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeLanguageStats(Gender.female, dateFrom, dateTo, sourceCountryIds)));

        title = "Survey";
        statReports.add(new StatReport(title,
                this.candidateService.computeSurveyStats(null, null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Jordan)",
                this.candidateService.computeSurveyStats(null, "jordan", dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Lebanon)",
                this.candidateService.computeSurveyStats(null, "lebanon", dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeSurveyStats(Gender.male, null, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeSurveyStats(Gender.female, null, dateFrom, dateTo, sourceCountryIds)));


        language = "English";
        title = "Spoken " + language + " Language Level";
        statReports.add(new StatReport(title,
                this.candidateService.computeSpokenLanguageLevelStats(null, language, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeSpokenLanguageLevelStats(Gender.male, language, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeSpokenLanguageLevelStats(Gender.female, language, dateFrom, dateTo, sourceCountryIds)));

        language = "French";
        title = "Spoken " + language + " Language Level";
        statReports.add(new StatReport(title,
                this.candidateService.computeSpokenLanguageLevelStats(null, language, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeSpokenLanguageLevelStats(Gender.male, language, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeSpokenLanguageLevelStats(Gender.female, language, dateFrom, dateTo, sourceCountryIds)));

        return statReports;
    }


    private List<StatReport> createReports(
        LocalDate dateFrom,
        LocalDate dateTo,
        Set<Long> candidateIds,
        List<Long> sourceCountryIds) {

        String language;
        String title;
        String chartType;

        List<StatReport> statReports = new ArrayList<>();
        chartType = "bar";
        statReports.add(new StatReport("Gender",
                this.candidateService.computeGenderStats(dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));

        title = "Registrations";
        chartType = "bar";
        statReports.add(new StatReport(title,
                this.candidateService.computeRegistrationStats(dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " (by occupations)",
                this.candidateService.computeRegistrationOccupationStats(dateFrom, dateTo, candidateIds, sourceCountryIds)));

        title = "Birth years";
        chartType = "bar";
        statReports.add(new StatReport(title,
                this.candidateService.computeBirthYearStats(null, dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeBirthYearStats(Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeBirthYearStats(Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));

        title = "LinkedIn";
        chartType = "bar";
        statReports.add(new StatReport(title + " links",
            this.candidateService.computeLinkedInExistsStats(dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + " links by candidate registration date",
            this.candidateService.computeLinkedInStats(dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));

        title = "UNHCR";
        chartType = "bar";
        statReports.add(new StatReport(title + "Registered",
            this.candidateService.computeUnhcrRegisteredStats(dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));
        statReports.add(new StatReport(title + "Status",
            this.candidateService.computeUnhcrStatusStats(dateFrom, dateTo, candidateIds, sourceCountryIds), chartType));

        title = "Nationalities";
        statReports.add(new StatReport(title,
                this.candidateService.computeNationalityStats(null, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeNationalityStats(Gender.male, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeNationalityStats(Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Jordan)",
                this.candidateService.computeNationalityStats(null, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Lebanon)",
                this.candidateService.computeNationalityStats(null, "lebanon", dateFrom, dateTo, candidateIds, sourceCountryIds)));

        title = "Statuses";
        statReports.add(new StatReport(title,
                this.candidateService.computeStatusStats(null, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeStatusStats(Gender.male, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeStatusStats(Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Jordan)",
                this.candidateService.computeStatusStats(null, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Lebanon)",
                this.candidateService.computeStatusStats(null, "lebanon", dateFrom, dateTo, candidateIds, sourceCountryIds)));

        title = "Occupations";
        statReports.add(new StatReport(title + "",
                this.candidateService.computeOccupationStats(null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeOccupationStats(Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeOccupationStats(Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds)));

        title = "Most Common Occupations";
        statReports.add(new StatReport(title + "",
                this.candidateService.computeMostCommonOccupationStats(null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeMostCommonOccupationStats(Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeMostCommonOccupationStats(Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds)));

        title = "Max Education Level";
        statReports.add(new StatReport(title,
                this.candidateService.computeMaxEducationStats(null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeMaxEducationStats(Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeMaxEducationStats(Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds)));

        title = "Languages";
        statReports.add(new StatReport(title,
                this.candidateService.computeLanguageStats(null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeLanguageStats(Gender.male, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeLanguageStats(Gender.female, dateFrom, dateTo, candidateIds, sourceCountryIds)));

        title = "Survey";
        statReports.add(new StatReport(title,
                this.candidateService.computeSurveyStats(null, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Jordan)",
                this.candidateService.computeSurveyStats(null, "jordan", dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (Lebanon)",
                this.candidateService.computeSurveyStats(null, "lebanon", dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeSurveyStats(Gender.male, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeSurveyStats(Gender.female, null, dateFrom, dateTo, candidateIds, sourceCountryIds)));


        language = "English";
        title = "Spoken " + language + " Language Level";
        statReports.add(new StatReport(title,
                this.candidateService.computeSpokenLanguageLevelStats(null, language, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeSpokenLanguageLevelStats(Gender.male, language, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeSpokenLanguageLevelStats(Gender.female, language, dateFrom, dateTo, candidateIds, sourceCountryIds)));

        language = "French";
        title = "Spoken " + language + " Language Level";
        statReports.add(new StatReport(title,
                this.candidateService.computeSpokenLanguageLevelStats(null, language, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.computeSpokenLanguageLevelStats(Gender.male, language, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.computeSpokenLanguageLevelStats(Gender.female, language, dateFrom, dateTo, candidateIds, sourceCountryIds)));

        return statReports;
    }

    /**
     * Get logged in user’s source country Ids, defaulting to all countries if empty
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
