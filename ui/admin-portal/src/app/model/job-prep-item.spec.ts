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

import {
  JobPrepDueDate,
  JobPrepJD,
  JobPrepJobSummary,
  JobPrepJOI,
  JobPrepSuggestedCandidates,
  JobPrepSuggestedSearches
} from "./job-prep-item";
import {Job} from "./job";
import {MockSavedSearch} from "../MockData/MockSavedSearch";
import {MockUser} from "../MockData/MockUser";
import {TaskType, UploadType} from "./task";

describe('JobPrepDueDate', () => {
  let item: JobPrepDueDate;
  let job: Job;

  beforeEach(() => {
    item = new JobPrepDueDate();
    job = {
      submissionDueDate: new Date(),
      submissionList: {
        id: 1,
        name: 'Mock Submission List',
        savedSearchSource: { id: 1 }, // SavedSearchRef example with only id
        fileJdLink: 'example.com/jd',
        fileJdName: 'JD File',
        fileJoiLink: 'example.com/joi',
        fileJoiName: 'JOI File',
      },
      jobSummary: 'This is a job summary',
      jobOppIntake: {
        employerCostCommitment: 'Full-time',
      },
      suggestedSearches: [new MockSavedSearch()],
    } as Job;
    item.job = job;
  });

  it('should return true if submission due date is set', () => {
    expect(item.isCompleted()).toBe(true);
  });
});

describe('JobPrepJD', () => {
  let item: JobPrepJD;
  let job: Job;

  beforeEach(() => {
    item = new JobPrepJD();
    job = {
      submissionList: {
        id: 1,
        name: 'Mock Submission List',
        savedSearchSource: { id: 1 }, // SavedSearchRef example with only id
        fileJdLink: 'example.com/jd',
        fileJdName: 'JD File',
        fileJoiLink: 'example.com/joi',
        fileJoiName: 'JOI File',
      },
    } as Job;
    item.job = job;
  });

  it('should return true if job description (JD) is provided', () => {
    expect(item.isCompleted()).toBe(true);
  });

  it('should return false if job description (JD) link is not provided', () => {
    job.submissionList.fileJdLink = '';
    expect(item.isCompleted()).toBe(false);
  });
});

describe('JobPrepJobSummary', () => {
  let item: JobPrepJobSummary;
  let job: Job;

  beforeEach(() => {
    item = new JobPrepJobSummary();
    job = {
      jobSummary: 'This is a job summary',
    } as Job;
    item.job = job;
  });

  it('should return true if job summary is provided', () => {
    expect(item.isCompleted()).toBe(true);
  });

  it('should return false if job summary is not provided', () => {
    job.jobSummary = '';
    expect(item.isCompleted()).toBe(false);
  });
});

describe('JobPrepJOI', () => {
  let item: JobPrepJOI;
  let job: Job;

  beforeEach(() => {
    item = new JobPrepJOI();
    job = {
      jobOppIntake: {
        employerCostCommitment: 'Full-time',
      },
    } as Job;
    item.job = job;
  });

  it('should return true if job opportunity intake (JOI) is provided', () => {
    expect(item.isCompleted()).toBe(true);
  });

  it('should return false if employer cost commitment is not provided', () => {
    job.jobOppIntake.employerCostCommitment = '';
    expect(item.isCompleted()).toBe(false);
  });
});

describe('JobPrepSuggestedCandidates', () => {
  let item: JobPrepSuggestedCandidates;

  beforeEach(() => {
    item = new JobPrepSuggestedCandidates();
  });

  it('should return false by default (empty is true)', () => {
    expect(item.isCompleted()).toBe(false);
  });

  it('should return true if suggested candidates are provided (empty is false)', () => {
    item.empty = false;
    expect(item.isCompleted()).toBe(true);
  });
});

describe('JobPrepSuggestedSearches', () => {
  let item: JobPrepSuggestedSearches;
  let job: Job;

  beforeEach(() => {
    item = new JobPrepSuggestedSearches();
    job = {
      suggestedSearches: [{ name: 'Search 1' }],
    } as Job;
    item.job = job;
  });

  it('should return true if suggested searches are provided', () => {
    expect(item.isCompleted()).toBe(true);
  });

  it('should return false if suggested searches are not provided', () => {
    job.suggestedSearches = [];
    expect(item.isCompleted()).toBe(false);
  });
});
