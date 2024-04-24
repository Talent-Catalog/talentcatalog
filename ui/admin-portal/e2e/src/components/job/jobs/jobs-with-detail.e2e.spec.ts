import {browser, by, element, ExpectedConditions, protractor} from 'protractor';

describe('JobsWithDetailComponent', () => {
  beforeEach(() => {
    // Navigate to the page containing the component
    browser.get('/jobs');
  });

  it('should render the component without errors', () => {
    // Check if the component container is present
    const container = element(by.css('app-jobs-with-detail'));
    expect(container.isPresent()).toBeTruthy();

    // Check if the jobs list is displayed
    const jobsList = container.all(by.css('app-jobs'));
    expect(jobsList.count()).toBeGreaterThan(0);

    // Check if the side panel is displayed
    const sidePanel = element(by.css('.detail-panel'));
    expect(sidePanel.isPresent()).toBeTruthy();
    //
    // const resizeButton = sidePanel.element(by.css('button.btn.btn-sm.btn-outline-secondary'));
    // expect(resizeButton.isPresent()).toBeTruthy();
  });


  it('should display the correct jobs list based on the searchBy parameter', async () => {
    // Define the searchBy parameter to use for testing
    const searchBy = 'Test';

    // Wait for the component to load
    const container = element(by.css('app-jobs-with-detail'));
    await browser.wait(ExpectedConditions.presenceOf(container), 5000);

    // Find the search input field
    const searchInput = container.element(by.id('keyword'));

    // Type the search term and press Enter
    await searchInput.sendKeys(searchBy, protractor.Key.ENTER);

    // Wait for the jobs list to update with the search results
    const jobRows = container.all(by.css('app-jobs tbody tr')); // Assuming each job is represented as a row in the table

    // Ensure that the jobs list is present and not empty
    expect(await jobRows.isPresent()).toBeTruthy();
    expect(await jobRows.count()).toBeGreaterThan(0);

    // Check each job row to verify if it matches the search criteria
    await jobRows.each(async (jobRow) => {
      const nameColumn = jobRow.element(by.css('td:nth-child(1)')); // Assuming "Name" column is the first column (index 1)

      // Wait for the nameColumn element to be present
      await browser.wait(ExpectedConditions.presenceOf(nameColumn), 5000, 'Name column element not found');

      const nameValue = await nameColumn.getText();

      // Assert that the nameValue includes the searchBy parameter
      expect(nameValue).toContain(searchBy);
    });
  });

  it('should display the details of the selected job in the side panel', async () => {
    // Wait for the component to load
    const container = element(by.css('app-jobs-with-detail'));
    await browser.wait(ExpectedConditions.presenceOf(container), 5000);

    // Find the job list component
    const jobList = container.element(by.css('app-jobs'));

    // Find the list of job rows
    const jobRows = jobList.all(by.css('tbody tr'));

    // Iterate over each job row
    for (let i = 0; i < await jobRows.count(); i++) {
      // Click on the job row to select it
      await jobRows.get(i).click();
      const nameColumn = jobRows.get(i).element(by.css('td:nth-child(1)')); // Assuming "Name" column is the first column (index 1)
      await browser.wait(ExpectedConditions.presenceOf(nameColumn), 5000, 'Name column element not found');

      const nameValue = await nameColumn.getText();
      const trimmedNameValue = nameValue.endsWith('...') ? nameValue.slice(0, -3) : nameValue;      // Wait for the details of the selected job to be displayed in the side panel
      const finalNameValue = trimmedNameValue.startsWith('*') ? trimmedNameValue.slice(1) : trimmedNameValue;
      const sidePanel = container.element(by.css('app-view-job'));
      await browser.wait(ExpectedConditions.visibilityOf(sidePanel), 5000, 'Side panel not visible');

      // Get the details of the selected job from the side panel
      const selectedJobDetails = await sidePanel.element(by.css('.align-items-center h1')).getText();
      console.log("PCI "+selectedJobDetails);

      expect(selectedJobDetails.replace(/\s+/g, '')).toContain(finalNameValue.replace(/\s+/g, ''));    }
  });


});
