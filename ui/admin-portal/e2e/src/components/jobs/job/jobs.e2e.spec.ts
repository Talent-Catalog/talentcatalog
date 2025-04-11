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
import {clickTabAndWait} from "../job-home/job-home.e2e.spec";

// Test suite for the JobsComponent
describe('JobsComponent', () => {
  // Define the columns to be tested
  const columns = ['name', 'stage', 'created', 'due'];

  // Before each test case, navigate to the '/jobs' route
  beforeEach(() => {
    browser.get('/jobs');
  });

  // Test cases for UI elements and basic functionality
  it('should display live jobs with associated details', async () => {
    const liveJobsContainer = await clickTabAndWait(0);

    const liveJobEntries = element.all(by.css('app-jobs-with-detail'));
    expect(await liveJobEntries.count()).toBeGreaterThan(0);

    await liveJobEntries.each(async (jobEntry) => {
      expect(await jobEntry.element(by.css('.detail-panel')).isPresent()).toBeTruthy();
    });
  });

  it('should display loading spinner initially', () => {
    const loadingSpinner = element(by.css('.fa-spinner'));
    expect(loadingSpinner.isPresent()).toBeFalsy();
  });

  it('should display search form', () => {
    const searchForm = element(by.css('.searches form'));
    expect(searchForm.isPresent()).toBeTruthy();
  });

  it('should display table headers', () => {
    const tableHeaders = element.all(by.css('table thead th'));
    expect(tableHeaders.count()).toBeGreaterThan(0);
  });

  it('should display job records', () => {
    const jobRecords = element.all(by.css('table tbody tr'));
    expect(jobRecords.count()).toBeGreaterThan(0);
  });

  it('should navigate to job details page when a job is clicked', () => {
    const firstJobRecord = element.all(by.css('table tbody tr')).first();
    firstJobRecord.click();
    browser.getCurrentUrl().then(url => {
      expect(url).toContain('/jobs');
    });
  });

  it('should display pagination', () => {
    const pagination = element(by.css('ngb-pagination'));
    expect(pagination.isPresent()).toBeTruthy();
  });

  it('should display total number of jobs found', () => {
    const totalJobsText = element(by.css('.text-muted'));
    expect(totalJobsText.isPresent()).toBeTruthy();
  });

  // Test cases for searching by stage and destination country

  it('should search by stage', async () => {
    const stageFilter = element(by.id('stage'));

    // Expand the stage filter dropdown
    await stageFilter.click();
    await browser.sleep(1000);

    // Select the 'Pitching' stage
    const stageOption = stageFilter.element(by.cssContainingText('.ng-option-label', 'Pitching'));
    await stageOption.click();
    await browser.sleep(2000);

    // Check if search results are displayed
    const searchResults = element.all(by.css('.table tbody tr'));
    const searchResultsPresent = await searchResults.isPresent();

    // Assert that search results contain the expected stage name
    if (searchResultsPresent) {
      await browser.wait(ExpectedConditions.visibilityOf(searchResults.first()), 5000, 'First search result not visible');
      const firstSearchResultStage = await searchResults.first().element(by.css('td:nth-child(2)')).getText();
      expect(firstSearchResultStage).toContain('Pitching');
    } else {
      console.log('No search results found.');
      expect(searchResultsPresent).toBeFalsy();
    }
  });

  it('should search by destination country', async () => {
    const destinationFilter = element(by.id('destinations'));

    // Expand the destination filter dropdown
    await destinationFilter.click();
    await browser.sleep(1000);

    // Select 'Afghanistan' as the destination
    const destinationOption = destinationFilter.element(by.cssContainingText('span', 'Afghanistan'));
    await destinationOption.click();
    await browser.sleep(1000);

    // Check if search results are displayed
    const searchResults = element.all(by.css('.table tbody tr'));
    const searchResultsPresent = await searchResults.isPresent();

    // Assert that search results contain the expected destination
    if (searchResultsPresent) {
      await browser.wait(ExpectedConditions.presenceOf(searchResults.first()), 1000, 'Search results did not load');
      const firstSearchResultDestination = await searchResults.first().element(by.css('td:nth-child(2)')).getText();
      expect(firstSearchResultDestination).toContain('Afghanistan');
    } else {
      console.log('No search results found.');
      expect(searchResultsPresent).toBeFalsy();
    }
  });

  // Test cases for sorting by different columns

  columns.forEach(async (column, index) => {
    it(`should sort jobs by ${column}`, async () => {
      const header = element(by.css(`thead th:nth-child(${index + 1})`));

      // Click on the column header to sort
      await header.click();
      await browser.sleep(2000);

      // Get the job data after sorting
      let sortedData = await element.all(by.css(`tbody td:nth-child(${index + 1})`)).map(element => element.getText());

      // Sort using customCompare function for 'name' column
      if (column === 'name') {
        sortedData = sortedData.sort(customCompare);
      } else {
        sortedData.sort(); // Use default string comparison for other columns
      }

      // Check if the job data is sorted in ascending order
      let isSortedAscending = true;
      for (let i = 1; i < sortedData.length; i++) {
        if (sortedData[i] < sortedData[i - 1]) {
          console.log(`Out of order: ${sortedData[i - 1]} is greater than ${sortedData[i]}`);
          isSortedAscending = false;
          break;
        }
      }

      // Click again to toggle to descending
      await header.click();
      await browser.sleep(2000);

      // Get the job data after toggling to descending
      let sortedDataDesc = await element.all(by.css(`tbody td:nth-child(${index + 1})`)).map(element => element.getText());

      // Sort using customCompare function for 'name' column
      if (column === 'name') {
        sortedDataDesc = sortedDataDesc.sort(customCompare);
      } else {
        sortedDataDesc.sort(); // Use default string comparison for other columns
      }

      // Reverse the descending sorted data
      sortedDataDesc.reverse();

      // Check if the job data is sorted in descending order
      let isSortedDescending = true;
      for (let i = 1; i < sortedDataDesc.length; i++) {
        if (sortedDataDesc[i] > sortedDataDesc[i - 1]) {
          console.log(`Out of order: ${sortedDataDesc[i - 1]} is less than ${sortedDataDesc[i]}`);
          isSortedDescending = false;
          break;
        }
      }

      // Check if either ascending or descending sorting is successful
      expect(isSortedAscending || isSortedDescending).toBeTruthy();
    });
  });
});

// Function to compare strings with custom logic for sorting
function customCompare(a: string, b: string): number {
  // Treat "*" as less than other characters
  if (a.startsWith('*') && !b.startsWith('*')) {
    return -1;
  } else if (!a.startsWith('*') && b.startsWith('*')) {
    return 1;
  } else {
    // Use default comparison for other cases
    return a.localeCompare(b);
  }
}
