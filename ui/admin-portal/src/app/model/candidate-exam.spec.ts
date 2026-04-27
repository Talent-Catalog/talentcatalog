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
import {CandidateExam} from './candidate-exam';

describe('CandidateExam', () => {
  let exam: CandidateExam;

  beforeEach(() => {
    // Initialize a sample exam object before each test
    exam = {
      id: 1,
      name: 'IELTS',
      institution: 'British Council',
      dateCompleted: '2023-05-15'
    };
  });

  it('should create an instance', () => {
    expect(exam).toBeTruthy();
    expect(typeof exam).toEqual('object');
  });

  it('should have correct properties', () => {
    expect(exam.id).toBeDefined();
    expect(exam.name).toBeDefined();
    expect(exam.institution).toBeDefined();
    expect(exam.dateCompleted).toBeDefined();
  });

  it('should have correct property types', () => {
    expect(typeof exam.id).toEqual('number');
    expect(typeof exam.name).toEqual('string');
    expect(typeof exam.institution).toEqual('string');
    expect(typeof exam.dateCompleted).toEqual('string');
  });

  it('should have valid date format for dateCompleted', () => {
    // Example of a regular expression to validate date format (YYYY-MM-DD)
    const dateFormat = /^\d{4}-\d{2}-\d{2}$/;
    expect(exam.dateCompleted).toMatch(dateFormat);
  });
});
