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

// Protractor configuration file, see link for more information
// https://github.com/angular/protractor/blob/master/lib/config.ts
const {SpecReporter} = require('jasmine-spec-reporter');
const httpMock = require('protractor-http-mock/lib/httpMock');
  exports.config = {
    allScriptsTimeout: 11000,
    specs: [
    './src/components/account/login/login.e2e.spec.ts',
    './src/**/*.e2e-spec.ts',
    './src/components/account/reset-password/reset-password.e2e.spec.ts',
    './src/components/settings/users/create-update-user.e2e.spec.ts',
    './src/components/account/change-password/change-password.e2e.spec.ts',
    './src/components/settings/users/cleanup.e2e.spec.ts',
    './src/components/jobs/job-home/job-home.e2e.spec.ts',
    './src/components/jobs/job/jobs.e2e.spec.ts',
    './src/components/jobs/jobs-with-details/jobs-with-details.e2e.spec.ts',
    './src/components/jobs/new-job/new-job.e2e.spec.ts',
    './src/components/jobs/view/info/view-job-info/view-job-info.e2e.spec.ts', './src/components/jobs/view/info/edit-job-info/edit-job-info.e2e.spec.ts',
    './src/components/jobs/view/source-contacts/job-source-contacts-with-chats/job-source-contacts-with-chats.e2e.spec.ts',
    './src/components/jobs/view/source-contacts/view-job-source-contacts/view-job-source-contacts.e2e.spec.ts',
    './src/components/jobs/view/source-contacts/view-job-source-contacts/view-job-source-contacts.e2e.spec.ts',
    './src/components/jobs/view/view-job-submission-list/view-job-submission-list.e2e.spec.ts',
    './src/components/jobs/view/view-job-suggested-searches/view-job-suggested-searches.e2e.spec.ts',
    './src/components/jobs/view/view-job-summary/view-job-summary.e2e.spec.ts',
    './src/components/jobs/view/tab/job-general-tab/job-general-tab.e2e.spec.ts',
    './src/components/jobs/view/tab/job-group-chats-tab/job-group-chats-tab.e2e.spec.ts',
    './src/components/jobs/view/tab/job-intake-tab/job-intake-tab.e2e.spec.ts',
    './src/components/jobs/view/tab/job-source-contacts-tab/job-source-contacts-tab.e2e.spec.ts',
    './src/components/jobs/view/tab/job-suggested-searches-tab/job-suggested-searches-tab.e2e.spec.ts',
    './src/components/jobs/view/tab/job-upload-tab/job-upload-tab.e2e.spec.ts',
    './src/components/jobs/view/view-job-uploads/view-job-uploads.e2e.spec.ts',
    './src/components/jobs/view/view-job/view-job.e2e.spec.ts',
    './src/components/jobs/view/view-job-from-url/view-job-from-url.e2e.spec.ts',
  ],
  capabilities: {
    'browserName': 'chrome'
  },
  directConnect: true,
  baseUrl: 'http://localhost:4201',
  framework: 'jasmine',
  jasmineNodeOpts: {
    showColors: true,
    defaultTimeoutInterval: 30000,
    print: function() {}
  },
  onPrepare() {
    require('ts-node').register({
      project: require('path').join(__dirname, './tsconfig.e2e.json')
    });
   jasmine.getEnv().addReporter(new SpecReporter({ spec: { displayStacktrace: true } }));

    // Initialize protractor-http-mock
    const mocks = []; // Define your mocks here if needed
    const plugins = []; // Define your plugins here if needed
    const skipDefaults = false; // Set to true to skip default mocks and plugins

    httpMock(mocks, plugins, skipDefaults);
  }
};
