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

// new-job.e2e-spec.ts

import {browser, by, element} from 'protractor';
import {clickTabAndWait} from "../job-home/job-home.e2e.spec";

describe('New Job Component', () => {

  beforeEach(() => {
    // Navigate to the page containing the new job component
    browser.get('/jobs');
  });
  // Test cases for UI elements and basic functionality
  it('should display new job tab ', async () => {
    const liveJobsContainer = await clickTabAndWait(5);

    const newJobEntries = element.all(by.css('app-new-job'));
    expect(await newJobEntries.count()).toBeGreaterThan(0);
  });
  it('should display error message for invalid link', () => {
    const invalidLink = 'https://example.com'; // An invalid link

    // Locate the input field and type the invalid link
    const inputField = element(by.css('app-sf-joblink input[type="text"]'));
    inputField.sendKeys(invalidLink);

    // Wait for a brief moment (optional, can be adjusted)
    browser.sleep(1000);

    // Assertion: Check if the error message is displayed
    const errorMessage = element(by.css('app-sf-joblink .alert.alert-danger'));
    expect(errorMessage.isPresent()).toBeTruthy();

    // Assertion: Check if the error message text matches the expected message
    expect(errorMessage.getText()).toContain("Doesn't look like a Salesforce Job opportunity link to me!");
  });
});
