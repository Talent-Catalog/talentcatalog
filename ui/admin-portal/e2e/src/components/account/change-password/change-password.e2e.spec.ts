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
import {config_test} from "../../../../../src/config-test";
import {searchByInput} from "../../settings/users/cleanup.e2e.spec";

// Helper function to wait for modal to close
async function waitForModalToClose() {
  const modalTitle = element(by.css('.modal-title'));
  await browser.wait(async () => {
    return !(await modalTitle.isPresent());
  }, 5000);
}

describe('Change Password Component', () => {
  beforeEach(async () => {
    browser.get(config_test.baseUrl+'/settings');
    await browser.wait(ExpectedConditions.invisibilityOf(element(by.css('.fa-spinner'))), 5000);
  });

  it('should display error with invalid fields', async () => {
    await searchByInput('4','keyword','test@example.com')

    const updateButton = element(by.css('app-change-password button.btn-primary'));
    await browser.wait(ExpectedConditions.visibilityOf(updateButton), 5000);
    await updateButton.click();
    const errorMessage = element(by.css('.alert.alert-danger'));
    expect(errorMessage.isPresent()).toBeTruthy();
  });

  it('should update password successfully with valid input', async () => {
    await searchByInput('4','keyword','test@example.com')
    const passwordInput = element(by.css('input#password'));
    const passwordConfirmationInput = element(by.css('input#passwordConfirmation'));
    const updateButton = element(by.css('app-change-password button.btn-primary'));
    await browser.wait(ExpectedConditions.visibilityOf(updateButton), 5000);
    await passwordInput.sendKeys('newPassword');
    await passwordConfirmationInput.sendKeys('newPassword');
    await updateButton.click();
    await waitForModalToClose();
    expect(await element(by.css('.modal-title')).isPresent()).toBeFalsy();
  });

  it('should close modal without updating password when dismissed', async () => {
    await searchByInput('4','keyword','test@example.com')
    const closeButton = element(by.css('button.btn-close'));
    await closeButton.click();
    await waitForModalToClose();
    expect(await element(by.css('.modal-title')).isPresent()).toBeFalsy();
  });
});
