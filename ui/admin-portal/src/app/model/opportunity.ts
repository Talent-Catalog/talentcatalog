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
import {
  CandidateOpportunity,
  CandidateOpportunityStage,
  isCandidateOpportunity,
  isOppStageGreaterThanOrEqualTo
} from "./candidate-opportunity";
import {Auditable, HasId} from "./base";
import {isJob, isJobOppStageGreaterThanOrEqualTo, Job, JobOpportunityStage} from "./job";
import {BadgeColor} from "../shared/components/badge/badge.component";

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

export function getStageBadgeColor(opp: Opportunity): BadgeColor {
  //Need to select the appropriate stage's badge color - depending on whether opp is a candidate or
  // job opportunity.
  if (isCandidateOpportunity(opp)) {
    return getCandidateOppStageBadgeColor(opp);
  }
  if (isJob(opp)) {
    return getJobStageBadgeColor(opp);
  }
}

/**
 * Badge colors groups by job opp stages:
 * - Won Stage: Yellow;
 * - Closed Stages: Gray;
 * - Recruiter Stages (stage CV Review onwards, but not closed or won): Orange;
 * - Prospect: Pink;
 * - Other stages (prospect to CV review): Purple;
 * @param opp
 */
function getJobStageBadgeColor(opp: Job): BadgeColor {
  if (opp.won) {
    return "yellow"
  } else if (opp.closed) {
    return "gray"
  } else if (isJobCvReviewStageOrMore(opp.stage)) {
    return "orange"
  } else if (CandidateOpportunityStage[opp.stage] === CandidateOpportunityStage.prospect) {
    return "pink"
  } else {
    return "purple"
  }
}

/**
 * Badge colors groups by candidate opp stages:
 * - Closed Stages: Gray;
 * - Employed Stages (stage 'training' onwards, but not closed): Yellow;
 * - Recruiter Stages (stage CV Review onwards, but not closed or employed): Orange;
 * - Prospect: Pink;
 * - Other stages (prospect to CV review): Purple;
 * @param opp
 */
function getCandidateOppStageBadgeColor(opp: CandidateOpportunity): BadgeColor {
  if (opp.closed) {
    return "gray"
  } else if (isEmployedStage(opp.stage)) {
    return "yellow"
  } else if (isCvReviewStageOrMore(opp.stage)) {
    return "orange"
  } else if (CandidateOpportunityStage[opp.stage] === CandidateOpportunityStage.prospect) {
    return "pink"
  } else {
    return "purple"
  }
}

export function isEmployedStage(stage: string): boolean {
  return isOppStageGreaterThanOrEqualTo(stage, "training")
}

/**
 *  Recruiters only see candidates past the CV Review stage.
 */
export function isCvReviewStageOrMore(stage: string) {
  return isOppStageGreaterThanOrEqualTo(stage, 'cvReview')
}

export function isJobCvReviewStageOrMore(stage: string) {
  return isJobOppStageGreaterThanOrEqualTo(stage, 'cvReview');
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

