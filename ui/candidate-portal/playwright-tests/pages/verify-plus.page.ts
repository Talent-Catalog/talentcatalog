/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {expect, Locator, Page} from '@playwright/test';

/**
 * Page object representing the candidate portal Verify+ feature.
 *
 * The production application does not currently expose dedicated E2E test
 * selectors. This page object therefore uses component boundaries, visible
 * text and existing icon classes.
 */
export class VerifyPlusPage {
  readonly page: Page;

  readonly profileComponent: Locator;
  readonly servicesTab: Locator;
  readonly servicesContainer: Locator;
  readonly verifyPlusServiceCard: Locator;

  readonly verifyPlusComponent: Locator;
  readonly title: Locator;
  readonly description: Locator;
  readonly scanner: Locator;
  readonly enableCameraButton: Locator;
  readonly backButton: Locator;

  /**
   * Creates locators for the candidate profile and Verify+ feature.
   *
   * @param page Playwright page belonging to the current test
   */
  constructor(page: Page) {
    this.page = page;

    this.profileComponent = page.locator('app-view-candidate');

    /*
     * The Services text is hidden on mobile viewports. The handshake icon
     * remains present on every viewport, so use it to locate the tab link.
     */
    this.servicesTab = this.profileComponent
    .locator('nav a')
    .filter({
      has: page.locator('i.fa-handshake'),
    });

    this.servicesContainer = page.locator('app-services');
    
    this.verifyPlusServiceCard = this.servicesContainer
    .locator('.service-card')
    .filter({
      hasText: 'UNHCR Verify+',
    });

    this.verifyPlusComponent = page.locator('app-verify-plus');

    this.title = this.verifyPlusComponent.getByRole('heading', {
      level: 2,
      name: 'UNHCR Verify+',
      exact: true,
    });

    this.description = this.verifyPlusComponent.getByText(
      'Scan your UNHCR Verify+ card QR code to capture the encoded payload.',
      {
        exact: true,
      },
    );

    this.scanner = this.verifyPlusComponent.locator(
      'app-verify-plus-scanner',
    );

    this.enableCameraButton = this.scanner.getByRole('button', {
      name: 'Enable camera',
      exact: true,
    });

    /*
     * The Back control is rendered by tc-button and contains an icon.
     * Locate the tc-button host by its DOM text and then select the native
     * button inside it. This avoids accessible-name differences between
     * Chromium, Firefox and WebKit.
     */
    this.backButton = this.verifyPlusComponent
    .locator('tc-button')
    .filter({
      hasText: /^\s*Back\s*$/,
    })
    .locator('button');
  }

  /**
   * Opens the candidate profile and selects the Services tab.
   *
   * The test deliberately does not navigate directly with `?tab=Services`.
   * The Services tab is rendered asynchronously after candidate and service
   * eligibility requests complete. Waiting for and clicking the rendered tab
   * avoids a race between the query parameter and ng-bootstrap navigation.
   */
  async gotoServices(): Promise<void> {
    await this.page.goto('/profile');

    await expect(
      this.page,
      'Expected authentication to allow access to the profile route',
    ).toHaveURL(/\/profile(?:\?|$)/);

    await expect(
      this.profileComponent,
      'Expected the candidate profile component to load',
    ).toBeVisible();

    await expect(
      this.servicesTab,
      'Expected the Services tab to become available',
    ).toBeVisible({
      timeout: 20_000,
    });

    await this.servicesTab.click();

    await expect(
      this.servicesContainer,
      'Expected the candidate Services tab to load',
    ).toBeVisible({
      timeout: 20_000,
    });

    await expect(
      this.verifyPlusServiceCard,
      'Expected Verify+ to be available for the authenticated GRN candidate',
    ).toBeVisible();
  }

  /**
   * Opens Verify+ from the Services list.
   */
  async openVerifyPlus(): Promise<void> {
    await this.verifyPlusServiceCard.click();

    await expect(
      this.verifyPlusComponent,
      'Expected the Verify+ component to open',
    ).toBeVisible();

    await expect(this.title).toBeVisible();
  }

  /**
   * Returns from Verify+ to the candidate Services list.
   */
  async returnToServices(): Promise<void> {
    await expect(
      this.backButton,
      'Expected the Verify+ Back button to be available',
    ).toBeVisible();

    await this.backButton.click();

    await expect(
      this.verifyPlusServiceCard,
      'Expected the Services list after clicking Back',
    ).toBeVisible();

    await expect(
      this.verifyPlusComponent,
      'Expected Verify+ to close after clicking Back',
    ).not.toBeVisible();
  }
}
