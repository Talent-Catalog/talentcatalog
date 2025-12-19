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

// Mock implementation for ShortCandidate
import {ShortCandidate} from "../model/candidate";
import {ShortJob} from "../model/job";
import {CandidateOpportunity, CandidateOpportunityStage} from "../model/candidate-opportunity";

const mockShortCandidate: ShortCandidate = {
  id: 1,
  candidateNumber: 'C12345',
  user: {
    username: 'testuser',
    firstName: 'John',
    lastName: 'Doe',
    email: 'john.doe@example.com',
    partner: {
      id: 1,
      name: 'Example Partner',
      abbreviation: 'EP',
      websiteUrl: 'http://example.com'
    }
  }
};

// Mock implementation for ShortJob
const mockShortJob: ShortJob = {
  id: 1,
  name: 'Example Job',
  country: {
    id: 1,
    name: 'Country',
    status: 'Active',
    translatedName: 'Translated Country'
  },
  submissionList: {
    id: 1,
    name: 'Submission List'
  },
  jobCreator: {
    id: 1,
    name: 'Job Creator',
    abbreviation: 'JC',
    websiteUrl: 'http://jobcreator.com'
  }
};

// Additional mock implementation of short job for methods that work with candidate opps array
const mockShortJob2: ShortJob = {
  id: 2,
  name: 'Example Job 2',
  country: {
    id: 2,
    name: 'Country',
    status: 'Active',
    translatedName: 'Translated Country'
  },
  submissionList: {
    id: 2,
    name: 'Submission List'
  },
  jobCreator: {
    id: 2,
    name: 'Job Creator Extraordinaire',
    abbreviation: 'JCE',
    websiteUrl: 'http://jobcreatorextraordinaire.com'
  }
};

// Mock implementation for CandidateOpportunityStage
const mockCandidateOpportunityStage: CandidateOpportunityStage = CandidateOpportunityStage.prospect;

// Create a mock CandidateOpportunity object by extending Opportunity and adding additional properties
const mockCandidateOpportunity: CandidateOpportunity = {
  closingCommentsForCandidate: 'Mock closing comments for candidate',
  employerFeedback: 'Mock employer feedback',
  fileOfferLink: 'http://example.com/offer',
  fileOfferName: 'Offer.pdf',
  candidate: mockShortCandidate,
  jobOpp: mockShortJob,
  stage: mockCandidateOpportunityStage,
  closed: false,
  closingComments: 'Mock closing comments',
  name: 'Mock Opportunity',
  nextStep: 'Mock next step',
  nextStepDueDate: new Date(),
  won: false,
  relocatingDependantIds: [1, 2, 3],
};

// Additional mock implementation of candidate opp for methods that work with candidate opps array
const mockCandidateOpportunity2: CandidateOpportunity = {
  closingCommentsForCandidate: 'Mock 2 closing comments for candidate',
  employerFeedback: 'Mock 2 employer feedback',
  fileOfferLink: 'http://example.com/offer',
  fileOfferName: 'Offer.pdf',
  candidate: mockShortCandidate,
  jobOpp: mockShortJob2,
  stage: mockCandidateOpportunityStage,
  closed: false,
  closingComments: 'Mock 2 closing comments',
  name: 'Mock 2 Opportunity',
  nextStep: 'Mock 2 next step',
  nextStepDueDate: new Date(),
  won: false,
  relocatingDependantIds: [1, 2, 3],
};

export { mockCandidateOpportunity, mockCandidateOpportunity2, mockShortJob, mockShortJob2 };
