/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
  TBBEligibilityAssessment,
  VisaEligibility,
  YesNo,
  YesNoUnsure
} from "../model/candidate";
import {MockUser} from "./MockUser";
import {MockJob} from "./MockJob";

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
  visaEligibilityAssessment: YesNo.Yes,
  pathwayAssessment: YesNoUnsure.Unsure,
  pathwayAssessmentNotes: 'Not sure about the pathway',
  candidateVisaJobChecks: [
    {
      id: 1,
      name: 'Job title',
      occupation: { id: 1, name: 'Software Engineer', isco08Code: '123', status: 'Active' },
      qualification: YesNo.Yes,
      qualificationNotes: 'Bachelor\'s degree in Computer Science',
      salaryTsmit: YesNo.Yes,
      regional: YesNo.No,
      interest: YesNo.Yes,
      interestNotes: 'Interested in the job',
      familyAus: YesNo.No,
      eligible_494: YesNo.No,
      eligible_494_Notes: 'Not eligible for subclass 494',
      eligible_186: YesNo.Yes,
      eligible_186_Notes: 'Eligible for subclass 186',
      eligibleOther: YesNo.No,
      eligibleOtherNotes: 'Not eligible for other visas',
      putForward: VisaEligibility.Yes,
      tbbEligibility: TBBEligibilityAssessment.Proceed,
      notes: 'Notes about the job check',
      relevantWorkExp: '3 years of relevant work experience',
      ageRequirement: YesNo.Yes,
      preferredPathways: 'Preferred pathways for visa application',
      ineligiblePathways: 'Ineligible pathways for visa application',
      eligiblePathways: 'Eligible pathways for visa application',
      occupationCategory: 'IT',
      occupationSubCategory: 'Software Development',
      languagesRequired: 'English',
      languagesThresholdMet: YesNo.Yes,
      languagesThresholdNotes: 'Met English language threshold',
      relocatingDependantIds: [1, 2, 3]
    }
  ]
};
