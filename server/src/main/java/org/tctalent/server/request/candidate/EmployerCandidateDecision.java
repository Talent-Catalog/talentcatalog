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

package org.tctalent.server.request.candidate;

import java.util.ArrayList;
import java.util.List;

/**
 * Employer decisions on candidates
 * <p/>
 * MODEL - look up enum from display name
 * @author John Cameron
 */
public enum EmployerCandidateDecision {
  NoDecision(""),
  JobOffer("Job Offer"),
  NoJobOffer("No Offer");

  private final String displayText;

  EmployerCandidateDecision(String displayText) {
    this.displayText = displayText;
  }

  public static List<String> getDisplayTextValues() {
    List<String> values = new ArrayList<>();
    for (EmployerCandidateDecision decision: EmployerCandidateDecision.values()) {
      values.add(decision.displayText);
    }
    return values;
  }

  public static EmployerCandidateDecision textToEnum(String displayText) {
    for (EmployerCandidateDecision decision: EmployerCandidateDecision.values()) {
      if (decision.displayText.equals(displayText)) {
        return decision;
      }
    }
    throw new IllegalArgumentException("Unrecognized EmployerCandidateDecision: " + displayText);
  }
}
