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
  readonly permissionDeniedMessage: Locator;
  readonly tryAgainButton: Locator;
  readonly profileError: Locator;
  readonly noCameraMessage: Locator;
  readonly scannerErrorMessage: Locator;

  readonly payloadReviewHeading: Locator;
  readonly payloadPreview: Locator;
  readonly confirmButton: Locator;
  readonly rescanButton: Locator;
  readonly submissionSuccessHeading: Locator;
  readonly submittedUnhcrNumber: Locator;

  readonly duplicateResultHeading: Locator;
  readonly duplicateUnhcrNumber: Locator;
  readonly duplicateGuidance: Locator;
  readonly submissionErrorPanel: Locator;
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

    /*
     * The Verify+ service card is a clickable div in the production markup.
     * Scope the locator to app-services and select the card by its unique text.
     */
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

    this.permissionDeniedMessage = this.scanner.getByText(
      'Camera access was denied. Please enable camera permission and try again.',
      {
        exact: true,
      },
    );

    this.tryAgainButton = this.scanner.getByRole('button', {
      name: 'Try again',
      exact: true,
    });

    this.profileError = this.profileComponent.locator('app-error');

    this.noCameraMessage = this.scanner.getByText(
      'No camera was detected on this device.',
      {
        exact: true,
      },
    );

    this.scannerErrorMessage = this.verifyPlusComponent.getByText(
      'A camera or scanner error occurred. Please refresh and try again.',
      {
        exact: true,
      },
    );

    this.payloadReviewHeading =
      this.verifyPlusComponent.getByRole(
        'heading',
        {
          name: 'Review scanned payload',
          exact: true,
        },
      );

    this.payloadPreview =
      this.verifyPlusComponent.locator(
        '.scan-result pre',
      );

    this.confirmButton =
      this.verifyPlusComponent
      .locator('tc-button')
      .filter({
        hasText: /^\s*Confirm\s*$/,
      })
      .locator('button');

    this.rescanButton =
      this.verifyPlusComponent
      .locator('tc-button')
      .filter({
        hasText: /^\s*Rescan\s*$/,
      })
      .locator('button');

    this.submissionSuccessHeading =
      this.verifyPlusComponent.getByRole(
        'heading',
        {
          name: 'Verification submitted',
          exact: true,
        },
      );

    this.submittedUnhcrNumber =
      this.verifyPlusComponent
      .locator('.scan-result')
      .filter({
        hasText: 'Verification submitted',
      })
      .locator('strong');

    this.duplicateResultHeading =
      this.verifyPlusComponent.getByRole(
        'heading',
        {
          name: 'Duplicate UNHCR number found',
          exact: true,
        },
      );

    this.duplicateUnhcrNumber =
      this.verifyPlusComponent
      .locator('.scan-result')
      .filter({
        hasText:
          'Duplicate UNHCR number found',
      })
      .locator('strong');

    this.duplicateGuidance =
      this.verifyPlusComponent.getByText(
        'You can rescan if needed.',
        {
          exact: true,
        },
      );

    this.submissionErrorPanel =
      this.verifyPlusComponent
      .locator('p.text-danger')
      .filter({
        hasText:
          'If this QR code is a valid UNHCR Verify+ code, please rescan.',
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

    try {
      await expect(
        this.servicesTab,
        'Expected the Services tab to become available',
      ).toBeVisible({
        timeout: 30_000,
      });
    } catch (error) {
      await this.throwIfProfileHasError();

      throw error;
    }

    await this.servicesTab.click();

    await expect(
      this.servicesContainer,
      'Expected the candidate Services tab to load',
    ).toBeVisible({
      timeout: 30_000,
    });

    await expect(
      this.verifyPlusServiceCard,
      'Expected Verify+ to be available for the authenticated GRN candidate',
    ).toBeVisible();
  }

  /**
   * Throws a useful error when the profile rendered an application error instead
   * of the expected Services tab.
   */
  private async throwIfProfileHasError(): Promise<void> {
    const errorText = (
      await this.profileError.textContent()
    )?.trim();

    if (errorText) {
      throw new Error(
        `Candidate profile failed to load Services: ${errorText}`,
      );
    }
  }
  /**
   * Confirms the currently decoded Verify+ payload.
   */
  async confirmPayload(): Promise<void> {
    await expect(
      this.confirmButton,
      'Expected the Confirm button after decoding a QR payload',
    ).toBeVisible();

    await expect(
      this.confirmButton,
    ).toBeEnabled();

    await this.confirmButton.click();
  }
  /**
   * Starts the Verify+ camera scanner.
   */
  async enableCamera(): Promise<void> {
    await expect(
      this.enableCameraButton,
      'Expected the Enable camera button before scanning',
    ).toBeVisible();

    await this.enableCameraButton.click();
  }

  /**
   * Retries camera access after an earlier camera failure.
   */
  async retryCamera(): Promise<void> {
    await expect(
      this.tryAgainButton,
      'Expected the Try again button after camera permission denial',
    ).toBeVisible();

    await this.tryAgainButton.click();
  }

  /**
   * Opens Verify+ from the Services list.
   *
   * The production Verify+ card is a clickable div rather than a native button.
   * Dispatching the event avoids browser-specific actionability waits caused by
   * layout movement while service-card content and images finish rendering.
   */
  async openVerifyPlus(): Promise<void> {
    await expect(
      this.verifyPlusServiceCard,
      'Expected the Verify+ service card before opening it',
    ).toBeVisible({
      timeout: 30_000,
    });

    await this.verifyPlusServiceCard.dispatchEvent('click');

    await expect(
      this.verifyPlusComponent,
      'Expected the Verify+ component to open',
    ).toBeVisible({
      timeout: 20_000,
    });

    await expect(this.title).toBeVisible();
  }

  /**
   * Returns from Verify+ to the candidate Services list.
   *
   * The custom tc-button can trigger browser-specific scrolling waits on mobile
   * WebKit. Dispatching the click directly still exercises the Angular output
   * event and parent component navigation behavior.
   */
  async returnToServices(): Promise<void> {
    await expect(
      this.backButton,
      'Expected the Verify+ Back button to be available',
    ).toBeAttached();

    await this.backButton.dispatchEvent('click');

    await expect(
      this.verifyPlusComponent,
      'Expected Verify+ to close after clicking Back',
    ).not.toBeVisible({
      timeout: 20_000,
    });

    await expect(
      this.verifyPlusServiceCard,
      'Expected the Services list after clicking Back',
    ).toBeVisible({
      timeout: 20_000,
    });
  }

  /**
   * Clears the current decoded payload and starts another camera scan.
   */
  async rescanPayload(): Promise<void> {
    await expect(
      this.rescanButton,
      'Expected Rescan to be available after decoding a QR payload',
    ).toBeVisible();

    await expect(
      this.rescanButton,
    ).toBeEnabled();

    await this.rescanButton.click();
  }
}
