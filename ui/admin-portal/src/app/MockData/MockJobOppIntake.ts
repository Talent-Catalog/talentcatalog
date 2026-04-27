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

import {JobOppIntake} from "../model/job-opp-intake";

export class MockJobOppIntake implements JobOppIntake {
  employerCostCommitment: string = 'Default Employer Cost Commitment';
  recruitmentProcess: string = 'Default Recruitment Process';
  minSalary: number = 0; // Default minimum salary
  occupationCode: string = 'Default Occupation Code';
  salaryRange: string = 'Default Salary Range';
  locationDetails: string = 'Default Location Details';
  location: string = 'Default Location';
  visaPathways: string = 'Default Visa Pathways';
  benefits: string = 'Default Benefits';
  educationRequirements: string = 'Default Education Requirements';
  languageRequirements: string = 'Default Language Requirements';
  employmentExperience: string = 'Default Employment Experience';
  skillRequirements: string = 'Default Skill Requirements';

  constructor(options?: Partial<JobOppIntake>) {
    if (options) {
      Object.assign(this, options);
    }
  }
}
