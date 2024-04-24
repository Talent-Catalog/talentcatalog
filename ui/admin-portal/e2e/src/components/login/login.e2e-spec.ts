import { browser, by, element, ExpectedConditions as EC } from "protractor";

describe('login', () => {
  beforeEach(async () => {
    await browser.get('/');
  });

  // it('should disable the login button when fields are empty', async () => {
  //   // Find the login button
  //   const loginButton = element(by.css('.btn-primary'));
  //
  //   // Check if the login button is initially disabled
  //   expect(await loginButton.isEnabled()).toBe(false);
  //
  //   // Find the username and password input fields
  //   const usernameInput = element(by.css('[formControlName="username"]'));
  //   const passwordInput = element(by.css('[formControlName="password"]'));
  //
  //   // Clear username and password fields
  //   await usernameInput.clear();
  //   await passwordInput.clear();
  //
  //   // Check if the login button is still disabled after clearing the fields
  //   expect(await loginButton.isEnabled()).toBe(false);
  //
  //   // Enter valid username and password
  //   await usernameInput.sendKeys('valid_username');
  //   await passwordInput.sendKeys('valid_password');
  //
  //   // Check if the login button is enabled after entering valid data
  //   expect(await loginButton.isEnabled()).toBe(true);
  // });
  //
  // it('should display validation error for invalid credentials', async () => {
  //   // Find the username and password input fields
  //   const usernameInput = element(by.css('[formControlName="username"]'));
  //   const passwordInput = element(by.css('[formControlName="password"]'));
  //
  //   // Enter invalid username and password
  //   await usernameInput.sendKeys('invalid_username');
  //   await passwordInput.sendKeys('invalid_password');
  //
  //   // Find the login button
  //   const loginButton = element(by.css('.btn-primary'));
  //
  //   // Click on the login button
  //   await loginButton.click();
  //   await browser.sleep(1000);
  //
  //   // Find the error message element
  //   const errorMessage = element(by.css('.alert-danger'));
  //   await browser.sleep(1000);
  //
  //   // Assert that the error message is displayed
  //   expect(await errorMessage.isPresent()).toBe(true);
  //  });


  it('should not display validation error when valid credentials are entered', async () => {
    // Find the username and password input fields
    const usernameInput = element(by.css('[formControlName="username"]'));
    const passwordInput = element(by.css('[formControlName="password"]'));
    // Enter valid username and password
    await usernameInput.sendKeys(process.env.TEST_USERNAME);
    await passwordInput.sendKeys(process.env.TEST_PASSWORD);

    // Find the login button
    const loginButton = element(by.css('.btn-primary'));

    // Click on the login button
    await loginButton.click();

    // Wait for the page to load
    await browser.wait(EC.urlContains('/job'), 5000);

    // Assert that there are no validation error messages displayed
    const errorMessages = element.all(by.css('.alert-danger'));
    expect(await errorMessages.count()).toBe(0);
  });


});
