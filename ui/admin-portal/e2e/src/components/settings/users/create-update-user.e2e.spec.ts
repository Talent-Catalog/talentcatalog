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
import {searchByInput} from "./cleanup.e2e.spec";
import {config_test} from "../../../../../src/config-test";


async function openNewUserModal() {
  const addBtn = element(by.css('app-search-users button.btn.btn-primary'));
  await browser.wait(ExpectedConditions.visibilityOf(addBtn), 5000);
  addBtn.click();
}
describe('User Creation Test Suite', () => {
  beforeEach(async () => {
    browser.get(config_test.baseUrl+'/settings');
    await browser.wait(ExpectedConditions.invisibilityOf(element(by.css('.fa-spinner'))), 5000);
  });

  it('should fill out the form and submit to create new user', async () => {
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

  it('should update user data successfully', async () => {
    // Open the modal for updating user data
    await searchByInput('1','keyword','test@example.com');

    // Update user data in the modal
    await updateUserData();

    // You can add assertions here to verify that the user data was updated successfully,
    // such as checking if the updated values are displayed correctly on the page.
  });
});

// Helper function to update user data in the modal
async function updateUserData() {
  // Wait for the form fields to load
  await browser.wait(ExpectedConditions.visibilityOf(element(by.id('email'))), 5000);

  // Locate the input fields for updating user data
  const firstNameInput = element(by.id('firstName'));
  const lastNameInput = element(by.id('lastName'));


  await firstNameInput.clear();
  await firstNameInput.sendKeys('New First Name');

  await lastNameInput.clear();
  await lastNameInput.sendKeys('New Last Name');

  // Locate and click the Save button
  const saveButton = element(by.buttonText('Save'));
  await saveButton.click();

  // Wait for the modal to close (assuming it disappears after saving)
  await browser.wait(ExpectedConditions.invisibilityOf(element(by.css('.modal'))), 5000);
}
