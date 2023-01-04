/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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
import {Job} from "./job";

/**
 * Classes representing things (items) needed to be performed when preparing a job for publication.
 * <p/>
 * All classes subclass JobPrepItem.
 */
export abstract class JobPrepItem {

  /**
   * Every item has a ...
   * @param _job the job being prepared for publication
   * @param _description displayable description of this item
   */
  constructor(private _job: Job, private _description: string) {
  }

  /**
   * Returns true if this item is completed
   */
  public abstract isCompleted(): boolean;

  get job(): Job {
    return this._job;
  }

  get description(): string {
    return this._description;
  }
}

export class JobPrepDueDate extends JobPrepItem {
  constructor(job: Job) {
    super(job, "Submission due date");
  }

  isCompleted(): boolean {
    return this.job?.submissionDueDate != null;
  }
}

export class JobPrepJD extends JobPrepItem {
  constructor(job: Job) {
    super(job, "Provide job description (JD)");
  }

  isCompleted(): boolean {
    return this.job?.submissionList?.fileJdLink != null
      && this.job.submissionList.fileJdLink.trim().length > 0;
  }
}

export class JobPrepJobSummary extends JobPrepItem {
  constructor(job: Job) {
    super(job, "Provide job summary");
  }

  isCompleted(): boolean {
    return this.job?.jobSummary != null && this.job.jobSummary.trim().length > 0;
  }
}

export class JobPrepJOI extends JobPrepItem {
  constructor(job: Job) {
    super(job, "Provide job opportunity intake (JOI)");
  }

  isCompleted(): boolean {
    return this.job?.submissionList?.fileJoiLink != null;
  }
}

export class JobPrepSuggestedCandidates extends JobPrepItem {
  constructor(job: Job) {
    super(job, "Suggested candidate(s)");
  }

  isCompleted(): boolean {
    //todo Really want to check that list is not empty.
    return this.job?.submissionList != null;
  }
}

export class JobPrepSuggestedSearches extends JobPrepItem {
  constructor(job: Job) {
    super(job, "Suggested search(es)");
  }

  isCompleted(): boolean {
    return this.job?.suggestedSearches != null;
  }
}
