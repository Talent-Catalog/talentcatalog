import {browser, by, element, ExpectedConditions} from "protractor";
import {clickTabAndWait} from "../../../job-home/job-home.e2e.spec";

describe('JobGroupChatsTabComponent', () => {
  beforeEach(() => {
    // Navigate to the page where the component is rendered
    browser.get('/jobs');
  });
  it('should display group chats general tab', async () => {
    await clickTabAndWait(0);
    const jobEntries = element.all(by.css('app-jobs-with-detail'));
    expect(await jobEntries.count()).toBeGreaterThan(0);
    const myTabELement = element(by.css('app-jobs-with-detail app-view-job nav.nav-tabs.nav a:nth-child(6)'))
    await browser.wait(ExpectedConditions.elementToBeClickable(myTabELement), 5000);
    myTabELement.click();
  });
  it('should display chats with posts correctly', async () => {
    // Find the app-chats-with-posts component
    const chatsWithPosts = element(by.tagName('app-chats-with-posts'));

    // Check if the component is present
    expect(await chatsWithPosts.isPresent()).toBeTruthy();

    // Get the inner HTML of the component
    const innerHTML = await chatsWithPosts.getAttribute('innerHTML');

    expect(innerHTML).toContain('class="table table-hover"');

    // Verify that certain elements or patterns are present in the inner HTML
    expect(innerHTML).toContain('All associated with job plus candidates who have accepted job offers');
    expect(innerHTML).toContain('Talent Beyond Boundaries and all source partners');

  });

  // Add more test cases as needed
});
