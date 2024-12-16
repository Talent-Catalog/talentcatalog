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

import {CandidateDestination} from "./candidate-destination";
import {MockCandidate} from "../MockData/MockCandidate";
import {YesNoUnsure} from "./candidate";

describe('CandidateDestination Interface', () => {
  const mockCandidate = new MockCandidate();
  it('should create an object that conforms to the CandidateDestination interface', () => {
    const destination: CandidateDestination = {
      id: 1,
      country: mockCandidate.country,
      candidate: mockCandidate,
      interest: YesNoUnsure.Yes,
      notes: 'Additional notes'
    };

    expect(destination).toBeTruthy();
    expect(destination.id).toBe(1);
    expect(destination.country?.name).toBe('United States');
    expect(destination.interest).toBe(YesNoUnsure.Yes);
    expect(destination.notes).toBe('Additional notes');
  });

  it('should have the correct types for each property', () => {
    const destination: CandidateDestination = {
      id: 1,
      country: mockCandidate.country,
      candidate: mockCandidate,
      interest: YesNoUnsure.Yes,
      notes: 'Additional notes'
    };

    expect(typeof destination.id).toBe('number');
    expect(typeof destination.country).toBe('object'); // Assuming Country is a complex object
    expect(typeof destination.candidate).toBe('object'); // Assuming Candidate is a complex object
    expect(typeof destination.interest).toBe('string');
    expect(typeof destination.notes).toBe('string');
  });
});
