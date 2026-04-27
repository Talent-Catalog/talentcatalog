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

import {Candidate} from "./candidate";
import {CandidateFieldInfo} from "./candidate-field-info";
import {MockSavedList} from "../MockData/MockSavedList";

describe('CandidateFieldInfo Class', () => {
  let candidate: Candidate;
  const savedSubList = MockSavedList;

  beforeEach(() => {
    // Initialize a sample Candidate object before each test
    candidate = {
      id: 1,
      firstName: 'John',
      lastName: 'Doe',
      maritalStatus: 'Single',
      ieltsScore: 7.5,
      latestIntake: 'Spring 2023',
      latestIntakeDate: new Date('2023-04-01'),
    } as unknown as Candidate;
  });

  it('should create a CandidateFieldInfo instance', () => {
    const fieldInfo = new CandidateFieldInfo(
      'Marital Status',
      'maritalStatus',
      null,
      null,
      () => true,
      false
    );

    expect(fieldInfo).toBeDefined();
    expect(fieldInfo instanceof CandidateFieldInfo).toBe(true);
  });

  it('should correctly initialize properties', () => {
    const fieldInfo = new CandidateFieldInfo(
      'Marital Status',
      'maritalStatus',
      null,
      null,
      () => true,
      false
    );

    expect(fieldInfo.displayName).toBe('Marital Status');
    expect(fieldInfo.fieldPath).toBe('maritalStatus');
    expect(fieldInfo.fieldSelector(savedSubList)).toBe(true);
    expect(fieldInfo.sortable).toBe(false);
  });

  it('should return correct tooltip', () => {
    const tooltipSupplier = (value: any) => `Tooltip for ${value}`;

    const fieldInfo = new CandidateFieldInfo(
      'Marital Status',
      'maritalStatus',
      tooltipSupplier,
      null,
      () => true,
      false
    );

    const tooltip = fieldInfo.getTooltip(candidate, savedSubList);
    expect(tooltip).toBe(`Tooltip for ${candidate}`);
  });

  it('should return formatted value', () => {
    const fieldFormatter = (value: any) => `Formatted ${value}`;

    const fieldInfo = new CandidateFieldInfo(
      'First Name',
      'firstName',
      null,
      fieldFormatter,
      () => true,
      false
    );

    const formattedValue = fieldInfo.getValue(candidate, savedSubList);
    expect(formattedValue).toBe('Formatted John');
  });

  it('should return unformatted value', () => {
    const fieldInfo = new CandidateFieldInfo(
      'IELTS Score',
      'ieltsScore',
      null,
      null,
      () => true,
      false
    );

    const unformattedValue = fieldInfo.getUnformattedValue(candidate);
    expect(unformattedValue).toBe(7.5);
  });

  it('should handle nested field path', () => {
    const fieldInfo = new CandidateFieldInfo(
      'Latest Intake Date',
      'latestIntakeDate',
      null,
      null,
      () => true,
      false
    );

    const unformattedValue = fieldInfo.getUnformattedValue(candidate);
    expect(unformattedValue).toEqual(new Date('2023-04-01'));
  });

  it('should handle null candidate object', () => {
    const fieldInfo = new CandidateFieldInfo(
      'Last Name',
      'lastName',
      null,
      null,
      () => true,
      false
    );

    const unformattedValue = fieldInfo.getUnformattedValue(null);
    expect(unformattedValue).toBeNull();
  });

  it('should handle sortable field', () => {
    const fieldInfo = new CandidateFieldInfo(
      'Last Name',
      'lastName',
      null,
      null,
      () => true,
      true
    );

    const isSortable = fieldInfo.sortable;
    expect(isSortable).toBe(true);
  });
});
