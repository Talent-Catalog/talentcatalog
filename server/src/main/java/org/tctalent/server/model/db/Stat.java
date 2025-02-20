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
    registrationsOccupations("Registrations (by occupations)", "doughnut"),
    birthYears("Birth years", "bar"),
    birthYearsMale("Birth years (male)", "bar"),
    birthYearsFemale("Birth years (female)", "bar"),
    linkedin("LinkedIn links", "bar"),
    linkedinRegistration("LinkedIn links by candidate registration date", "bar"),
    unhcrRegistered("UNHCR Registered", "bar"),
    unhcrStatus("UNHCR Status", "bar"),
    referrers("Referrers", "bar"),
    referrersMale("Referrers (male)", "bar"),
    referrersFemale("Referrers (female)", "bar"),
    nationalities("Nationalities", "doughnut"),
    nationalitiesMale("Nationalities (male)", "doughnut"),
    nationalitiesFemale("Nationalities (female)", "doughnut"),
    nationalitiesJordan("Nationalities (Jordan)", "doughnut"),
    nationalitiesLebanon("Nationalities (Lebanon)", "doughnut"),
    sourceCountries("Source Countries", "doughnut"),
    sourceCountriesMale("Source Countries (male)", "doughnut"),
    sourceCountriesFemale("Source Countries (female)", "doughnut"),
    statuses("Statuses", "doughnut"),
    statusesMale("Statuses (male)", "doughnut"),
    statusesFemale("Statuses (female)", "doughnut"),
    statusesJordan("Statuses (Jordan)", "doughnut"),
    statusesLebanon("Statuses (Lebanon)", "doughnut"),
    occupations("Occupations", "doughnut"),
    occupationsMale("Occupations (male)", "doughnut"),
    occupationsFemale("Occupations (female)", "doughnut"),
    occupationsCommon("Most Common Occupations", "doughnut"),
    occupationsCommonMale("Most Common Occupations (male)", "doughnut"),
    occupationsCommonFemale("Most Common Occupations (female)", "doughnut"),
    maxEducation("Max Education Level", "doughnut"),
    maxEducationMale("Max Education Level (male)", "doughnut"),
    maxEducationFemale("Max Education Level (female)", "doughnut"),
    languages("Languages", "doughnut"),
    languagesMale("Languages (male)", "doughnut"),
    languagesFemale("Languages (female)", "doughnut"),
    survey("Survey", "doughnut"),
    surveyJordan("Survey (Jordan)", "doughnut"),
    surveyLebanon("Survey (Lebanon)", "doughnut"),
    surveyMale("Survey (male)", "doughnut"),
    surveyFemale("Survey (female)", "doughnut"),
    spokenEnglish("Spoken English Language Level", "doughnut"),
    spokenEnglishMale("Spoken English Language Level (male)", "doughnut"),
    spokenEnglishFemale("Spoken English Language Level (female)", "doughnut"),
    spokenFrench("Spoken French Language Level", "doughnut"),
    spokenFrenchMale("Spoken French Language Level (male)", "doughnut"),
    spokenFrenchFemale("Spoken French Language Level (female)", "doughnut");

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
