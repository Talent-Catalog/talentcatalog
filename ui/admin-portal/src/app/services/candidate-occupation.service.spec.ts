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

import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {CandidateOccupationService} from './candidate-occupation.service';
import {Occupation} from '../model/occupation';
import {CandidateOccupation} from '../model/candidate-occupation';
import {environment} from '../../environments/environment';
import {User} from '../model/user';
import {MockUser} from "../MockData/MockUser";

describe('CandidateOccupationService', () => {
  let service: CandidateOccupationService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/candidate-occupation';
  const mockUser = new MockUser();
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CandidateOccupationService]
    });
    service = TestBed.inject(CandidateOccupationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should retrieve a list of occupations', () => {
    const mockOccupations: Occupation[] = [
      { id: 1, name: 'Software Developer', isco08Code: '2512', status: 'Active' },
      { id: 2, name: 'Data Scientist', isco08Code: '2521', status: 'Active' }
    ];

    service.listOccupations().subscribe((occupations) => {
      expect(occupations.length).toBe(2);
      expect(occupations).toEqual(mockOccupations);
    });

    const req = httpMock.expectOne(`${apiUrl}/occupation`);
    expect(req.request.method).toBe('GET');
    req.flush(mockOccupations);
  });

  it('should retrieve a list of candidate occupations', () => {
    const mockCandidateOccupations: CandidateOccupation[] = [
      { id: 1, occupation: { id: 1, name: 'Software Developer', isco08Code: '2512', status: 'Active' }, yearsExperience: 5, migrationOccupation: 'Developer', createdBy: mockUser, createdDate: Date.now(), updatedBy: mockUser, updatedDate: Date.now() },
      { id: 2, occupation: { id: 2, name: 'Data Scientist', isco08Code: '2521', status: 'Active' }, yearsExperience: 3, migrationOccupation: 'Scientist', createdBy: mockUser, createdDate: Date.now(), updatedBy: mockUser, updatedDate: Date.now() }
    ];

    service.get(1).subscribe((candidateOccupations) => {
      expect(candidateOccupations.length).toBe(2);
      expect(candidateOccupations).toEqual(mockCandidateOccupations);
    });

    const req = httpMock.expectOne(`${apiUrl}/1/list`);
    expect(req.request.method).toBe('GET');
    req.flush(mockCandidateOccupations);
  });

  it('should update a candidate occupation', () => {
    const updatedOccupation: CandidateOccupation = {
      id: 1,
      occupation: { id: 1, name: 'Software Developer', isco08Code: '2512', status: 'Active' },
      yearsExperience: 6,
      migrationOccupation: 'Senior Developer',
      createdBy: mockUser,
      createdDate: Date.now(),
      updatedBy: mockUser,
      updatedDate: Date.now()
    };

    service.update(1, updatedOccupation).subscribe((occupation) => {
      expect(occupation).toEqual(updatedOccupation);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updatedOccupation);
    req.flush(updatedOccupation);
  });

  it('should create a new candidate occupation', () => {
    const newOccupation: CandidateOccupation = {
      id: 3,
      occupation: { id: 3, name: 'Business Analyst', isco08Code: '2411', status: 'Active' },
      yearsExperience: 2,
      migrationOccupation: 'Analyst',
      createdBy: mockUser,
      createdDate: Date.now(),
      updatedBy: mockUser,
      updatedDate: Date.now()
    };

    service.create(3, newOccupation).subscribe((occupation) => {
      expect(occupation).toEqual(newOccupation);
    });

    const req = httpMock.expectOne(`${apiUrl}/3`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newOccupation);
    req.flush(newOccupation);
  });

  it('should delete a candidate occupation', () => {
    const mockOccupation: CandidateOccupation = {
      id: 1,
      occupation: { id: 1, name: 'Software Developer', isco08Code: '2512', status: 'Active' },
      yearsExperience: 5,
      migrationOccupation: 'Developer',
      createdBy: mockUser,
      createdDate: Date.now(),
      updatedBy: mockUser,
      updatedDate: Date.now()
    };

    service.delete(1).subscribe((occupation) => {
      expect(occupation).toEqual(mockOccupation);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(mockOccupation);
  });
});
