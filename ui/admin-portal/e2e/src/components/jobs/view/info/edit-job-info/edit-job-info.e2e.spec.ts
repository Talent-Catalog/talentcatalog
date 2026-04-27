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

import {browser, by, element, ExpectedConditions, ExpectedConditions as EC} from 'protractor';
import {clickTabAndWait} from "../../../job-home/job-home.e2e.spec";

describe('Edit Job Info Component', () => {

  beforeEach(() => {
    // Navigate to the page containing the component
    browser.get('/jobs');
  });
  // Test cases for UI elements and basic functionality
  it('should display live jobs with associated details', async () => {
    const liveJobsContainer = await clickTabAndWait(1);
    const liveJobEntries = element.all(by.css('app-jobs-with-detail'));
    expect(await liveJobEntries.count()).toBeGreaterThan(0);

  });
  it('should open modal for editing job info', async () => {
    // Simulate clicking the edit button
    element(by.css('app-view-job-info .btn.btn-sm.btn-secondary')).click();
    const modalDialog = element(by.css('.modal-dialog.modal-dialog-centered'));
    await browser.wait(ExpectedConditions.presenceOf(modalDialog), 5000);
    // Assertion to check if edit mode is triggered
    expect(modalDialog.isDisplayed()).toBeTruthy();
  });

  it('should save changes and close modal', async () => {
    // Simulate clicking the button to open the modal
    element(by.css('app-view-job-info .btn.btn-sm.btn-secondary')).click();

    // Wait for the modal to appear
    const modalDialog = element(by.css('.modal-dialog.modal-dialog-centered'));
    await browser.wait(ExpectedConditions.presenceOf(modalDialog), 5000);

    // Simulate filling the form fields
    element(by.css('#submissionDueDate input.form-control')).sendKeys('2024-05-31'); // Example date

    // Simulate clicking the save button
    element(by.css('app-edit-job-info div.modal-footer .btn.btn-primary')).click();

    // Wait for the modal to close
    browser.wait(EC.invisibilityOf(element(by.css('app-view-job-info .modal-dialog'))), 5000);

    // Assertion: Check if modal is closed
    expect(element(by.css('app-view-job-info .modal-dialog.modal-dialog-centered')).isPresent()).toBeFalsy();

    // Assertion: Optionally, check if changes are reflected in the UI
    // For example, check if the updated submission due date is displayed on the page
    // You may need to navigate to the page containing the job information to perform this check
  });

  // Add more test cases for error handling, edge cases, etc. as needed

});
