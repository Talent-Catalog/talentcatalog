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

import {CandidateDestination, describeFamilyInDestination} from "./candidate-destination";
import {MockCandidate} from "../MockData/MockCandidate";
import {CandidateIntakeData, FamilyRelations, YesNoUnsure} from "./candidate";

fdescribe('CandidateDestination Interface', () => {
  const mockCandidate = new MockCandidate();
  it('should create an object that conforms to the CandidateDestination interface', () => {
    const destination: CandidateDestination = {
      id: 1,
      country: mockCandidate.country,
      candidate: mockCandidate,
      interest: YesNoUnsure.Yes,
      family: FamilyRelations.Other,
      location: 'City X',
      notes: 'Additional notes'
    };

    expect(destination).toBeTruthy();
    expect(destination.id).toBe(1);
    expect(destination.country?.name).toBe('United States');
    expect(destination.interest).toBe(YesNoUnsure.Yes);
    expect(destination.family).toBe(FamilyRelations.Other);
    expect(destination.location).toBe('City X');
    expect(destination.notes).toBe('Additional notes');
  });

  it('should have the correct types for each property', () => {
    const destination: CandidateDestination = {
      id: 1,
      country: mockCandidate.country,
      candidate: mockCandidate,
      interest: YesNoUnsure.Yes,
      family: FamilyRelations.Child,
      location: 'City X',
      notes: 'Additional notes'
    };

    expect(typeof destination.id).toBe('number');
    expect(typeof destination.country).toBe('object'); // Assuming Country is a complex object
    expect(typeof destination.candidate).toBe('object'); // Assuming Candidate is a complex object
    expect(typeof destination.interest).toBe('string');
    expect(typeof destination.family).toBe('string');
    expect(typeof destination.location).toBe('string');
    expect(typeof destination.notes).toBe('string');
  });

  it('should return correct family description when family and location are present', () => {
    const countryId = 1;
    const candidateIntakeData = {
      candidateDestinations: [
        { country: { id: 1 }, family: 'Parents', location: 'City Y' }
      ]
    } as unknown as CandidateIntakeData;

    const result = describeFamilyInDestination(countryId, candidateIntakeData);
    expect(result).toBe('Parents in City Y');
  });


  it('should return correct family description when only family is present', () => {
    const countryId = 1;
    const candidateIntakeData = {
      candidateDestinations: [
        { country: { id: 1 }, family: 'Siblings' }
      ]
    } as unknown as CandidateIntakeData;

    const result = describeFamilyInDestination(countryId, candidateIntakeData);
    expect(result).toBe('Siblings');
  });

  it('should return "No family entered" when family is not present', () => {
    const countryId = 1;
    const candidateIntakeData = {
      candidateDestinations: [
        { country: { id: 1 } }
      ]
    } as unknown as CandidateIntakeData;

    const result = describeFamilyInDestination(countryId, candidateIntakeData);
    expect(result).toBe('No family entered');
  });

  it('should return "No family entered" when destination for countryId is not found', () => {
    const countryId = 1;
    const candidateIntakeData = {
      candidateDestinations: [
        { country: { id: 2 }, family: 'Parents' }
      ]
    } as unknown as CandidateIntakeData;

    const result = describeFamilyInDestination(countryId, candidateIntakeData);
    expect(result).toBe('No family entered');
  });

  it('should return "No family entered" when candidateIntakeData is null or undefined', () => {
    const countryId = 1;
    const candidateIntakeData = null;

    const result = describeFamilyInDestination(countryId, candidateIntakeData);
    expect(result).toBe('No family entered');
  });
});
