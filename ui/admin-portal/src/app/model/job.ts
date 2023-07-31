/*
 * Copyright = c) 2022 Talent Beyond Boundarie.
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
import {SavedList, ShortSavedList} from "./saved-list";
import {User} from "./user";
import {ShortPartner} from "./partner";
import {SavedSearch} from "./saved-search";
import {Router} from "@angular/router";
import {Location} from "@angular/common";
import {getExternalHref} from "../util/url";
import {JobOppIntake} from "./job-opp-intake";
import {SearchOpportunityRequest} from "./candidate-opportunity";
import {Opportunity, OpportunityProgressParams} from "./opportunity";

export function isJob(opp: Opportunity): opp is Job {
  return opp ? 'submissionList' in opp : false;
}

export interface ShortJob {
  id: number,
  name: string;
  country?: string;
  submissionList?: ShortSavedList;
  recruiterPartner?: ShortPartner;
}

export interface Job extends Opportunity {
  employerWebsite: string;
  employerHiredInternationally: boolean;
  hiringCommitment: string;
  opportunityScore: string;
  employerDescription: string;
  contactUser: User;
  // Note: this country field comes from Salesforce, why it is a string and not a country object.
  country: string;
  employer: string;
  exclusionList: SavedList;
  jobSummary: string;
  publishedBy: User;
  publishedDate: Date;
  jobCreator: ShortPartner;
  stage: JobOpportunityStage;
  starringUsers: User[];
  submissionDueDate: Date;
  submissionList: SavedList;
  suggestedList: SavedList;
  suggestedSearches: SavedSearch[];
  jobOppIntake: JobOppIntake;
}

export function getJobExternalHref(router: Router, location: Location, job: Job): string {
  return getExternalHref(router, location, ['job', job.id]);
}

export type JobDocType = "jd" | "joi";

/**
 * Note that the string values of this enum MUST match the actual stage names for job
 * opportunities on Salesforce.
 * <p/>
 * See https://docs.google.com/document/d/1B6DmpYaONV_yNmyAqL76cu0TUQcpNgKtOmKELCkpRoc/edit#heading=h.qx7je1tuwoqv
 */
export enum JobOpportunityStage {
  prospect = "0. Prospect",
  briefing = "1. Briefing",
  pitching = "2. Pitching",
  identifyingRoles = "3. Identifying roles",
  candidateSearch = "4. Candidate search",
  visaEligibility = "5. Visa eligibility",
  cvPreparation = "6. CV preparation",
  cvReview = "7. CV review",
  recruitmentProcess = "8. Recruitment process",
  jobOffer = "9. Job offer",
  visaPreparation = "10. Visa preparation",
  postHireEngagement = "11. Post hire engagement",
  hiringCompleted = "12 Closed won. Hiring completed",
  ineligibleEmployer = "Closed. Ineligible employer",
  ineligibleOccupation = "Closed. Ineligible occupation",
  ineligibleRegion = "Closed. Ineligible region",
  noInterest = "Closed. No interest",
  noJobOffer = "Closed. No job offer",
  noPrPathway = "Closed. No PR pathway",
  noSuitableCandidates = "Closed. No suitable candidates",
  noVisa = "Closed. No visa",
  tooExpensive = "Closed. Too expensive",
  tooHighWage = "Closed. Too high wage",
  tooLong = "Closed. Too long"
}

/**
 * Adds extra job opportunity specific fields to standard SearchOpportunityRequest
 */
export class SearchJobRequest extends SearchOpportunityRequest {
  published?: boolean;
  starred?: boolean;
}

export interface UpdateJobRequest extends OpportunityProgressParams {
  contactUserId?: number;
  sfJoblink?: string;
  submissionDueDate?: Date;
}

