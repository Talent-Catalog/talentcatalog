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

import {browser, by, element, ElementFinder, ExpectedConditions} from "protractor";
import {clickTabAndWait} from "../../job-home/job-home.e2e.spec";

describe('ViewJobComponent', () => {
  beforeEach(async () => {
    // Navigate to the page where the component is located
    await browser.get('/jobs');
  });
  it('should display view job component', async () => {
    await clickTabAndWait(1);
    const jobEntries = element.all(by.css('app-jobs-with-detail'));
    expect(await jobEntries.count()).toBeGreaterThan(0);
  });
  it('should toggle the starred status of a job', async () => {
    // Assuming the component selector is 'app-view-job'
    const viewJobComponent = element(by.tagName('app-view-job'));

    // Wait for the component to be present on the page
    await browser.wait(ExpectedConditions.presenceOf(viewJobComponent), 5000);

    // Assuming there's a button for toggling the starred status
    const toggleStarredButton = viewJobComponent.element(by.css('div.btn.btn-lg.pt-0'));
    // Wait for the button to be clickable
    await browser.wait(ExpectedConditions.elementToBeClickable(toggleStarredButton), 5000);

    // Get the initial starred status of the job
    const initialStarredStatus = await getStarredStatus(toggleStarredButton);
    // Click the button to toggle the starred status
    await toggleStarredButton.click();

    // Wait for the button to become clickable again
    await browser.wait(ExpectedConditions.elementToBeClickable(toggleStarredButton), 5000);

    // Get the updated starred status of the job
    const updatedStarredStatus = await getStarredStatus(toggleStarredButton);

    // Verify that the starred status has been toggled
    expect(updatedStarredStatus).not.toEqual(initialStarredStatus);

    // Click the button again to toggle back the starred status
    await toggleStarredButton.click();

    // Wait for the button to become clickable again
    await browser.wait(ExpectedConditions.elementToBeClickable(toggleStarredButton), 5000);

    // Get the final starred status of the job
    const finalStarredStatus = await getStarredStatus(toggleStarredButton);

    // Verify that the starred status has been toggled back to the initial status
    expect(finalStarredStatus).toEqual(initialStarredStatus);
  });


  async function getStarredStatus(toggleStarredButton:ElementFinder) {
    // Get the title attribute of the star element
    const starElement = toggleStarredButton.element(by.css('.fa-star'));
    return await starElement.getAttribute('title');
  }
});
