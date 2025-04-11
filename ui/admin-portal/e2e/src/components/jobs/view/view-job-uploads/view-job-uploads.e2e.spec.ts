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

describe('ViewJobUploadsComponent', () => {
  beforeEach(async () => {
    // Navigate to the page where the component is located
    await browser.get('/jobs');
  });

  it('should display view job upload component', async () => {
    await clickTabAndWait(0);
    const jobEntries = element.all(by.css('app-jobs-with-detail'));
    expect(await jobEntries.count()).toBeGreaterThan(0);
    const myTabELement = element(by.css('app-jobs-with-detail app-view-job nav.nav-tabs.nav a:nth-child(3)'))
    await browser.wait(ExpectedConditions.elementToBeClickable(myTabELement), 5000);
    myTabELement.click();
  });
  it('should display job related uploads and perform actions', async () => {
    // Assuming app-view-job-uploads is the selector for the component
    const viewJobUploadsComponent = element(by.tagName('app-view-job-uploads'));

    // Wait for the component to be present on the page
    await browser.wait(ExpectedConditions.presenceOf(viewJobUploadsComponent), 5000);

    // Verify that the card header contains "Job related uploads"
    const cardHeader = viewJobUploadsComponent.element(by.css('.card-header'));
    expect(await cardHeader.getText()).toContain('Job related uploads');

    // Verify that there are upload sections present
    const uploadSections = viewJobUploadsComponent.all(by.css('.card-body .row')).first();
     // Assuming there's a button for adding/editing links
    const addEditLinkButton = uploadSections.element(by.css('button[title="Enter/change a link to the document"]'));
    await browser.wait(ExpectedConditions.elementToBeClickable(addEditLinkButton), 5000);

    // Cancel Functionality
    if (addEditLinkButton.isPresent()) {
      // Click the add/edit link button
       addEditLinkButton.click();
      // Perform additional actions as needed for entering/editing the link
      const modalDialog = element(by.css('ngb-modal-window app-input-link'));
      await browser.wait(ExpectedConditions.presenceOf(modalDialog), 5000);
      const cancelBtn = modalDialog.element(by.cssContainingText('.modal-footer .btn-primary','Cancel'))
      await browser.wait(ExpectedConditions.elementToBeClickable(cancelBtn), 5000);
      cancelBtn.click();
      await browser.wait(ExpectedConditions.invisibilityOf(modalDialog), 5000);

    }

    // Assuming there's a button for uploading files
      const uploadFileButton = uploadSections.element(by.css('button[title="Upload the document from your computer"]'));
      if (await uploadFileButton.isPresent()) {
        // Click the upload file button
        await uploadFileButton.click();
        // Perform additional actions as needed
        const modalDialog = element(by.css('ngb-modal-window app-file-selector'));
        await browser.wait(ExpectedConditions.presenceOf(modalDialog), 5000);
        const cancelBtn = modalDialog.element(by.css('.modal-footer .btn-accent-2'))
        await browser.wait(ExpectedConditions.elementToBeClickable(cancelBtn), 5000);
        cancelBtn.click();
        await browser.wait(ExpectedConditions.invisibilityOf(modalDialog), 5000);
      }
  });
});
