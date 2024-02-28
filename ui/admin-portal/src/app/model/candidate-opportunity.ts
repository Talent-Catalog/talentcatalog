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
  provincialVisaPreparation = "14. Provincial visa preparation (Canada only)",
  provincialVisaProcessing = "15. Provincial visa processing (Canada only)",
  visaPreparation = "16. Visa preparation",
  visaProcessing = "17. Visa processing",
  relocating = "18. Relocating",
  relocated = "19. Relocated (candidate has arrived at employer's location)",
  settled = "20. Settled (candidate indicates no further need for support)",
  durableSolution = "21 Closed won. Durable solution (permanent residence, normal citizen rights)",
  noJobOffer = "Closed. No job offer",
  noVisa = "Closed. No visa",
  notFitForRole = "Closed. Not fit for role",
  notEligibleForTC = "Closed. Not eligible for TC",
  notEligibleForVisa = "Closed. Not eligible for visa",
  noInterview = "Closed. No interview",
  candidateLeavesDestination = "Closed. Candidate leaves destination",
  candidateRejectsOffer = "Closed. Candidate rejects offer",
  candidateUnreachable = "Closed. Candidate unreachable",
  candidateWithdraws = "Closed. Candidate withdraws",
  jobOfferRetracted = "Closed. Job offer retracted",
  relocatedNoJobOfferPathway = "Closed. No job offer pathway (Canadian pathway - based on skills not job offer)"
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
  sfOppClosed?: boolean;
  stages?: string[];
  destinationIds?: number[];
  withUnreadMessages?: boolean;
}


