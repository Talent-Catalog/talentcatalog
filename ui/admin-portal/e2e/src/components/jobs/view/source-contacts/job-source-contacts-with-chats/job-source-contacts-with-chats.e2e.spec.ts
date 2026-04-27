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

import {browser, by, element, ExpectedConditions, ExpectedConditions as EC} from 'protractor';
import {clickTabAndWait} from "../../../job-home/job-home.e2e.spec";

describe('Selecting Source Partner', () => {

  beforeEach(() => {
    // Navigate to the page containing the component
    browser.get('/jobs');
  });
  // Test cases for UI elements and basic functionality
  it('should display live jobs with associated details', async () => {
    await clickTabAndWait(1);
    const liveJobEntries = element.all(by.css('app-jobs-with-detail'));
    expect(await liveJobEntries.count()).toBeGreaterThan(0);
  });
  it('should display chat with selected source partner', async () => {
    const myTabELement = element(by.id('ngb-nav-10'));
    await browser.wait(ExpectedConditions.elementToBeClickable(myTabELement), 5000);
    myTabELement.click();
    const sourcePartner = element(by.css('app-view-job-source-contacts table tbody tr:nth-child(1) td:nth-child(3)')); // Click on the dropdown
    await browser.wait(ExpectedConditions.elementToBeClickable(sourcePartner), 5000);
    sourcePartner.click();
    // // Wait for the chat to appear
     browser.wait(EC.visibilityOf(element(by.css('app-view-chat-posts'))), 5000);
    // Assertion: Check if the chat with the selected source partner is displayed
     expect(element(by.css('app-view-chat-posts')).isDisplayed()).toBeTruthy();

    // Simulate adding new chat posts
    const newChatMessage1 = 'New chat message 1';
    const newChatMessage2 = 'New chat message 2';

    // Simulate typing and sending a message in an input field
    const inputChat = element.all(by.css('app-create-update-post .ql-editor.ql-blank p')).last();
     browser.wait(EC.visibilityOf(inputChat), 5000);
    //
    browser.executeScript('arguments[0].scrollIntoView(true);', inputChat.getWebElement());
    browser.sleep(1000);
    const sendBtn = element(by.css('app-create-update-post .btn.btn-success'));
    browser.wait(EC.visibilityOf(sendBtn), 5000);

    inputChat.sendKeys(newChatMessage1);
    sendBtn.click(); // Assuming there's a button to send the chat message

    // Optionally, add a delay to ensure the first message is sent before sending the second message
     browser.sleep(1000);
     inputChat.sendKeys(newChatMessage2);
     sendBtn.click(); // Assuming there's a button to send the chat message
     const chatMessage = element.all(by.css('app-view-post')).last();
     browser.wait(EC.visibilityOf(chatMessage), 5000);
      const existingChatMessages = element.all(by.css('app-view-post .content.html'));
      expect(existingChatMessages.count()).toBeGreaterThan(0); // Ensure that there are existing chat messages
      // Assertion: Verify that the new chat posts appear in the chat view
      const newChatMessages = element.all(by.css('app-view-post .content.html')).filter((element) => {
        return element.getText().then((text) => {
          return text.includes(newChatMessage1) || text.includes(newChatMessage2);
        });
      });
      expect(newChatMessages.count()).toBeGreaterThan(0)// Expecting two new chat messages
  });
  it('should mark the chat as read', async () => {

    const myTabELement = element(by.id('ngb-nav-10'));
    await browser.wait(ExpectedConditions.elementToBeClickable(myTabELement), 5000);
    myTabELement.click();
    const sourcePartner = element(by.css('app-view-job-source-contacts table tbody tr:nth-child(1) td:nth-child(3)')); // Click on the dropdown
    await browser.wait(ExpectedConditions.elementToBeClickable(sourcePartner), 5000);
    sourcePartner.click();
    // Simulate clicking the "Mark as Read" button
    element(by.css('app-view-chat-posts .btn.btn-sm.btn-secondary')).click(); // Assuming there's a button to mark the chat as read

    // Optionally, wait for the UI changes indicating read status (e.g., disappearance of unread indicators)
    browser.sleep(1000); // Wait for 1 second for the UI changes to take effect

    // Assertion: Verify that the chat is marked as read
    const unreadIndicators = element.all(by.css('app-view-job-source-contacts table tbody tr:nth-child(1) td:nth-child(2) app-chat-read-status .notification-dot'));
    // Resolve the promise and assert that the chat is marked as read
    const unreadIndicatorCount = await unreadIndicators.count();
    expect(unreadIndicatorCount).toEqual(0); // Expecting no unread indicators to be present
  });

});
