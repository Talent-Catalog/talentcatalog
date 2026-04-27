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

import {browser, by, element, ExpectedConditions} from 'protractor';
import {clickTabAndWait} from "../../../job-home/job-home.e2e.spec";

describe('View Job Info Component', () => {

  beforeEach(() => {
    // Navigate to the page containing the ViewJobInfoComponent
    browser.get('/jobs');
  });
  // Test cases for UI elements and basic functionality
  it('should display live jobs with associated details', async () => {
    const liveJobsContainer = await clickTabAndWait(1);

    const liveJobEntries = element.all(by.css('app-jobs-with-detail'));
    expect(await liveJobEntries.count()).toBeGreaterThan(0);

  });
  it('should display job information correctly', () => {
    const jobInfo = element.all(by.css('app-view-job-info'));

    // Check if job information such as country, employer, evergreen status, etc., are displayed correctly
    expect(element(by.css('app-view-job-info .card-header')).getText()).toContain('Job Information');
    expect(element(by.css('app-view-job-info .card-body')).getText()).toContain('Country');
    expect(element(by.css('app-view-job-info .card-body')).getText()).toContain('Employer');
    expect(element(by.css('app-view-job-info .card-body')).getText()).toContain('Evergreen');
    // Add more assertions for other job information as needed
  });

  it('should trigger edit mode when edit button is clicked', async () => {
    // Simulate clicking the edit button
    element(by.css('app-view-job-info .btn.btn-sm.btn-secondary')).click();
    const modalDialog = element(by.css('.modal-dialog.modal-dialog-centered'));
    await browser.wait(ExpectedConditions.presenceOf(modalDialog), 5000);
    // Assertion to check if edit mode is triggered
    expect(modalDialog.isDisplayed()).toBeTruthy();
  });
});
