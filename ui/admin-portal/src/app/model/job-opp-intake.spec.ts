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

import {JobOppIntake} from "./job-opp-intake";

describe('JobOppIntake', () => {
  let intake: JobOppIntake;

  beforeEach(() => {
    intake = {
      employerCostCommitment: 'Full-time',
      recruitmentProcess: 'Interview',
      minSalary: 50000,
      occupationCode: '12345',
      salaryRange: '$50,000 - $70,000',
      locationDetails: 'Remote',
      location: 'Any',
      visaPathways: 'Skilled Worker Visa',
      benefits: 'Healthcare, Dental',
      educationRequirements: 'Bachelor\'s degree',
      languageRequirements: 'English',
      employmentExperience: '5 years',
      skillRequirements: 'Communication, Problem-solving'
    };
  });

  it('should create a JobOppIntake instance', () => {
    expect(intake).toBeTruthy(); // Check if intake instance exists
    expect(intake.employerCostCommitment).toBe('Full-time'); // Check employerCostCommitment property
    expect(intake.recruitmentProcess).toBe('Interview'); // Check recruitmentProcess property
    expect(intake.minSalary).toBe(50000); // Check minSalary property
    expect(intake.occupationCode).toBe('12345'); // Check occupationCode property
    expect(intake.salaryRange).toBe('$50,000 - $70,000'); // Check salaryRange property
    expect(intake.locationDetails).toBe('Remote'); // Check locationDetails property
    expect(intake.location).toBe('Any'); // Check location property
    expect(intake.visaPathways).toBe('Skilled Worker Visa'); // Check visaPathways property
    expect(intake.benefits).toBe('Healthcare, Dental'); // Check benefits property
    expect(intake.educationRequirements).toBe("Bachelor's degree"); // Check educationRequirements property
    expect(intake.languageRequirements).toBe('English'); // Check languageRequirements property
    expect(intake.employmentExperience).toBe('5 years'); // Check employmentExperience property
    expect(intake.skillRequirements).toBe('Communication, Problem-solving'); // Check skillRequirements property
  });

  it('should allow undefined properties', () => {
    const partialIntake: JobOppIntake = {
      employerCostCommitment: 'Full-time',
      location: 'Remote'
    };
    expect(partialIntake.employerCostCommitment).toBe('Full-time');
    expect(partialIntake.location).toBe('Remote');
    // Check that other properties are undefined
    expect(partialIntake.recruitmentProcess).toBeUndefined();
    expect(partialIntake.minSalary).toBeUndefined();
    expect(partialIntake.occupationCode).toBeUndefined();
    expect(partialIntake.salaryRange).toBeUndefined();
    expect(partialIntake.locationDetails).toBeUndefined();
    expect(partialIntake.visaPathways).toBeUndefined();
    expect(partialIntake.benefits).toBeUndefined();
    expect(partialIntake.educationRequirements).toBeUndefined();
    expect(partialIntake.languageRequirements).toBeUndefined();
    expect(partialIntake.employmentExperience).toBeUndefined();
    expect(partialIntake.skillRequirements).toBeUndefined();
  });
});
