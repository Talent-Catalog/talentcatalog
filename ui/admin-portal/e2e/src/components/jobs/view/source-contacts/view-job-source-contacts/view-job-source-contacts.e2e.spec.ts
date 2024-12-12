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

import {browser, by, element, ExpectedConditions,} from 'protractor';
import {clickTabAndWait} from "../../../job-home/job-home.e2e.spec";

describe('Job-Source-Contacts Component', () => {
  beforeEach(()=>{
    browser.get('/jobs')
  })
  // Test cases for UI elements and basic functionality
  it('should display live jobs with associated details', async () => {
    await clickTabAndWait(1);
    const liveJobEntries = element.all(by.css('app-jobs-with-detail'));
    expect(await liveJobEntries.count()).toBeGreaterThan(0);
  });
  it('should emit the correct event when selecting a source partner', async () => {
    // Navigate to the page containing the component
    // Simulate the component being in a state where it has loaded and source partners are displayed
    const myTabELement = element(by.css('app-jobs-with-detail app-view-job nav.nav-tabs.nav a:nth-child(5)'))
    await browser.wait(ExpectedConditions.elementToBeClickable(myTabELement), 5000);
    myTabELement.click();
    // Identify the source partner to select (assuming the first one for simplicity)
    const sourcePartnerRow = element.all(by.css('app-view-job-source-contacts tbody tr')).first();

    // Get the name of the source partner before selection
    const originalSourcePartnerName = await sourcePartnerRow.element(by.css('td:nth-child(3)')).getText();
    await browser.wait(ExpectedConditions.elementToBeClickable(sourcePartnerRow), 5000);

    // Simulate selecting the source partner by clicking on its row
    await sourcePartnerRow.click();
     // Wait for the event emission to complete (assuming it's asynchronous)
    await browser.sleep(1000); // Adjust the delay as needed

    // Get the emitted source partner name from the component's output
    const emittedSourcePartnerName = await element(by.css('app-job-source-contacts-with-chats .detail-panel.side-panel-color h5')).getText();

    // Assert that the emitted source partner name matches the selected source partner's name
    expect(emittedSourcePartnerName).toEqual('Chat with '+originalSourcePartnerName);
  });
});
