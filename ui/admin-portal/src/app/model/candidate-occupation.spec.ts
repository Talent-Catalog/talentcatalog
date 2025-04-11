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

import {User} from "./user";
import {Occupation} from "./occupation";
import {CandidateOccupation} from "./candidate-occupation";

describe('CandidateOccupation Model', () => {
  let user1: User;
  let user2: User;
  let occupation: Occupation;
  let candidateOccupation: CandidateOccupation;

  beforeEach(() => {
    user1 = { id: 1, name: 'John Doe', email: 'john.doe@example.com' } as User;
    user2 = { id: 2, name: 'Jane Smith', email: 'jane.smith@example.com' }  as User;
    occupation = { id: 1, name: 'Software Engineer', isco08Code: '1234', status: 'Active' };
    candidateOccupation = {
      id: 1,
      occupation: occupation,
      yearsExperience: 5,
      migrationOccupation: 'Migration Specialist',
      createdBy: user1,
      createdDate: Date.now(),
      updatedBy: user2,
      updatedDate: Date.now()
    };
  });

  it('should create an instance of CandidateOccupation', () => {
    expect(candidateOccupation).toBeTruthy();
  });

  it('should have the correct id', () => {
    expect(candidateOccupation.id).toBe(1);
  });

  it('should have the correct occupation', () => {
    expect(candidateOccupation.occupation).toEqual(occupation);
  });

  it('should have the correct years of experience', () => {
    expect(candidateOccupation.yearsExperience).toBe(5);
  });

  it('should have the correct migration occupation', () => {
    expect(candidateOccupation.migrationOccupation).toBe('Migration Specialist');
  });

  it('should have the correct createdBy user', () => {
    expect(candidateOccupation.createdBy).toEqual(user1);
  });

  it('should have the correct createdDate', () => {
    expect(candidateOccupation.createdDate).toBeTruthy();
  });

  it('should have the correct updatedBy user', () => {
    expect(candidateOccupation.updatedBy).toEqual(user2);
  });

  it('should have the correct updatedDate', () => {
    expect(candidateOccupation.updatedDate).toBeTruthy();
  });
});
