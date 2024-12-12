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

import {ShortCandidate} from "./candidate";
import {ShortJob} from "./job";
import {
  CandidateOpportunity,
  CandidateOpportunityStage,
  isCandidateOpportunity, isOppStageGreaterThanOrEqualTo
} from "./candidate-opportunity";
import {Opportunity} from "./opportunity";

describe('CandidateOpportunity Model', () => {
  let candidate: ShortCandidate;
  let jobOpp: ShortJob;
  let candidateOpportunity: CandidateOpportunity;

  beforeEach(() => {
    candidate = { id: 1, candidateNumber: '21221' } as ShortCandidate;
    jobOpp = { id: 1, name:'Job' ,country: {
      id:1,name:'USA',status:'active',translatedName:'United Stated'
      }, submissionList:{
      id:1, name:'ShortSavedList'
      }, jobCreator:{
      id:1,name:'Ahmad',abbreviation:'Ahm',websiteUrl:'URL'
    }};

    candidateOpportunity = {
      closed: false, name: "", won: false,
      id: 1,
      candidate: candidate,
      jobOpp: jobOpp,
      stage: CandidateOpportunityStage.prospect,
      closingCommentsForCandidate: 'Good progress',
      employerFeedback: 'Positive',
      fileOfferLink: 'http://example.com/offer',
      fileOfferName: 'Offer.pdf'
    };
  });

  it('should create an instance of CandidateOpportunity', () => {
    expect(candidateOpportunity).toBeTruthy();
  });

  it('should have the correct candidate', () => {
    expect(candidateOpportunity.candidate).toEqual(candidate);
  });

  it('should have the correct job opportunity', () => {
    expect(candidateOpportunity.jobOpp).toEqual(jobOpp);
  });

  it('should have the correct stage', () => {
    expect(candidateOpportunity.stage).toBe(CandidateOpportunityStage.prospect);
  });

  it('should have the correct closing comments for candidate', () => {
    expect(candidateOpportunity.closingCommentsForCandidate).toBe('Good progress');
  });

  it('should have the correct employer feedback', () => {
    expect(candidateOpportunity.employerFeedback).toBe('Positive');
  });

  it('should have the correct file offer link', () => {
    expect(candidateOpportunity.fileOfferLink).toBe('http://example.com/offer');
  });

  it('should have the correct file offer name', () => {
    expect(candidateOpportunity.fileOfferName).toBe('Offer.pdf');
  });
});

describe('isCandidateOpportunity Function', () => {
  let candidateOpportunity: CandidateOpportunity;

  beforeEach(() => {
    candidateOpportunity = {
      closed: false, name: "", won: false,
      id: 1,
      candidate : { id: 1, candidateNumber: '21221' } as ShortCandidate,
      jobOpp : { id: 1, name:'Job' ,country: {
          id:1,name:'USA',status:'active',translatedName:'United Stated'
        }, submissionList:{
          id:1, name:'ShortSavedList'
        }, jobCreator:{
          id:1,name:'Ahmad',abbreviation:'Ahm',websiteUrl:'URL'
        }},
      stage: CandidateOpportunityStage.prospect
    };
  });

  it('should return true for CandidateOpportunity objects', () => {
    expect(isCandidateOpportunity(candidateOpportunity)).toBeTrue();
  });

  it('should return false for non-CandidateOpportunity objects', () => {
    const nonCandidateOpportunity: Opportunity = {
      id: 2,
      closed:false,
      closingComments: 'Non-Candidate Opportunity'
    } as Opportunity;
    expect(isCandidateOpportunity(nonCandidateOpportunity)).toBeFalse();
  });
});

describe('isOppStageGreaterThanOrEqualTo Function', () => {
  it('should return true if the selected stage is greater than or equal to the desired stage', () => {
    expect(isOppStageGreaterThanOrEqualTo(CandidateOpportunityStage.fullIntake, CandidateOpportunityStage.prospect)).toBeTrue();
    expect(isOppStageGreaterThanOrEqualTo(CandidateOpportunityStage.visaProcessing, CandidateOpportunityStage.visaEligibility)).toBeTrue();
  });

  it('should return true if the selected stage is the same as the desired stage', () => {
    expect(isOppStageGreaterThanOrEqualTo(CandidateOpportunityStage.prospect, CandidateOpportunityStage.prospect)).toBeTrue();
    expect(isOppStageGreaterThanOrEqualTo(CandidateOpportunityStage.visaProcessing, CandidateOpportunityStage.visaProcessing)).toBeTrue();
  });
});
