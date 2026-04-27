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

import {browser, by, element} from 'protractor';

describe('Reset Password Component', () => {

  beforeEach(async () => {
    await browser.get('/reset-password'); // Adjust the URL if needed
  });

  it('should display error message for invalid email', async () => {
    const emailInput = element(by.id('email'));
    const email = 'invalidemail';
    await emailInput.sendKeys(email);

    const emailErrorMessage = element(by.css('.text-danger'));
    expect(await emailErrorMessage.getText()).toContain('This field must contain a valid email address');
  });

  it('should send reset password email for valid email', async () => {
    const emailInput = element(by.id('email'));
    const email = 'test@example.com'; // Provide a valid email address
    await emailInput.sendKeys(email);

    const emailButton = element(by.css('button'));
    await emailButton.click();

    const successMessage = element(by.css('.alert-success'));
    expect(await successMessage.getText()).toContain('We have sent you an email with reset instructions.');
  });


});
