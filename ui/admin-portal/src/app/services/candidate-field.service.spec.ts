/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

import {TestBed} from '@angular/core/testing';
import {DatePipe, TitleCasePipe} from '@angular/common';
import {CandidateFieldService} from './candidate-field.service';
import {AuthorizationService} from './authorization.service';
import {Status} from '../model/base';
import {Candidate} from '../model/candidate';
import {TaskAssignment} from '../model/task-assignment';

describe('CandidateFieldService', () => {
  let service: CandidateFieldService;
  let authService: jasmine.SpyObj<AuthorizationService>;
  let datePipe: DatePipe;
  let titleCasePipe: TitleCasePipe;

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthorizationService', ['canViewCandidateName', 'canViewCandidateCountry', 'isAnAdmin']);
    const datePipeSpy = new DatePipe('en-US');
    const titleCasePipeSpy = new TitleCasePipe();

    TestBed.configureTestingModule({
      providers: [
        CandidateFieldService,
        { provide: AuthorizationService, useValue: authServiceSpy },
        { provide: DatePipe, useValue: datePipeSpy },
        { provide: TitleCasePipe, useValue: titleCasePipeSpy }
      ]
    });

    service = TestBed.inject(CandidateFieldService);
    authService = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    datePipe = TestBed.inject(DatePipe);
    titleCasePipe = TestBed.inject(TitleCasePipe);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('get defaultDisplayableFieldsLong', () => {
    it('should return the default fields in long format', () => {
      const fields = service.defaultDisplayableFieldsLong;
      expect(fields.length).toBe(5); // Check the length of default fields
      expect(fields[0].fieldPath).toBe('status');
    });
  });

  describe('get defaultDisplayableFieldsShort', () => {
    it('should return the default fields in short format', () => {
      const fields = service.defaultDisplayableFieldsShort;
      expect(fields.length).toBe(2); // Check the length of default fields
      expect(fields[0].fieldPath).toBe('user.partner.abbreviation');
    });
  });

  describe('get displayableFieldsMap', () => {
    it('should return a map of displayable fields', () => {
      const fieldsMap = service.displayableFieldsMap;
      expect(fieldsMap.size).toBeGreaterThan(0);
    });
  });


  describe('getFieldsFromPaths', () => {
    it('should return fields based on provided paths', () => {
      const paths = ['gender'];
      const fields = service.getFieldsFromPaths(paths);
      expect(fields.length).toBe(1);
      expect(fields[0].fieldPath).toBe('gender');
    });
  });

  describe('isCandidateNameViewable', () => {
    it('should return true if candidate name is viewable', () => {
      authService.canViewCandidateName.and.returnValue(true);
      expect(service.isCandidateNameViewable()).toBe(true);
    });

    it('should return false if candidate name is not viewable', () => {
      authService.canViewCandidateName.and.returnValue(false);
      expect(service.isCandidateNameViewable()).toBe(false);
    });
  });

  describe('isCountryViewable', () => {
    it('should return true if country is viewable', () => {
      authService.canViewCandidateCountry.and.returnValue(true);
      expect(service.isCountryViewable()).toBe(true);
    });

    it('should return false if country is not viewable', () => {
      authService.canViewCandidateCountry.and.returnValue(false);
      expect(service.isCountryViewable()).toBe(false);
    });
  });

  describe('isAnAdmin', () => {
    it('should return true if the user is an admin', () => {
      authService.isAnAdmin.and.returnValue(true);
      expect(service.isAnAdmin()).toBe(true);
    });

    it('should return false if the user is not an admin', () => {
      authService.isAnAdmin.and.returnValue(false);
      expect(service.isAnAdmin()).toBe(false);
    });
  });

  describe('getIeltsScore', () => {
    it('should format IELTS score correctly', () => {
      const candidate: Candidate = { ieltsScore: '7.5' } as Candidate;
      spyOn(service, 'getIeltsScore').and.returnValue('7.5 (Gen)');
      expect(service.getIeltsScore(candidate)).toBe('7.5 (Gen)');
    });
  });

  describe('getIntakesCompleted', () => {
    it('should return the correct intake status', () => {
      const candidate: Candidate = { fullIntakeCompletedDate: 2012, miniIntakeCompletedDate: null } as Candidate;
      expect(service.getIntakesCompleted(candidate)).toBe('Full *no mini');
    });
  });

  describe('getTasksStatus', () => {
    it('should return the correct tasks status', () => {
      const tasks: TaskAssignment[] = [{ status: Status.active }] as TaskAssignment[];
      spyOn(service, 'getTasksStatus').and.returnValue('Overdue');
      expect(service.getTasksStatus(tasks)).toBe('Overdue');
    });
  });
});
