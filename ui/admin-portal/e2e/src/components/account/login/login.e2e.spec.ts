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

import {browser, by, element, ElementFinder, ExpectedConditions} from 'protractor';
import {config_test} from "../../../../../src/config-test";
export async function login(username: string, password: string, totpToken: string): Promise<void> {
  await browser.get(config_test.baseUrl+'/login');

  const usernameInput = element(by.css('input[formControlName="username"]'));
  const passwordInput = element(by.css('input[formControlName="password"]'));
  const totpTokenInput = element(by.css('input[formControlName="totpToken"]'));
  const loginButton = element(by.css('button[type="submit"]'));

  await usernameInput.clear();
  await usernameInput.sendKeys(username);
  await passwordInput.clear();
  await passwordInput.sendKeys(password);
  await totpTokenInput.clear();
  await totpTokenInput.sendKeys(totpToken);
  await loginButton.click();

  // Wait for login to complete and navigate to the settings page
   await browser.wait(ExpectedConditions.urlContains('/jobs'), 5000);
}


// Helper function to perform logout
export async function logout(): Promise<void> {
  const dropdownToggle = element(by.css('li[ngbdropdown]'));
  await dropdownToggle.click();

  // Wait for logout button to be present
  const logoutButton = element(by.css('button.dropdown-item'));
  await browser.wait(ExpectedConditions.presenceOf(logoutButton), 5000);

  // Click on the logout button
  await logoutButton.click();

  // Wait for logout to complete and check if user is redirected to login page
  await browser.wait(ExpectedConditions.urlContains('/login'), 5000);
}
describe('Login Component', () => {
  let usernameInput: ElementFinder;
  let passwordInput: ElementFinder;
  let totpTokenInput: ElementFinder;
  let loginButton: ElementFinder;
  let errorMessage: ElementFinder;
  let dropdownToggle: ElementFinder;

  beforeAll(async () => {
    await browser.get(config_test.baseUrl+'/login');
    usernameInput = element(by.css('input[formControlName="username"]'));
    passwordInput = element(by.css('input[formControlName="password"]'));
    totpTokenInput = element(by.css('input[formControlName="totpToken"]'));
    loginButton = element(by.css('button[type="submit"]'));
    errorMessage = element(by.css('.alert.alert-danger'));
    dropdownToggle = element(by.css('li[ngbdropdown]'));
  });

  it('should display error message for invalid credentials', async () => {
    await usernameInput.sendKeys('invalid_username');
    await passwordInput.sendKeys('invalid_password');
    await totpTokenInput.sendKeys('invalid_token');
    await loginButton.click();

    expect(errorMessage.isPresent()).toBeTruthy();
  });


  it('should login and then logout', async () => {
    await login(config_test.credentials.username, config_test.credentials.password, config_test.credentials.totpToken);
    // await logout();
    // Assert that user is redirected to the login page
    expect(await browser.getCurrentUrl()).toContain('/jobs');
  });
});
