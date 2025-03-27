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
import {PagedSearchRequest} from "./base";
import {ShortJob} from "./job";
import {ShortCandidate} from "./candidate";
import {Opportunity, OpportunityOwnershipType} from "./opportunity";
import {getOrdinal} from "../util/enum";

export function isCandidateOpportunity(opp: Opportunity): opp is CandidateOpportunity {
  return opp ? 'jobOpp' in opp : false;
}

export interface CandidateOpportunity extends Opportunity {

  closingCommentsForCandidate?: string;
  employerFeedback?: string;
  fileOfferLink?: string;
  fileOfferName?: string;

  candidate: ShortCandidate;
  jobOpp: ShortJob;
  stage: CandidateOpportunityStage;
  relocatingDependantIds?: number[];
}

export enum CandidateOpportunityStage {
  prospect = "0. Prospect",
  miniIntake = "1. Mini intake",
  fullIntake = "2. Full intake",
  visaEligibility = "3. Visa eligibility",
  cvPreparation = "4. CV preparation",
  cvReview = "5. CV review",
  oneWayPreparation = "6. 1 way preparation (Optional depending on employer)",
  oneWayReview = "7. 1 way review (Optional depending on employer)",
  testPreparation = "8. Test preparation  (Optional depending on employer)",
  testing = "9. Testing (Optional depending on employer)",
  twoWayPreparation = "10. 2 way interview preparation",
  twoWayReview = "11. 2 way interview review",
  offer = "12. Offer (employer is preparing written offer)",
  acceptance = "13. Acceptance (informed decision making as candidate considers offer(s))",
  training = "14. Training",
  provincialVisaPreparation = "15. Provincial visa preparation (Canada only)",
  provincialVisaProcessing = "16. Provincial visa processing (Canada only)",
  visaPreparation = "17. Visa preparation",
  visaProcessing = "18. Visa processing",
  relocating = "19. Relocating",
  relocated = "20. Relocated (candidate has arrived at employer's location)",
  settled = "21. Settled (candidate indicates no further need for support)",
  durableSolution = "22 Closed won. Durable solution (permanent residence, normal citizen rights)",
  noJobOffer = "Closed. No job offer",
  noVisa = "Closed. No visa",
  notFitForRole = "Closed. Not fit for role",
  notEligibleForTC = "Closed. Not eligible for TC",
  notEligibleForVisa = "Closed. Not eligible for visa",
  noInterview = "Closed. No interview",
  candidateLeavesDestination = "Closed. Candidate leaves destination",
  candidateMistakenProspect = "Closed. Candidate was mistakenly proposed as a prospect for the job",
  candidateRejectsOffer = "Closed. Candidate rejects offer",
  candidateUnreachable = "Closed. Candidate unreachable",
  candidateWithdraws = "Closed. Candidate withdraws",
  jobIneligible = "Closed. Job ineligible",
  jobOfferRetracted = "Closed. Job offer retracted",
  jobWithdrawn = "Closed. Job withdrawn",
  relocatedNoJobOfferPathway = "Closed. No job offer stream (Canadian pathway stream - based on skills not job offer)",
  trainingNotCompleted = "Closed. Training not completed",
  programSuspended = "Closed. Program suspended",
}

/**
 * Base class for both Job opportunity and candidate opportunity requests
 */
export class SearchOpportunityRequest extends PagedSearchRequest {
  activeStages?: boolean;
  keyword?: string;
  overdue?: boolean;
  ownershipType?: OpportunityOwnershipType;
  ownedByMe?: boolean;
  ownedByMyPartner?: boolean;
  published?: boolean;
  sfOppClosed?: boolean;
  stages?: string[];
  destinationIds?: number[];
  withUnreadMessages?: boolean;
}

export function isOppStageGreaterThanOrEqualTo(selectedOppStageKey: string, desiredStageKey: string) {
  let oppOrdinal: number = getOrdinal(CandidateOpportunityStage, selectedOppStageKey);
  let desiredOrdinal: number = getOrdinal(CandidateOpportunityStage, desiredStageKey);
  return oppOrdinal >= desiredOrdinal;
}


