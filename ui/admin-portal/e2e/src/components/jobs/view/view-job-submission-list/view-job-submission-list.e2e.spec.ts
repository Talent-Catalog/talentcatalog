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

import {browser, by, element, ExpectedConditions} from "protractor";
import {clickTabAndWait} from "../../job-home/job-home.e2e.spec";

describe('View-Job-Submission-List Component', () => {
  beforeEach(() => {
    // Navigate to the page where the component is rendered
    browser.get('/jobs');
  });
  // Test cases for UI elements and basic functionality
  it('should display TBB Job Cases with associated details', async () => {
    await clickTabAndWait(3);
    const candidateOpportunities = element.all(by.css('app-candidate-opps-with-detail'));
    expect(await candidateOpportunities.count()).toBeGreaterThan(0);
  });
  it('should render the submission list component when there is a submission list available for the job', async () => {
    // Identify the candidate opportunity to select (assuming the first one for simplicity)
      const candidateOpportunityRow = element.all(by.css('app-candidate-opps-with-detail app-candidate-opps tbody tr')).first();
      const submissionListCol = candidateOpportunityRow.element(by.css('td:nth-child(2)'));
      const submissionListLink = submissionListCol.element(by.css('a:nth-child(5)'));
      await browser.wait(ExpectedConditions.elementToBeClickable(submissionListLink), 10000);
      submissionListLink.click();
      await browser.wait(ExpectedConditions.invisibilityOf(element(by.css('.fa-spinner'))), 10000);
      browser.sleep(1000);
      // Get the candidate show list component
      const appShowCandidates = element(by.css('app-show-candidates table tbody'));
      browser.wait(ExpectedConditions.presenceOf(appShowCandidates), 5000, 'Tbody element is not present');
      // Assert that the submission list component contains the app-show-candidates component
      expect(appShowCandidates.isPresent()).toBeTruthy();
      const rows = appShowCandidates.all(by.css('tr'));
      // Assert if the count of rows is greater than zero
      expect(rows.count()).toBeGreaterThan(0);

  });
});
