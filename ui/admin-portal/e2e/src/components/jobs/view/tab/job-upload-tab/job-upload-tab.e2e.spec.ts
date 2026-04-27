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

import {clickTabAndWait} from "../../../job-home/job-home.e2e.spec";
import {browser, by, element, ExpectedConditions} from "protractor";

describe('JobUploadTabComponent', () => {
  beforeEach(async () => {
    // Navigate to the page where the component is located
    await browser.get('/jobs');
  });
  it('should job upload tab', async () => {
    await clickTabAndWait(0);
    const jobEntries = element.all(by.css('app-jobs-with-detail'));
    expect(await jobEntries.count()).toBeGreaterThan(0);
    const myTabELement = element(by.css('app-jobs-with-detail app-view-job nav.nav-tabs.nav a:nth-child(3)'))
    await browser.wait(ExpectedConditions.elementToBeClickable(myTabELement), 5000);
    myTabELement.click();
  });
  it('should display job related uploads', async () => {
    // Assuming app-job-upload-tab is the selector for the component
    const jobUploadTabComponent = element(by.tagName('app-job-upload-tab'));

    // Wait for the component to be present on the page
    await browser.wait(ExpectedConditions.presenceOf(jobUploadTabComponent), 5000);

    // Verify that the card header contains "Job related uploads"
    const cardHeader = jobUploadTabComponent.element(by.css('.card-header'));
    expect(await cardHeader.getText()).toContain('Job related uploads');

    // Verify that there are upload sections present
    const uploadSections = jobUploadTabComponent.all(by.css('.card-body .row'));
    expect(await uploadSections.count()).toBeGreaterThan(0);

    // Loop through upload sections and verify their contents
    await uploadSections.each(async (uploadSection) => {
      // Verify that the section contains a label
      const sectionLabel = uploadSection.element(by.css('.col-10.col-sm .col-form-label'));
      expect(await sectionLabel.isPresent()).toBeTruthy();

      // Verify that the section label is not empty
      const labelText = await sectionLabel.getText();
      expect(labelText.trim()).not.toBe('');

      // Verify that the section contains buttons for adding/editing links and uploading files
      const buttons = uploadSection.all(by.css('.col-2.col-sm-auto button'));
      expect(await buttons.count()).toBe(2); // Assuming there are two buttons
    });
  });
});
