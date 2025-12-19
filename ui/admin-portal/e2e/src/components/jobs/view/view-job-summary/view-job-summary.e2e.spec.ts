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

// view-job-summary.e2e-spec.ts

import {browser, by, element} from 'protractor';
import {clickTabAndWait} from "../../job-home/job-home.e2e.spec";

describe('ViewJobSummaryComponent', () => {

  beforeEach(() => {
    browser.get('/jobs');
  });

  it('should display job summary input field', async () => {
    await clickTabAndWait(0);
    const jobEntries = element.all(by.css('app-jobs-with-detail'));
    expect(await jobEntries.count()).toBeGreaterThan(0);
    expect(element(by.id('jobSummary')).isPresent()).toBeTruthy();
  });

  it('should save changes to job summary', async () => {
    const newSummary = 'Updated job summary';

    // Find the job summary textarea and enter a new value
    const jobSummaryTextarea = element(by.id('jobSummary'));
    jobSummaryTextarea.clear();
    jobSummaryTextarea.sendKeys(newSummary);

    // Find and click the save button
    const saveButton = element.all(by.css('app-view-job-summary .btn.btn-sm.btn-secondary')).first();
    saveButton.click();

    // Wait for the save operation to complete
    browser.waitForAngular();

    // Check if the job summary has been updated
    expect(await jobSummaryTextarea.getAttribute('value')).toEqual(newSummary);
  });

  it('should cancel changes to job summary', async () => {

    // Find the job summary textarea and enter a new value
    const jobSummaryTextarea = element(by.id('jobSummary'));
    const initialSummary = jobSummaryTextarea.getAttribute('value');
    jobSummaryTextarea.clear();
    jobSummaryTextarea.sendKeys('New Updated job summary');

    // Find and click the cancel button
    const cancelButton = element(by.css('.btn.btn-sm.btn-secondary:nth-of-type(2)'));
    cancelButton.click();

    // Check if the job summary reverts to the initial value
    expect(await jobSummaryTextarea.getAttribute('value')).toEqual(await initialSummary);
  });

});
