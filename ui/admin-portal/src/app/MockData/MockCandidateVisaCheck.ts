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

import {CandidateVisaJobCheck, TBBEligibilityAssessment, VisaEligibility, YesNo} from "../model/candidate";
import {MockJob} from "./MockJob";

export const MockCandidateVisaJobCheck: CandidateVisaJobCheck = {
  id: 1,
  jobOpp: MockJob,
  occupation: {
    id: 1,
    name: 'Mock Occupation',
    isco08Code: '1234',
    status: 'Active'
  },
  qualification: YesNo.Yes,
  salaryTsmit: YesNo.Yes,
  regional: YesNo.No,
  interest: YesNo.Yes,
  interestNotes: 'Interest notes',
  familyAus: YesNo.No,
  eligible_494: YesNo.Yes,
  eligible_494_Notes: 'Eligible 494 notes',
  eligible_186: YesNo.No,
  eligible_186_Notes: 'Not eligible 186 notes',
  eligibleOther: YesNo.No,
  eligibleOtherNotes: 'Not eligible other notes',
  putForward: VisaEligibility.Yes,
  tbbEligibility: TBBEligibilityAssessment.Proceed,
  notes: 'Mock job notes',
  relevantWorkExp: 'Mock relevant work experience',
  ageRequirement: YesNo.Yes,
  preferredPathways: 'Preferred pathways',
  ineligiblePathways: 'Ineligible pathways',
  eligiblePathways: 'Eligible pathways',
  occupationCategory: 'Occupation category',
  occupationSubCategory: 'Occupation subcategory',
  languagesRequired: 'English, Spanish',
  languagesThresholdMet: YesNo.Yes,
  languagesThresholdNotes: 'Languages threshold met'
};
