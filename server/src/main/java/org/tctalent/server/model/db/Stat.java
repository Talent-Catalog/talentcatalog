/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.model.db;

import lombok.Getter;

/**
 * Enum for all the stats offered via TC stats. Includes their stat name and chart type for building of stat report.
 *
 * @author Caroline Cameron
 */
@Getter
public enum Stat {
    gender("Gender", "bar"),
    registrations("Registrations", "bar"),
    registrationsOccupations("Registrations (by Occupation)", "doughnut"),
    birthYears("Birth Year", "bar"),
    birthYearsMale("Birth Year (Male)", "bar"),
    birthYearsFemale("Birth Year (Female)", "bar"),
    linkedin("LinkedIn Links", "bar"),
    linkedinRegistration("LinkedIn Links by Candidate Registration Date", "bar"),
    unhcrRegistered("UNHCR Registered", "bar"),
    unhcrStatus("UNHCR Status", "bar"),
    referrers("Referrers", "bar"),
    referrersMale("Referrers (Male)", "bar"),
    referrersFemale("Referrers (Female)", "bar"),
    nationalities("Nationalities", "doughnut"),
    nationalitiesMale("Nationalities (Male)", "doughnut"),
    nationalitiesFemale("Nationalities (Female)", "doughnut"),
    nationalitiesJordan("Nationalities (Jordan)", "doughnut"),
    nationalitiesLebanon("Nationalities (Lebanon)", "doughnut"),
    sourceCountries("Source Countries", "doughnut"),
    sourceCountriesMale("Source Countries (Male)", "doughnut"),
    sourceCountriesFemale("Source Countries (Female)", "doughnut"),
    statuses("Statuses", "doughnut"),
    statusesMale("Statuses (Male)", "doughnut"),
    statusesFemale("Statuses (Female)", "doughnut"),
    statusesJordan("Statuses (Jordan)", "doughnut"),
    statusesLebanon("Statuses (Lebanon)", "doughnut"),
    occupations("Occupations", "doughnut"),
    occupationsMale("Occupations (Male)", "doughnut"),
    occupationsFemale("Occupations (Female)", "doughnut"),
    occupationsCommon("Most Common Occupations", "doughnut"),
    occupationsCommonMale("Most Common Occupations (Male)", "doughnut"),
    occupationsCommonFemale("Most Common Occupations (Female)", "doughnut"),
    maxEducation("Max Education Level", "doughnut"),
    maxEducationMale("Max Education Level (Male)", "doughnut"),
    maxEducationFemale("Max Education Level (Female)", "doughnut"),
    languages("Languages", "doughnut"),
    languagesMale("Languages (Male)", "doughnut"),
    languagesFemale("Languages (Female)", "doughnut"),
    survey("Survey", "doughnut"),
    surveyJordan("Survey (Jordan)", "doughnut"),
    surveyLebanon("Survey (Lebanon)", "doughnut"),
    surveyMale("Survey (Male)", "doughnut"),
    surveyFemale("Survey (Female)", "doughnut"),
    spokenEnglish("Spoken English Language Level", "doughnut"),
    spokenEnglishMale("Spoken English Language Level (Male)", "doughnut"),
    spokenEnglishFemale("Spoken English Language Level (Female)", "doughnut"),
    spokenFrench("Spoken French Language Level", "doughnut"),
    spokenFrenchMale("Spoken French Language Level (Male)", "doughnut"),
    spokenFrenchFemale("Spoken French Language Level (Female)", "doughnut");

    private final String displayName;
    private final String chartType;

    /**
     * Initializes each enum value
     * @param displayName Name of stat
     * @param chartType best chart type for the stat display
     */
    Stat(String displayName, String chartType) {
        this.displayName = displayName;
        this.chartType = chartType;
    }

}
