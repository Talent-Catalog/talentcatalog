// login.e2e.spec.ts

import {browser, by, element, ElementFinder, ExpectedConditions} from 'protractor';
import {config_test} from "../../../../../src/config-test";
// Helper function to perform login
export async function login(username: string, password: string, totpToken: string): Promise<void> {
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

  // Wait for login to complete
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
    await browser.get('/login');
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
    await logout();
    // Assert that user is redirected to the login page
    expect(await browser.getCurrentUrl()).toContain('/login');
  });
});
