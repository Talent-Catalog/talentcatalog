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
  CandidateVisa,
  FamilyRelations,
  TBBEligibilityAssessment,
  YesNo,
  YesNoUnsure
} from "../model/candidate";
import {MockUser} from "./MockUser";
import {MockJob} from "./MockJob";
import {MockCandidateVisaJobCheck} from "./MockCandidateVisaCheck";

const mockUser = new MockUser();
export const MockCandidateVisa: CandidateVisa = {
  id: 1,
  country: MockJob.country,
  protection: YesNo.Yes,
  protectionGrounds: 'Political persecution',
  tbbEligibilityAssessment: TBBEligibilityAssessment.Proceed,
  englishThreshold: YesNo.Yes,
  englishThresholdNotes: 'IELTS score: 7.5',
  healthAssessment: YesNo.Yes,
  healthAssessmentNotes: 'No major health issues',
  characterAssessment: YesNo.Yes,
  characterAssessmentNotes: 'No criminal record',
  securityRisk: YesNo.No,
  securityRiskNotes: 'No security issues',
  validTravelDocs: YesNo.Yes,
  validTravelDocsNotes: 'Passport valid until 2025',
  overallRisk: 'Low',
  overallRiskNotes: 'Overall risk is low',
  intProtection: 'No',
  createdBy: mockUser,
  createdDate: 2012,
  updatedBy: mockUser,
  updatedDate: 2017,
  pathwayAssessment: YesNoUnsure.Unsure,
  pathwayAssessmentNotes: 'Not sure about the pathway',
  destinationFamily: FamilyRelations.Child,
  destinationFamilyLocation:'USA',
  candidateVisaJobChecks: [MockCandidateVisaJobCheck]
};
