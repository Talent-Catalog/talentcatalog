/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import {MockJob} from "../MockData/MockJob";

fdescribe('Job Preparation Items', () => {


  describe('JobPrepDueDate', () => {
    let item: JobPrepDueDate;

    beforeEach(() => {
      item = new JobPrepDueDate();
      item.job = MockJob;
    });

    it('should return true if submission due date is set', () => {
      expect(item.isCompleted()).toBe(true);
    });
  });

  describe('JobPrepJD', () => {
    let item: JobPrepJD;

    beforeEach(() => {
      item = new JobPrepJD();
      item.job = MockJob;
    });

    it('should return true if job description (JD) is provided', () => {
      expect(item.isCompleted()).toBe(true);
    });

    it('should return false if job description (JD) link is not provided', () => {
      item.job.submissionList.fileJdLink = '';
      expect(item.isCompleted()).toBe(false);
    });
  });

  describe('JobPrepJobSummary', () => {
    let item: JobPrepJobSummary;

    beforeEach(() => {
      item = new JobPrepJobSummary();
      item.job = MockJob;
    });

    it('should return true if job summary is provided', () => {
      expect(item.isCompleted()).toBe(true);
    });

    it('should return false if job summary is not provided', () => {
      item.job.jobSummary = '';
      expect(item.isCompleted()).toBe(false);
    });
  });

  describe('JobPrepJOI', () => {
    let item: JobPrepJOI;

    beforeEach(() => {
      item = new JobPrepJOI();
      item.job = MockJob;
    });

    it('should return true if job opportunity intake (JOI) is provided', () => {
      expect(item.isCompleted()).toBe(true);
    });

    it('should return false if employer cost commitment is not provided', () => {
      item.job.jobOppIntake.employerCostCommitment = '';
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
  });

  describe('JobPrepSuggestedSearches', () => {
    let item: JobPrepSuggestedSearches;

    beforeEach(() => {
      item = new JobPrepSuggestedSearches();
      item.job = MockJob;
    });

    it('should return false if suggested searches are not provided', () => {
      item.job.suggestedSearches = [];
      expect(item.isCompleted()).toBe(false);
    });
  });
});
