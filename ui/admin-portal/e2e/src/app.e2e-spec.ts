/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

import {AppPage} from './app.po';

import {browser, by, element, ElementFinder, ExpectedConditions} from 'protractor';
import {config_test} from "../../src/config-test";
import {login, logout} from "./components/account/login/login.e2e.spec";

describe('AppComponent', () => {
  beforeEach(async () => {
    await browser.get('/');
  });

  it('should display header when user is logged in', async () => {
    await login(config_test.credentials.username, config_test.credentials.password, config_test.credentials.totpToken);
    // Assert that header is displayed
    const header = element(by.tagName('app-header'));
    await browser.wait(ExpectedConditions.presenceOf(header), 5000);
    // Assert to be true because user logged in
    expect(await header.isPresent()).toBeTruthy();
  });

  it('should not display header when user is logged out', async () => {
    await logout();    // Navigate to home page
    await browser.get('/');

    // Assert that header is not displayed
    const header = element(by.tagName('app-header'));
    expect(await header.isPresent()).toBeFalsy();
  });

  // Add more tests for browser title updates and logout redirection as per your requirements
});
