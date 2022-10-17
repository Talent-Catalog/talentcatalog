/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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
import {CandidateSource, SearchCandidateSourcesRequest} from "./base";
import {SavedList} from "./saved-list";

//todo This doesn't extend CandidateSource any more
export interface Job extends CandidateSource {
  tcJobId: number;
  employer: string;
  country: string;
  stage: JobOpportunityStage;
  submissionDueDate: Date;
  submissionList: SavedList;
}

export function isJob(source: CandidateSource): source is Job {
  return source ? 'submissionList' in source : false;
}

/**
 * Note that the string values of this enum MUST match the actual stage names for job
 * opportunities on Salesforce.
 * <p/>
 * See https://docs.google.com/document/d/1B6DmpYaONV_yNmyAqL76cu0TUQcpNgKtOmKELCkpRoc/edit#heading=h.qx7je1tuwoqv
 */
export enum JobOpportunityStage {
  prospect = "Prospect",
  briefing = "Briefing",
  pitching = "Pitching",
  identifyingRoles = "Identifying roles",
  candidateSearch = "Candidate search",
  visaEligibility = "Visa eligibility",
  cvPreparation = "CV preparation",
  cvReview = "CV review",
  recruitmentProcess = "Recruitment process",
  jobOffer = "Job offer",
  visaPreparation = "Visa preparation",
  postHireEngagement = "Post hire engagement",
  hiringCompleted = "Hiring completed",
  employerIneligible = "Employer ineligible",
  noJobOffer = "No job offer",
  noInterest = "No interest",
  noSuitableCandidates = "No suitable candidates",
  noVisa = "No visa"
}

export interface SalesforceJobOpp {
  id: string;
}

export class SearchJobRequest extends SearchCandidateSourcesRequest {
  sfOppClosed?: boolean;
  stages?: string[];
}

export interface UpdateJobRequest {
  sfJoblink: string;
}

