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
export async function clickTabAndWait(tabIndex: number) {
  const tabElement = element(by.id(`ngb-nav-${tabIndex}`));
  await browser.wait(ExpectedConditions.elementToBeClickable(tabElement), 5000);
  await tabElement.click();
  const isActive = await tabElement.getAttribute('class').then(classes => classes.includes('active'));
  expect(isActive).toBe(true);
  return element(by.id(`ngb-nav-${tabIndex}-panel`));
}
describe('Dashboard - Viewing Live Jobs', () => {
  beforeEach(() => {
    browser.get('/jobs');
  });

  it('should display live jobs with associated details', async () => {
    const liveJobsContainer = await clickTabAndWait(0);

    const liveJobEntries = element.all(by.css('app-jobs-with-detail'));
    expect(await liveJobEntries.count()).toBeGreaterThan(0);

    await liveJobEntries.each(async (jobEntry) => {
      expect(await jobEntry.element(by.css('.detail-panel')).isPresent()).toBeTruthy();
    });
  });

  it('should display a list of starred jobs when Starred Jobs tab is selected', async () => {
    const starredJobsContainer = await clickTabAndWait(1);
    const starredJobsList = element.all(by.css('app-jobs-with-detail'));

    expect(await starredJobsList.isPresent()).toBeTruthy();

    await starredJobsList.each(async (starredJobEntry) => {
      const chatReadStatusElement = starredJobEntry.element(by.css('app-chat-read-status'));
      const isChatReadStatusDisplayed = await chatReadStatusElement.isPresent();
      expect(isChatReadStatusDisplayed).toBe(true);
    });
  });

  it('should display a list of jobs created by the partner organization', async () => {
    const jobListContainer = await clickTabAndWait(2);
    const jobList = element.all(by.css('app-jobs-with-detail .table tbody tr'));
    expect(await jobList.count()).toBeGreaterThan(0);
  });

  it('should display candidate cases associated with jobs created by the partner organization', async () => {
    const casesContainer = await clickTabAndWait(3);
    const cases = element.all(by.css('app-candidate-opps-with-detail .card-body'));
    expect(await cases.count()).toBeGreaterThan(0);
  });

  it('should display TBB Source Cases associated with jobs created by the partner organization', async () => {
    const tbbSourceContainer = await clickTabAndWait(4);
    const cases = element.all(by.css('app-candidate-opps-with-detail .card-body'));
    expect(await cases.count()).toBeGreaterThan(0);
  });

  it('should display a form or interface for creating a new job', async () => {
    const newJobFormContainer = await clickTabAndWait(5);
    const newJobForm = element(by.css('app-new-job form'));
    expect(await newJobForm.isPresent()).toBeTruthy();
  });
});
