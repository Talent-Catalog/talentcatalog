import { browser, by, element, ElementFinder, ExpectedConditions } from 'protractor';
import { UserService } from "../../../../../src/app/services/user.service";
import { login } from "../login/login.e2e.spec";
import { config_test } from "../../../../../src/config-test";

// Perform login before any test cases in any describe block
beforeAll(async () => {
  // await login(config_test.credentials.username, config_test.credentials.password, config_test.credentials.totpToken);
  // Now you can proceed with other actions after login
});

// Helper function to open change password modal
async function openChangePasswordModal() {
  const dropdownToggle = element.all(by.css('div.dropdown button.dropdown-toggle')).first();
  await dropdownToggle.click();
  const changePasswordElement = element(by.xpath('/html/body/div/ul/li[4]'));
  await changePasswordElement.click();
}

// Helper function to wait for modal to close
async function waitForModalToClose() {
  const modalTitle = element(by.css('.modal-title'));
  await browser.wait(async () => {
    return !(await modalTitle.isPresent());
  }, 5000);
}

describe('Change Password Component', () => {
  beforeEach(async () => {
    browser.get('/settings');
    await browser.wait(ExpectedConditions.invisibilityOf(element(by.css('.fa-spinner'))), 5000);
  });

  it('should change the password', async () => {
    await openChangePasswordModal();
    const updateButton = element(by.css('app-change-password button.btn-primary'));
    await browser.wait(ExpectedConditions.visibilityOf(updateButton), 5000);
    await updateButton.click();
    const errorMessage = element(by.css('.alert.alert-danger'));
    expect(errorMessage.isPresent()).toBeTruthy();
  });

  it('should update password successfully with valid input', async () => {
    await openChangePasswordModal();
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
    await openChangePasswordModal();
    const closeButton = element(by.css('button.btn-close'));
    await closeButton.click();
    await waitForModalToClose();
    expect(await element(by.css('.modal-title')).isPresent()).toBeFalsy();
  });
});
