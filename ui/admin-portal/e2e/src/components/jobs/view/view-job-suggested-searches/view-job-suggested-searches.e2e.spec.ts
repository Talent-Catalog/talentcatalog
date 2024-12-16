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

describe('View-Job-Suggested-Searches Component', () => {
  beforeEach(() => {
    // Navigate to the page where the component is rendered
    browser.get('/jobs');
  });
  // Test cases for UI elements and basic functionality
  it('should display starred jobs with associated details', async () => {
    await clickTabAndWait(0);
    const jobEntries = element.all(by.css('app-jobs-with-detail'));
    expect(await jobEntries.count()).toBeGreaterThan(0);
  });
  it('should add a suggested search to the job', async () => {
    // Simulate the component being in a state where it has loaded and source partners are displayed
    const myTabELement = element(by.css('app-jobs-with-detail app-view-job nav.nav-tabs.nav a:nth-child(2)'))
    await browser.wait(ExpectedConditions.elementToBeClickable(myTabELement), 5000);
    myTabELement.click();
    // Get the count of search count before adding
    const oldSearchCount = await element.all(by.css('app-view-job-suggested-searches .row.mb-3')).count();
    // Get the button for adding a new search
    const addButton = element(by.css('app-view-job-suggested-searches .btn-primary'));
    await browser.wait(ExpectedConditions.elementToBeClickable(addButton), 5000);
    // Get the count of search items in the list after adding the new search

    // Click the button to open the modal for entering the search name suffix
    addButton.click();

    // Wait for the modal to be visible
    const modal = element(by.css('ngb-modal-window .modal-dialog'));
    browser.wait(ExpectedConditions.visibilityOf(modal), 5000);

    // Get the input field in the modal
    const inputField = modal.element(by.id('text'));

    // Enter the search name suffix
    inputField.sendKeys('search 1');
    // Get the button for confirming the input
    const confirmButton = modal.element(by.cssContainingText('app-input-text .btn-primary','Ok'));
    await browser.wait(ExpectedConditions.elementToBeClickable(confirmButton), 5000);
    // Click the confirm button
    confirmButton.click();
    // Wait for the modal to close
    browser.wait(ExpectedConditions.invisibilityOf(modal), 5000);

    const newSearchCount = await element.all(by.css('app-view-job-suggested-searches .row.mb-3')).count();

    // Assert that the count of search items after adding the new search is one more than the count before adding it
    expect(newSearchCount).toEqual(oldSearchCount + 1);
  });

  it('should delete the last created job', async () => {
    const oldSearchesList = element.all(by.css('app-view-job-suggested-searches div.card div.card-body div div.row.mb-3'));
    const newSearch = oldSearchesList.last();
    const oldCount = await oldSearchesList.count();
    expect(newSearch.isPresent()).toBeTruthy();

    // Assuming the last job has a delete button with class 'delete-button'
    const deleteButton = newSearch.element(by.css('button.btn.btn-sm.btn-outline-danger'));
    await browser.wait(ExpectedConditions.elementToBeClickable(deleteButton), 10000);
    browser.executeScript('arguments[0].scrollIntoView(true);', deleteButton.getWebElement());
    browser.sleep(1000);
    expect(deleteButton.isPresent()).toBeTruthy();
    deleteButton.click();
    const newSearchesList = element.all(by.css('app-view-job-suggested-searches div.card div.card-body div div.row.mb-3'));
    const newCount = await newSearchesList.count();
    // Assert that the count of new list is less than the count of old list
    expect(newCount).toBeLessThan(oldCount);
  });
});
