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
import {config_test} from "../../../../../src/config-test";


async function openNewUserModal() {
  const addBtn = element(by.css('app-search-users button.btn.btn-primary'));
  await browser.wait(ExpectedConditions.visibilityOf(addBtn), 5000);
  addBtn.click();
}
describe('User Deletion Test Suite', () => {
  beforeEach(async () => {
    browser.get(config_test.baseUrl+'/settings');
    await browser.wait(ExpectedConditions.invisibilityOf(element(by.css('.fa-spinner'))), 5000);
  });


  it('should delete user data successfully', async () => {
    // Open the modal for updating user data
    await searchByInput('2','keyword','test@example.com');

    // Update user data in the modal
    await deleteUser();

  });
});

async function deleteUser(){

  const appDeleteUserModal = element(by.css('app-confirmation'));
  await browser.wait(ExpectedConditions.visibilityOf(appDeleteUserModal), 5000);
  const deleteBtn = element(by.cssContainingText('app-confirmation button.btn.btn-primary','Ok'));
  await browser.wait(ExpectedConditions.visibilityOf(deleteBtn), 5000);
  deleteBtn.click();

  browser.wait(ExpectedConditions.stalenessOf(appDeleteUserModal), 5000).then(() => {
    // Check if modal is closed
    expect(appDeleteUserModal.isPresent()).toBeFalsy();

  }).catch(() => {
    // If modal is not closed within the specified time, log an error
    // Check if error dialog is displayed
    const errorDialog = element(by.css('.alert.alert-danger'));

    errorDialog.isPresent().then(isPresent => {
      if (isPresent) {
        // If error dialog is present, fail the test with an appropriate message
        fail('User not deleted');
      }
    });
    console.error('Modal did not close after user deletion');
  });
}
// Helper function to open the modal for updating user data
export async function searchByInput(liItem:string,searchId:string,searchText:string) {

  // Type 'test@example.com' into the search input
  element(by.css(`#${searchId}`)).sendKeys(`${searchText}`);

  // Wait for the search results to appear (adjust the timeout as needed)
  browser.wait(ExpectedConditions.visibilityOf(element(by.css('tbody tr'))), 5000);

  // Find all rows in the table
  const rows = element.all(by.css('tbody tr'));

  // Check if the email 'test@example.com' is present in any of the rows
  const found = rows.reduce((acc, row) => {
    return acc || row.element(by.css('td:nth-child(6)')).getText().then((text) => {
      return text.trim() === 'test@example.com';
    });
  }, false);

  // Assert that the email 'test@example.com' is found in one of the rows
  expect(found).toBeTruthy();

  const dropdownToggle = element.all(by.css('div.dropdown button.dropdown-toggle')).first();
  await dropdownToggle.click();
  const liElement = element(by.xpath(`/html/body/div/ul/li[${liItem}]`));
  await liElement.click();
}
