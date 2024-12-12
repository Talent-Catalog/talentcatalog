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

import {browser, by, element} from "protractor";
import {clickTabAndWait} from "../../job-home/job-home.e2e.spec";

describe('ViewJobFromUrlComponent', () => {
  beforeEach(() => {
    // Navigate to the page where the ViewJobFromUrlComponent is rendered
    browser.get('/jobs');
  });
  it('should display view job component', async () => {
    await clickTabAndWait(1);
    const jobEntries = element.all(by.css('app-jobs-with-detail'));
    expect(await jobEntries.count()).toBeGreaterThan(0);
  });
  it('should display job details when loaded successfully', async () => {
    const appJobs = element(by.css('app-jobs'));
    const firstRow = appJobs.all(by.css('table tbody tr')).first();
    const jobColumn = firstRow.element(by.css('td:nth-child(1)'));
    const jobLink = jobColumn.element(by.css('.fa-briefcase'));

    expect(await jobLink.isPresent()).toBe(true);

    // Check if the spinner is not displayed after loading
    const spinner = element(by.css('.fa-spinner'));
    expect(await spinner.isPresent()).toBe(false);

    // Check if the error alert is not displayed after successful loading
    const errorAlert = element(by.css('.alert-danger'));
    expect(await errorAlert.isPresent()).toBe(false);

    // Check if the job details are displayed
    const jobDetails = element(by.css('app-view-job'));
    expect(await jobDetails.isPresent()).toBe(true);
  });
});
