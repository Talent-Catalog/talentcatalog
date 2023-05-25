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

import {Opportunity} from "./base";
import {ShortJob} from "./job";

export interface CandidateOpportunity extends Opportunity {

  closingCommentsForCandidate?: string;
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
  durableSolution = "21. Durable solution (permanent residence, normal citizen rights)",
  noJobOffer = "No job offer",
  noVisa = "No visa",
  notFitForRole = "Not fit for role",
  notEligibleForTC = "Not eligible for TC",
  notEligibleForVisa = "Not eligible for visa",
  noInterview = "No interview",
  candidateLeavesDestination = "Candidate leaves destination",
  candidateRejectsOffer = "Candidate rejects offer",
  candidateUnreachable = "Candidate unreachable",
  candidateWithdraws = "Candidate withdraws"

}

