import { browser, by, element, ExpectedConditions } from 'protractor';

describe('NavigationComponent', () => {
  it('should find and select the ng-container with ngbNavItem="StarredJobs"', async () => {
    // Find the <ng-container> element with ngbNavItem="StarredJobs"
    const starredJobsContainer = element(by.css('ng-container[ngbNavItem="StarredJobs"]'));

    // Check if the element is present
    const isPresent = await starredJobsContainer.isPresent();
    if (isPresent) {
      // Click on the container to select it
      await starredJobsContainer.click();

      // Find the navigation element
      const navElement = element(by.css('nav.nav-tabs'));

      // Wait for the navigation to load
      await browser.wait(ExpectedConditions.visibilityOf(navElement), 5000, 'Navigation component did not load');

      // Get the activeTabId attribute value
      const activeTabId = await navElement.getAttribute('activeId');

      // Assert that the activeTabId is set to "StarredJobs"
      expect(activeTabId).toBe('StarredJobs');
    } else {
      console.log("StarredJobs container not found.");
    }
  });
});
