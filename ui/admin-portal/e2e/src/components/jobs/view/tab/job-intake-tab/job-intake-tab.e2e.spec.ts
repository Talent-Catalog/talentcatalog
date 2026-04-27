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

describe('JobIntakeTabComponent', () => {
  beforeEach(() => {
    // Navigate to the page where the component is rendered
    browser.get('/jobs');
  });
  it('should display intake tab', async () => {
    await clickTabAndWait(0);
    const jobEntries = element.all(by.css('app-jobs-with-detail'));
    expect(await jobEntries.count()).toBeGreaterThan(0);
    const myTabELement = element(by.css('app-jobs-with-detail app-view-job nav.nav-tabs.nav a:nth-child(4)'))
    await browser.wait(ExpectedConditions.elementToBeClickable(myTabELement), 5000);
    myTabELement.click();
  });



  it('should display Job Opportunity Intake (JOI) heading', async () => {
    // Check if JOI heading is displayed
    const joiHeading = element(by.css('app-job-intake-tab h5.mb-4'));
    await browser.wait(ExpectedConditions.visibilityOf(joiHeading), 5000);
    expect(joiHeading.isPresent()).toBeTruthy();
    expect(joiHeading.getText()).toContain('Job Opportunity Intake (JOI)');
  });

  it('should display employer panels correctly', async () => {
    // Check if employer panels are displayed correctly
    const employerDetailsPanel = element(by.id('employer-details'));
    expect(await employerDetailsPanel.isPresent()).toBeTruthy();

    const positionDetailsPanel = element(by.id('position-details'));
    expect(await positionDetailsPanel.isPresent()).toBeTruthy();

    const immigrationConsiderationPanel = element(by.id('immigration-considerations'));
    expect(await immigrationConsiderationPanel.isPresent()).toBeTruthy();
  });
});
