/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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


async function openNewUserModal() {
  const addBtn = element(by.css('app-search-users button.btn.btn-primary'));
  await browser.wait(ExpectedConditions.visibilityOf(addBtn), 5000);
  addBtn.click();
}
describe('User Creation Test Suite', () => {
  beforeEach(async () => {
    browser.get('/settings');
    await browser.wait(ExpectedConditions.invisibilityOf(element(by.css('.fa-spinner'))), 5000);
  });

  it('should fill out the form and submit', async () => {
    const addBtn = element(by.css('app-search-users button.btn.btn-primary'));
    await browser.wait(ExpectedConditions.visibilityOf(addBtn), 5000);
    addBtn.click();
    const appCreateUserModal = element(by.css('app-create-update-user'));
    await browser.wait(ExpectedConditions.visibilityOf(appCreateUserModal), 5000);

    // Fill out the form fields
    element(by.id('email')).sendKeys('test@example.com');
    element(by.id('username')).sendKeys('testuser');
    element(by.id('password')).sendKeys('testpassword');
    element(by.id('firstName')).sendKeys('Test');
    element(by.id('lastName')).sendKeys('User');

    // Select partner from dropdown
    const partnerDropdown = element(by.id('partner'));
    browser.wait(ExpectedConditions.presenceOf(partnerDropdown), 5000, 'Partner dropdown is not present');
    // Select role from dropdown
    partnerDropdown.click();
    element(by.cssContainingText('.ng-option-label', 'Talent Beyond Boundaries')).click(); // Assuming 'System Admin' is the desired role

    // Select role from dropdown
    const roleDropdown = element(by.id('role'));
    browser.wait(ExpectedConditions.presenceOf(roleDropdown), 5000, 'Role dropdown is not present');
    // Select role from dropdown
    roleDropdown.click();
    element(by.cssContainingText('.ng-option-label', 'System Admin')).click(); // Assuming 'System Admin' is the desired role

    // Uncheck MFA authentication checkbox
    const mfaCheckbox = element(by.id('usingMfa'));
    mfaCheckbox.isSelected().then(selected => {
      if (selected) {
        mfaCheckbox.click();
      }
    });

    // Submit the form
    element(by.buttonText('Save')).click();

    browser.wait(ExpectedConditions.stalenessOf(appCreateUserModal), 5000).then(() => {
      // Check if modal is closed
      expect(appCreateUserModal.isPresent()).toBeFalsy();

    }).catch(() => {
      // If modal is not closed within the specified time, log an error
      // Check if error dialog is displayed
      const errorDialog = element(by.css('.alert.alert-danger'));

      errorDialog.isPresent().then(isPresent => {
        if (isPresent) {
          // If error dialog is present, fail the test with an appropriate message
          fail('User registration failed');
        }
      });
      console.error('Modal did not close after user registration');
    });
  });
});
