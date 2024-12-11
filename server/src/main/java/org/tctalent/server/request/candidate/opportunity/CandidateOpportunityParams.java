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

package org.tctalent.server.request.candidate.opportunity;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.CandidateOpportunityStage;

/**
 * Candidate Opportunity parameters
 * <p/>
 * These can be set by a user on the admin portal - from Angular
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString(callSuper = true)
public class CandidateOpportunityParams extends OpportunityParams {

  /**
   * Salesforce Candidate Opportunity stage
   */
  @Nullable
  private CandidateOpportunityStage stage;

  /**
   * Comments explaining why the opportunity was closed intended to be
   * shared with candidate
   */
  @Nullable
  private String closingCommentsForCandidate;

  /**
   * Employer feedback on a candidate
   */
  @Nullable
  private String employerFeedback;

  /**
   * Relocation information for SF candidate opportunity update - gender and age breakdown of
   * all relocating individuals including the candidate. Dependants are recorded at intake and then
   * confirmed as relocating at visa assessment.
   */
  @Nullable
  private Map<String, Integer> relocationInfo;
}
