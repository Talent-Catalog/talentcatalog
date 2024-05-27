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
  won: false
};

export { mockCandidateOpportunity };
