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

package org.tbbtalent.server.request.candidate;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.CandidateOpportunityStage;

/**
 * Opportunity parameters - common to candidate opportunities and job opportunities
 * <p/>
 * These can be set by a user on the admin portal - from Angular
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class SalesforceOppParams {

  /**
   * todo This is candidate opp specific
   * Must match the name of a Salesforce Candidate Opportunity stage
   */
  @Nullable
  private CandidateOpportunityStage stage;

  /**
   * Any text which will update a Salesforce Opportunity next step
   */
  @Nullable
  private String nextStep;

  /**
   * Any text which will update a Salesforce Opportunity next step due date
   */
  @Nullable
  private LocalDate nextStepDueDate;

  /**
   * Comments explaining why the opportunity was closed
   */
  @Nullable
  private String closingComments;

  /**
   * todo This is candidate opp specific
   * Employer feedback on a candidate
   */
  @Nullable
  private String employerFeedback;
}
