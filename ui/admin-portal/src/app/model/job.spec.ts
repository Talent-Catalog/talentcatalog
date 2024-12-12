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
import {isJob,
  Job,
  JobOpportunityStage,
  SearchJobRequest,
  UpdateJobRequest
} from "./job";
import {MockJob} from "../MockData/MockJob";

describe('Job', () => {
  let job: Job;

  beforeEach(() => {
    job = MockJob;
  });

  it('should create a Job instance', () => {
    expect(job).toBeTruthy(); // Check if job instance exists
    expect(job.id).toBe(1); // Check id property
    expect(job.hiringCommitment).toBe('Full-time'); // Check hiringCommitment property
    expect(job.country.name).toBe('USA'); // Check nested country property
    expect(job.stage).toBe(JobOpportunityStage.prospect); // Check stage property
  });

  it('should correctly determine if an opportunity is a Job', () => {
    const isJobFlag = isJob({ ...job });
    expect(isJobFlag).toBe(true); // Expecting a Job opportunity
  });

});

describe('SearchJobRequest', () => {
  it('should create a SearchJobRequest instance', () => {
    const searchRequest: SearchJobRequest = {
      starred: true,
      jobNameAndIdOnly: false,
      pageNumber: 1,
      pageSize: 10
    };
    expect(searchRequest).toBeTruthy(); // Check if searchRequest instance exists
    expect(searchRequest.starred).toBe(true); // Check starred property
    expect(searchRequest.jobNameAndIdOnly).toBe(false); // Check jobNameAndIdOnly property
  });
});

describe('UpdateJobRequest', () => {
  it('should create an UpdateJobRequest instance', () => {
    const updateRequest: UpdateJobRequest = {
      contactUserId: 1,
      evergreen: true,
      roleName: 'Manager',
      sfId: '12345',
      sfJoblink: 'https://example.com/job/12345',
      jobToCopyId: 2
    };
    expect(updateRequest).toBeTruthy(); // Check if updateRequest instance exists
    expect(updateRequest.evergreen).toBe(true); // Check evergreen property
    expect(updateRequest.roleName).toBe('Manager'); // Check roleName property
    expect(updateRequest.sfId).toBe('12345'); // Check sfId property
    expect(updateRequest.sfJoblink).toBe('https://example.com/job/12345'); // Check sfJoblink property
  });
});
