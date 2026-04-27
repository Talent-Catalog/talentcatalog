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

import {CandidateJobExperience} from './candidate-job-experience';
import {Country} from './country';

describe('CandidateJobExperience Interface', () => {
  it('should create a valid CandidateJobExperience object', () => {
    const country: Country = {
      id: 1,
      name: 'Afghanistan',
      status: 'active'
    } as Country;

    const candidateJobExperience: CandidateJobExperience = {
      id: 1,
      country: country,
      companyName: 'Talent Beyond Boundaries',
      role: 'Software Developer',
      startDate: '2022-01-01',
      endDate: '2023-01-01',
      fullTime: 'Yes',
      paid: 'Yes',
      description: 'Developed software solutions for global talent.',
      expanded: true
    };

    expect(candidateJobExperience).toBeDefined();
    expect(candidateJobExperience.id).toBe(1);
    expect(candidateJobExperience.country).toBe(country);
    expect(candidateJobExperience.companyName).toBe('Talent Beyond Boundaries');
    expect(candidateJobExperience.role).toBe('Software Developer');
    expect(candidateJobExperience.startDate).toBe('2022-01-01');
    expect(candidateJobExperience.endDate).toBe('2023-01-01');
    expect(candidateJobExperience.fullTime).toBe('Yes');
    expect(candidateJobExperience.paid).toBe('Yes');
    expect(candidateJobExperience.description).toBe('Developed software solutions for global talent.');
    expect(candidateJobExperience.expanded).toBe(true);
  });

  it('should allow expanded to be optional', () => {
    const country: Country = {
      id: 2,
      name: 'Canada',
      status: 'active'
    } as Country;

    const candidateJobExperience: CandidateJobExperience = {
      id: 2,
      country: country,
      companyName: 'Tech Corp',
      role: 'Engineer',
      startDate: '2021-05-01',
      endDate: '2022-05-01',
      fullTime: 'No',
      paid: 'No',
      description: 'Worked on engineering projects.'
    };

    expect(candidateJobExperience).toBeDefined();
    expect(candidateJobExperience.expanded).toBeUndefined();
  });

  it('should have correct types for all properties', () => {
    const country: Country = {
      id: 3,
      name: 'United States',
      status: 'active'
    } as Country;

    const candidateJobExperience: CandidateJobExperience = {
      id: 3,
      country: country,
      companyName: 'Innovate Ltd.',
      role: 'Designer',
      startDate: '2020-08-01',
      endDate: '2021-08-01',
      fullTime: 'Yes',
      paid: 'Yes',
      description: 'Designed innovative solutions.'
    };

    expect(typeof candidateJobExperience.id).toBe('number');
    expect(typeof candidateJobExperience.country).toBe('object');
    expect(typeof candidateJobExperience.companyName).toBe('string');
    expect(typeof candidateJobExperience.role).toBe('string');
    expect(typeof candidateJobExperience.startDate).toBe('string');
    expect(typeof candidateJobExperience.endDate).toBe('string');
    expect(typeof candidateJobExperience.fullTime).toBe('string');
    expect(typeof candidateJobExperience.paid).toBe('string');
    expect(typeof candidateJobExperience.description).toBe('string');
  });
});
