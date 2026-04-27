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
import {clickTabAndWait} from "../../../job-home/job-home.e2e.spec";

describe('JobSuggestedSearchesTabComponent', () => {
  beforeEach(async () => {
    // Navigate to the page where the component is located
    await browser.get('/jobs');
  });
  it('should display suggested searches tab', async () => {
    await clickTabAndWait(0);
    const jobEntries = element.all(by.css('app-jobs-with-detail'));
    expect(await jobEntries.count()).toBeGreaterThan(0);
    const myTabELement = element(by.css('app-jobs-with-detail app-view-job nav.nav-tabs.nav a:nth-child(2)'))
    await browser.wait(ExpectedConditions.elementToBeClickable(myTabELement), 5000);
    myTabELement.click();
  });
  it('should display suggested searches details', async () => {
    // Assuming app-view-job-suggested-searches is the selector for the component
    const jobSuggestedSearchesComponent = element(by.tagName('app-view-job-suggested-searches'));

    // Wait for the component to be present on the page
    await browser.wait(ExpectedConditions.presenceOf(jobSuggestedSearchesComponent), 5000);

    // Verify that the card header contains "Suggested searches"
    const cardHeader = jobSuggestedSearchesComponent.element(by.css('.card-header'));
    expect(await cardHeader.getText()).toContain('Suggested searches');

    // Verify that the "Create a new suggested search" button is present
    const createSearchButton = jobSuggestedSearchesComponent.element(by.css('button[title="Create a new suggested search"]'));
    expect(await createSearchButton.isPresent()).toBeTruthy();

    // Verify that the card body contains some text (adjust to your specific case)
    const cardBody = jobSuggestedSearchesComponent.element(by.css('.card-body'));
    expect(await cardBody.getText()).not.toBe('');

    // Check if there are any search items present
    const searchItems = cardBody.all(by.css('.col-10.col-sm.edit-padding a'));
    if(await searchItems.count()>0){
      expect(await searchItems.count()).toBeGreaterThan(0);
      // Loop through search items and verify they are not empty
      await searchItems.each(async (searchItem) => {
        const searchText = await searchItem.getText();
        expect(searchText.trim()).not.toBe('');
      });
    }
  });
});
