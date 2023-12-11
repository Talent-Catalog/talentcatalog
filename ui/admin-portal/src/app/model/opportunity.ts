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
import {CandidateOpportunityStage, isCandidateOpportunity} from "./candidate-opportunity";
import {Auditable, HasId} from "./base";
import {isJob, JobOpportunityStage} from "./job";

/**
 * Given an opportunity (job or candidate), return the string value of the opportunity's stage's
 * name abbreviated for easy display by stripping out any numeric prefix or parenthesized suffix.
 * For example for CandidateOpportunityStage.oneWayReview, strip
 * "7. 1 way review (Optional depending on employer)" down to just "1 way review".
 * @param opp Opportunity
 * @return The string value of the stage (rather than the key). For example,
 *         "CV preparation" instead of "cvPreparation".
 */
export function getOpportunityStageName(opp: Opportunity): string {
  let s: string = null;

  //Pick up the string value associated with the opportunity's stage.
  //Need to select the appropriate stage enum - depending on whether opp is a candidate or job
  //opportunity.
  if (isCandidateOpportunity(opp)) {
    s = CandidateOpportunityStage[opp.stage];
  }
  if (isJob(opp)) {
    s = JobOpportunityStage[opp.stage];
  }
  if (s) {
    //Strip off extra stuff

    //Strip off any prefix - eg '17. '
    let prefixIndex = s.indexOf('. ');
    if (prefixIndex >= 0) {
      s = s.substring(prefixIndex + 2);
    }

    //Strip off any suffix - eg ' (Canada only)'
    let suffixIndex = s.indexOf(' (');
    if (suffixIndex >= 0) {
      s = s.substring(0, suffixIndex);
    }
  }
  return s;
}

export interface OpportunityProgressParams {
  stage?: string;
  nextStep?: string;
  nextStepDueDate?: string;
}

export interface OpportunityIds extends HasId {
  sfId?: string;
}

export interface Opportunity extends Auditable, OpportunityIds {
  closed: boolean;
  closingComments?: string;
  name: string;
  nextStep?: string;
  nextStepDueDate?: Date;
  won: boolean;
}

export enum OpportunityOwnershipType {
  AS_SOURCE_PARTNER,
  AS_JOB_CREATOR
}

