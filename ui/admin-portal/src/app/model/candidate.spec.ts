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

import {TestBed} from "@angular/core/testing";
import {
  Candidate,
  CandidateExam,
  CandidateVisa,
  checkIeltsScoreType,
  describeFamilyInDestination,
  Exam,
  getCandidateExternalHref,
  getCandidateNavigation,
  hasIeltsExam
} from "./candidate";
import {Router} from "@angular/router";
import {Location} from "@angular/common";

describe('Candidate Utility Functions', () => {

  let router: Router;
  let location: Location;

  beforeEach(() => {
    router = {
      createUrlTree: jasmine.createSpy('createUrlTree').and.returnValue({}),
      serializeUrl: jasmine.createSpy('serializeUrl').and.returnValue('/candidate/12345')
    } as any;

    location = {
      prepareExternalUrl: jasmine.createSpy('prepareExternalUrl').and.returnValue('/candidate/12345')
    } as any;

    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: router },
        { provide: Location, useValue: location }
      ]
    });

    router = TestBed.inject(Router);
    location = TestBed.inject(Location);
  });

  describe('getCandidateNavigation', () => {
    it('should return the correct navigation path', () => {
      const candidate: Candidate = { candidateNumber: '12345' } as Candidate;
      const result = getCandidateNavigation(candidate);
      expect(result).toEqual(['candidate', '12345']);
    });
  });

  describe('getCandidateExternalHref', () => {
    it('should return the correct external href', () => {
      const candidate: Candidate = { candidateNumber: '12345' } as Candidate;
      const result = getCandidateExternalHref(router, location, candidate);
      expect(result).toContain('/candidate/12345');
    });
  });

  describe('hasIeltsExam', () => {
    it('should return true if the candidate has an IELTS exam', () => {
      const candidate: Candidate = { candidateExams: [{exam: 'IELTSGen'}] as unknown as CandidateExam[] } as Candidate;
      const result = hasIeltsExam(candidate);
      expect(result).toBe(true);
    });

    it('should return false if the candidate does not have an IELTS exam', () => {
      const candidate: Candidate = { candidateExams: [{ exam: Exam.TOEFL }] as CandidateExam[] } as Candidate;
      const result = hasIeltsExam(candidate);
      expect(result).toBe(false);
    });

    it('should return false if the candidate has no exams', () => {
      const candidate: Candidate = { candidateExams: [] } as Candidate;
      const result = hasIeltsExam(candidate);
      expect(result).toBe(false);
    });
  });

  describe('checkIeltsScoreType', () => {
    it('should return the correct exam type for the candidate', () => {
      const candidate: Candidate = {
        candidateExams: [{ exam: Exam.IELTSGen, score: '7' }] as CandidateExam[],
        ieltsScore: '7'
      } as Candidate;
      const result = checkIeltsScoreType(candidate);
      expect(result).toEqual(Exam.IELTSGen);
    });

    it('should return null if no matching exam is found', () => {
      const candidate: Candidate = {
        candidateExams: [{ exam: Exam.TOEFL, score: '100' }] as CandidateExam[],
        ieltsScore: '7'
      } as Candidate;
      const result = checkIeltsScoreType(candidate);
      expect(result).toBeNull();
    });

    it('should return undefined if the candidate has no exams', () => {
      const candidate: Candidate = {
        candidateExams: [],
        ieltsScore: '7'
      } as Candidate;
      const result = checkIeltsScoreType(candidate);
      expect(result).toBeUndefined();
    });
  });
  it('should return correct family description when family and location are present', () => {
    const candidateVisaCheck = {
      destinationFamily: 'Parents', destinationFamilyLocation: 'City Y'
    } as unknown as CandidateVisa;

    const result = describeFamilyInDestination(candidateVisaCheck);
    expect(result).toBe('Parents in City Y');
  });


  it('should return correct family description when only family is present', () => {
    const candidateVisaCheck = {
      destinationFamily: 'Siblings'
    } as unknown as CandidateVisa;
    const result = describeFamilyInDestination(candidateVisaCheck);
    expect(result).toBe('Siblings');
  });

  it('should return "No family entered" when family is not present', () => {
    const candidateVisaCheck = {
    } as unknown as CandidateVisa;

    const result = describeFamilyInDestination(candidateVisaCheck);
    expect(result).toBe('No family entered');
  });

  it('should return "No family entered" when candidateIntakeData is null or undefined', () => {
    const candidateVisaCheck = null;

    const result = describeFamilyInDestination(candidateVisaCheck);
    expect(result).toBe('No family entered');
  });

});
