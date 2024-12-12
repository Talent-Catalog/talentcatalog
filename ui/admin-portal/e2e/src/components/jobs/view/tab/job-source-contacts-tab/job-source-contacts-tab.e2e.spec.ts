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

describe('Job Source Contacts with Chats Tab Component', () => {
  beforeEach(async () => {
    // Navigate to the page where the component is located
    await browser.get('/jobs');
  });
  it('should display source contacts tab', async () => {
    await clickTabAndWait(0);
    const jobEntries = element.all(by.css('app-jobs-with-detail'));
    expect(await jobEntries.count()).toBeGreaterThan(0);
    const myTabELement = element(by.css('app-jobs-with-detail app-view-job nav.nav-tabs.nav a:nth-child(5)'))
    await browser.wait(ExpectedConditions.elementToBeClickable(myTabELement), 5000);
    myTabELement.click();
  });
  it('should display the list of job source contacts', async () => {
    // Assuming app-job-source-contacts-with-chats is the selector for the component
    const jobSourceContactsComponent = element(by.tagName('app-job-source-contacts-with-chats'));

    // Wait for the component to be present on the page
    await browser.wait(ExpectedConditions.presenceOf(jobSourceContactsComponent), 5000);

    // Assuming jobSourceContactsComponent contains the job source contacts table
    const jobSourceContactsTable = jobSourceContactsComponent.element(by.css('.table-responsive table'));

    // Verify that the job source contacts table is displayed
    expect(await jobSourceContactsTable.isPresent()).toBeTruthy();

    // Assuming jobSourceContactsComponent contains a specific job source contact row
    const specificJobSourceContactRow = jobSourceContactsComponent.element(by.cssContainingText('.table-responsive table tbody tr', 'Talent Beyond Boundaries (Jordan,Lebanon)'));

    // Verify that the specific job source contact row is displayed
    expect(await specificJobSourceContactRow.isPresent()).toBeTruthy();
   });
});
