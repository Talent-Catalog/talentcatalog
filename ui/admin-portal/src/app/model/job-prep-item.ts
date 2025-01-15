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
import {Job} from "./job";

/**
 * Classes representing things (items) needed to be performed when preparing a job for publication.
 * <p/>
 * All classes subclass JobPrepItem.
 */
export abstract class JobPrepItem {
  private _job: Job;

  /**
   * Every item has a ...
   * @param _description displayable description of this item
   * @param _tabId ID of job display tab where this item is defined
   */
  constructor(
    private _description: string,
    private _tabId: string
    ) {
  }

  /**
   * Returns true if this item is completed
   */
  public abstract isCompleted(): boolean;

  get job(): Job {
    return this._job;
  }

  set job(value: Job) {
    this._job = value;
  }

  get description(): string {
    return this._description;
  }

  get tabId(): string {
    return this._tabId;
  }

  /**
   * Checks that given text is not null and is not empty string or all blank (spaces).
   * @param text Text to check
   */
  isBlank(text: string): boolean {
    return text == null ||
    //Replace non-breaking spaces with normal spaces so that trim works.
    //(Rich text (html) data entry can encode spaces as &nbsp;)
    text.replace(/&nbsp;/g, ' ').trim().length == 0
  }
}

export class JobPrepDueDate extends JobPrepItem {
  constructor() {
    super("Submission due date (optional)", "General");
  }

  isCompleted(): boolean {
    return this.job?.submissionDueDate != null;
  }
}

export class JobPrepJD extends JobPrepItem {
  constructor() {
    super("Provide job description (Job Uploads Tab)", "Uploads");
  }

  isCompleted(): boolean {
    const fileJdLink = this.job?.submissionList?.fileJdLink;
    return !this.isBlank(fileJdLink);
  }
}

export class JobPrepJobSummary extends JobPrepItem {
  constructor() {
    super("Provide job summary", null);
  }

  isCompleted(): boolean {
    const jobSummary = this.job?.jobSummary;
    return !this.isBlank(jobSummary);
  }
}

export class JobPrepJOI extends JobPrepItem {
  constructor() {
    super("Provide job opportunity intake (JOI)", "Intake");
  }

  isCompleted(): boolean {
    const joi = this.job?.jobOppIntake;
    let completed = false;
    if (joi != null) {
      completed = !this.isBlank(joi.employerCostCommitment);
    }
    return completed;
  }
}

export class JobPrepSuggestedCandidates extends JobPrepItem {
  private _empty = true;

  constructor() {
    super("Suggested candidate(s) (optional)", "General");
  }

  isCompleted(): boolean {
    return !this._empty;
  }

  set empty(value: boolean) {
    this._empty = value;
  }
}

export class JobPrepSuggestedSearches extends JobPrepItem {
  constructor() {
    super("Suggested search(es) (optional)", "Searches");
  }

  isCompleted(): boolean {
    const suggestedSearches = this.job?.suggestedSearches;
    return suggestedSearches != null && suggestedSearches.length > 0;
  }
}
