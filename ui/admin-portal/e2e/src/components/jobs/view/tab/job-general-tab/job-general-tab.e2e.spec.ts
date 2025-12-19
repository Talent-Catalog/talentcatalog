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

// job-general-tab.e2e-spec.ts

import {browser, by, element, ExpectedConditions, ExpectedConditions as EC} from 'protractor';
import {clickTabAndWait} from "../../../job-home/job-home.e2e.spec";

describe('JobGeneralTabComponent', () => {

  beforeEach(() => {
    browser.get('/jobs');
  });

  it('should display job general tab', async () => {
    await clickTabAndWait(0);
    const jobEntries = element.all(by.css('app-jobs-with-detail'));
    expect(await jobEntries.count()).toBeGreaterThan(0);
    const myTabELement = element(by.css('app-jobs-with-detail app-view-job nav.nav-tabs.nav a:nth-child(1)'))
    await browser.wait(ExpectedConditions.elementToBeClickable(myTabELement), 5000);
    myTabELement.click();
  });
  it('should display general tab information when clicked', async () => {
    // Wait for the general tab content to be visible
    const generalTabContent = element(by.css('app-job-general-tab .card'));
    browser.wait(EC.visibilityOf(generalTabContent), 5000);


    // Check if certain elements or attributes are present in the general tab content
    const isCountryPresent = await generalTabContent.element(by.cssContainingText('.col-form-label', 'Country')).isPresent();
    expect(isCountryPresent).toBe(true);

    const isEmployerPresent = await generalTabContent.element(by.cssContainingText('.col-form-label', 'Employer')).isPresent();
    expect(isEmployerPresent).toBe(true);

    const isEvergreenPresent = await generalTabContent.element(by.cssContainingText('.col-form-label', 'Evergreen')).isPresent();
    expect(isEvergreenPresent).toBe(true);

  });
});
