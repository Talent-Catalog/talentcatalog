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

import {CandidateCertification} from "./candidate-certification";

describe('CandidateCertification Interface', () => {
  it('should create an object that conforms to the CandidateCertification interface', () => {
    const certification: CandidateCertification = {
      id: 1,
      name: 'Certified Angular Developer',
      institution: 'Tech Institute',
      dateCompleted: '2021-06-15'
    };

    expect(certification).toBeTruthy();
    expect(certification.id).toBe(1);
    expect(certification.name).toBe('Certified Angular Developer');
    expect(certification.institution).toBe('Tech Institute');
    expect(certification.dateCompleted).toBe('2021-06-15');
  });

  it('should have the correct types for each property', () => {
    const certification: CandidateCertification = {
      id: 1,
      name: 'Certified Angular Developer',
      institution: 'Tech Institute',
      dateCompleted: '2021-06-15'
    };

    expect(typeof certification.id).toBe('number');
    expect(typeof certification.name).toBe('string');
    expect(typeof certification.institution).toBe('string');
    expect(typeof certification.dateCompleted).toBe('string');
  });
});
