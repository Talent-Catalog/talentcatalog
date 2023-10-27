/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.StatReport;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.request.candidate.stat.CandidateStatsRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/candidate/stat")
@RequiredArgsConstructor
public class CandidateStatAdminApi {

    private final CandidateService candidateService;
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

        addSpokenLanguageStatsReport("English", dateFrom, dateTo, sourceCountryIds, statReports);
        addSpokenLanguageStatsReport("French", dateFrom, dateTo, sourceCountryIds, statReports);

        return statReports;
    }

    private void addSpokenLanguageStatsReport(String language, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds,
        List<StatReport> statReports) {
        String title = "Spoken " + language + " Language Level";
        statReports.add(new StatReport(title,
            candidateService.computeSpokenLanguageLevelStats(null, language, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeSpokenLanguageLevelStats(Gender.male, language, dateFrom, dateTo, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeSpokenLanguageLevelStats(Gender.female, language, dateFrom, dateTo, sourceCountryIds)));
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


        language = "English";
        title = "Spoken " + language + " Language Level";
        statReports.add(new StatReport(title,
            candidateService.computeSpokenLanguageLevelStats(null, language, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeSpokenLanguageLevelStats(Gender.male, language, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeSpokenLanguageLevelStats(Gender.female, language, dateFrom, dateTo, candidateIds, sourceCountryIds)));

        language = "French";
        title = "Spoken " + language + " Language Level";
        statReports.add(new StatReport(title,
            candidateService.computeSpokenLanguageLevelStats(null, language, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (male)",
            candidateService.computeSpokenLanguageLevelStats(Gender.male, language, dateFrom, dateTo, candidateIds, sourceCountryIds)));
        statReports.add(new StatReport(title + " (female)",
            candidateService.computeSpokenLanguageLevelStats(Gender.female, language, dateFrom, dateTo, candidateIds, sourceCountryIds)));

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
