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

import {Country} from './country';
import {EducationMajor} from './education-major';
import {CandidateEducation} from "./candidate-education";

describe('CandidateEducation Model Interface', () => {
  let candidateEducation: CandidateEducation;

  beforeEach(() => {
    // Initialize a sample CandidateEducation object before each test
    candidateEducation = {
      id: 1,
      educationType: 'Bachelor',
      country: { id: 1, name: 'Sample Country' } as Country,
      educationMajor: { id: 1, name: 'Computer Science' } as EducationMajor,
      lengthOfCourseYears: 4,
      institution: 'Sample University',
      courseName: 'Computer Science',
      yearCompleted: '2022',
      incomplete: false,
    };
  });

  it('should create a CandidateEducation instance', () => {
    expect(candidateEducation).toBeDefined();
  });

  it('should have all properties defined correctly', () => {
    expect(candidateEducation.id).toBe(1);
    expect(candidateEducation.educationType).toBe('Bachelor');
    expect(candidateEducation.country.id).toBe(1);
    expect(candidateEducation.educationMajor.id).toBe(1);
    expect(candidateEducation.lengthOfCourseYears).toBe(4);
    expect(candidateEducation.institution).toBe('Sample University');
    expect(candidateEducation.courseName).toBe('Computer Science');
    expect(candidateEducation.yearCompleted).toBe('2022');
    expect(candidateEducation.incomplete).toBe(false);
  });

  it('should handle incomplete education correctly', () => {
    candidateEducation.incomplete = true;
    expect(candidateEducation.incomplete).toBe(true);
  });

  it('should correctly handle undefined or null values', () => {
    candidateEducation.country = undefined;
    candidateEducation.educationMajor = null;
    expect(candidateEducation.country).toBeUndefined();
    expect(candidateEducation.educationMajor).toBeNull();
  });

  it('should handle different education types', () => {
    candidateEducation.educationType = 'Master';
    expect(candidateEducation.educationType).toBe('Master');
  });

  it('should handle zero or negative length of course years', () => {
    candidateEducation.lengthOfCourseYears = 0;
    expect(candidateEducation.lengthOfCourseYears).toBe(0);

    candidateEducation.lengthOfCourseYears = -1;
    expect(candidateEducation.lengthOfCourseYears).toBe(-1);
  });

  it('should correctly format year completed', () => {
    candidateEducation.yearCompleted = '2023'; // Valid format
    expect(candidateEducation.yearCompleted).toBe('2023');

    candidateEducation.yearCompleted = 'Invalid Year'; // Invalid format
    expect(candidateEducation.yearCompleted).toBe('Invalid Year');
  });

  it('should handle empty institution name', () => {
    candidateEducation.institution = '';
    expect(candidateEducation.institution).toBe('');
  });

  it('should handle empty course name', () => {
    candidateEducation.courseName = '';
    expect(candidateEducation.courseName).toBe('');
  });

  it('should handle undefined or null values for incomplete status', () => {
    candidateEducation.incomplete = undefined;
    expect(candidateEducation.incomplete).toBeUndefined();

    candidateEducation.incomplete = null;
    expect(candidateEducation.incomplete).toBeNull();
  });
});
