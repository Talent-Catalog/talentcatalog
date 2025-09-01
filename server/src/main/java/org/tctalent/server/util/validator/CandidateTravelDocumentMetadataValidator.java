package org.tctalent.server.util.validator;/*
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.tctalent.server.exception.InvalidDocumentDataException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.response.MetadataFieldResponse;

public class CandidateTravelDocumentMetadataValidator implements MetadataValidator {
  private static final Set<String> OVERLAPPING_FIELDS = Set.of(
      "firstName", "lastName", "dob", "gender", "birthCountry");

  @Override
  public void validate(Candidate candidate, Map<String, String> fieldAnswers, List<MetadataFieldResponse> requiredMetadata) {
    List<String> missingFields = new ArrayList<>();
    List<String> discrepancies = new ArrayList<>();

    for (MetadataFieldResponse field : requiredMetadata) {
      String fieldName = field.getName();
      String value = fieldAnswers.get(fieldName);

      if (value == null || value.trim().isEmpty()) {
        missingFields.add(fieldName);
      } else if (OVERLAPPING_FIELDS.contains(fieldName)) {
        switch (fieldName) {
          case "firstName":
            if (!value.equalsIgnoreCase(candidate.getUser().getFirstName())) {
              discrepancies.add("First name does not match profile: " + candidate.getUser().getFirstName());
            }
            break;
          case "lastName":
            if (!value.equalsIgnoreCase(candidate.getUser().getLastName())) {
              discrepancies.add("Last name does not match profile: " + candidate.getUser().getLastName());
            }
            break;
          case "dob":
            if (!value.equals(candidate.getDob() != null ? candidate.getDob().toString() : null)) {
              discrepancies.add("Date of birth does not match profile: " + candidate.getDob());
            }
            break;
          case "gender":
            if (!value.equalsIgnoreCase(candidate.getGender() != null ? candidate.getGender().toString() : null)) {
              discrepancies.add("Gender does not match profile: " + candidate.getGender());
            }
            break;
          case "birthCountry":
            if (!value.equalsIgnoreCase(String.valueOf(
                candidate.getBirthCountry() != null ? candidate.getBirthCountry().getId() : null))) {
              discrepancies.add("Country of birth does not match profile: " + (candidate.getBirthCountry() != null ? candidate.getBirthCountry().getName() : "null"));
            }
            break;
        }
      }
    }

    if (!missingFields.isEmpty()) {
      throw new IllegalArgumentException("Missing required metadata fields: " + String.join(", ", missingFields));
    }
    if (!discrepancies.isEmpty()) {
      throw new InvalidDocumentDataException(
          "The entered data does not match your TC profile. Please update your profile to match your legal document: " +
              String.join("; ", discrepancies));
    }
  }
}
