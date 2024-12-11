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

import java.util.List;
import org.tctalent.server.model.db.DataRow;

public class StatsApiTestUtil {

  private static final List<DataRow> dateRows = List.of(
      new DataRow("2016-06-04", 3L),
      new DataRow("2016-06-10", 2L),
      new DataRow("2016-06-14", 1L)
  );

  static List<DataRow> getGenderStats() {
    return List.of(
        new DataRow("male", 15111L),
        new DataRow("undefined", 3772L),
        new DataRow("female", 2588L)
    );
  }

  static List<DataRow> getRegistrationStats() {
    return dateRows;
  }

  static List<DataRow> getRegistrationByOccupationStats() {
    return List.of(
        new DataRow("undefined", 11414L),
        new DataRow("Unknown", 1652L),
        new DataRow("Teacher", 777L)
    );
  }

  static List<DataRow> getBirthYearStats() {
    return List.of(
        new DataRow("1948", 3L),
        new DataRow("1950", 2L),
        new DataRow("1951", 1L)
    );
  }

  static List<DataRow> getLinkedInExistsStats() {
    return List.of(
        new DataRow("No link", 2L),
        new DataRow("Has link", 10L)
    );
  }

  static List<DataRow> getLinkedInByRegistrationDateStats() {
    return dateRows;
  }

  static List<DataRow> getUnhcrRegistrationStats() {
    return List.of(
        new DataRow("NoResponse", 3L),
        new DataRow("Yes", 2L),
        new DataRow("No", 1L)
    );
  }

  static List<DataRow> getUnhcrStatusStats() {
    return List.of(
        new DataRow("NoResponse", 4L),
        new DataRow("RegisteredAsylum", 3L),
        new DataRow("NotRegistered", 2L),
        new DataRow("RegisteredStatusUnknown", 1L)
    );
  }

  static List<DataRow> getNationalityStats() {
    return List.of(
        new DataRow("Palestinian Territories", 1073L),
        new DataRow("Syria", 14852L)
    );
  }

  static List<DataRow> getSourceCountryStats() {
    return List.of(
        new DataRow("Lebanon", 7231L),
        new DataRow("Jordan", 4396L)
    );
  }

  static List<DataRow> getStatusStats() {
    return List.of(
        new DataRow("pending", 1000L),
        new DataRow("incomplete", 2000L),
        new DataRow("active", 3000L),
        new DataRow("employed", 4000L),
        new DataRow("autonomousEmployment", 5000L)
    );
  }

  static List<DataRow> getOccupationStats() {
    return List.of(
        new DataRow("undefined", 1000L),
        new DataRow("Teacher", 2000L),
        new DataRow("Accountant", 3000L)
    );
  }

  static List<DataRow> getMaxEducationStats() {
    return List.of(
        new DataRow("Bachelor's Degree", 1000L),
        new DataRow("Primary School", 2000L),
        new DataRow("Doctoral Degree", 3000L)
    );
  }

  static List<DataRow> getLanguageStats() {
    return List.of(
        new DataRow("English", 1000L),
        new DataRow("Arabic", 2000L),
        new DataRow("French", 3000L)
    );
  }

  static List<DataRow> getReferrerStats() {
    return List.of(
        new DataRow("auntie rene", 1000L),
        new DataRow("uncle fred", 2000L)
    );
  }

  static List<DataRow> getSurveyStats() {
    return List.of(
        new DataRow("Facebook", 1000L),
        new DataRow("From a friend", 2000L),
        new DataRow("NGO", 3000L)
    );
  }

  static List<DataRow> getSpokenLanguageLevelStats() {
    return List.of(
        new DataRow("Intermediate Proficiency", 1000L),
        new DataRow("Full Professional Proficiency", 2000L),
        new DataRow("Elementary Proficiency", 3000L)
    );
  }

}
