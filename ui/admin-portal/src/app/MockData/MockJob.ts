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


import {MockUser} from "./MockUser";
import {Job, JobOpportunityStage} from "../model/job";
import {TaskType, UploadType} from "../model/task";
import {MockSavedSearch} from "./MockSavedSearch";

export const MockJob: Job = {
  evergreen: false,
  skipCandidateSearch: false,
  closed: false,
  name: "XYZ",
  submissionList: {
    id: 1,
    name: 'Mock Submission List',
    savedSearchSource: { id: 1 }, // SavedSearchRef example with only id
    fileJdLink: 'example.com/jd',
    fileJdName: 'JD File',
    fileJoiLink: 'example.com/joi',
    fileJoiName: 'JOI File',
    fileInterviewGuidanceLink: 'example.com/interview',
    fileInterviewGuidanceName: 'Interview File',
    folderlink: 'example.com/folder',
    folderjdlink: 'example.com/folder/jd',
    publishedDocLink: 'example.com/published',
    tcShortName: 'TC',
    sfJobCountry: 'USA',
    sfJobStage: 'Prospect',
    fixed: true,
    global: false,
    tasks: [{
      id: 1,
      name: 'Task 1',
      createdBy: new MockUser(),
      createdDate: new Date(),
      updatedBy: new MockUser(),
      updatedDate: new Date(),
      daysToComplete: 7,
      description: 'Task description',
      displayName: 'Task 1',
      optional: false,
      docLink: 'example.com/help',
      taskType: TaskType.Question,
      uploadType: UploadType.degree,
      uploadSubfolderName: 'subfolder',
      uploadableFileTypes: '.pdf',
      candidateAnswerField: 'answer',
      allowedAnswers: [{ name: 'Answer 1', displayName: 'Answer 1' }]
    }]
  },
  suggestedList: undefined,
  suggestedSearches: [new MockSavedSearch()],
  won: false,
  id: 1,
  hiringCommitment: 'Full-time',
  opportunityScore: 'High',
  contactUser: {
    id: 1,
    username: 'johndoe',
    firstName: 'John',
    lastName: 'Doe',
    email: 'johndoe@example.com',
    role: 'admin',
    jobCreator: true,
    approver: null,
    purpose: 'Some purpose',
    readOnly: false,
    sourceCountries: [{ id: 1, name: 'USA', status: 'Active', translatedName: 'United States' }],
    status: 'Active',
    createdDate: Date.now(),
    createdBy: null,
    updatedDate: Date.now(),
    lastLogin: Date.now(),
    usingMfa: true,
    mfaConfigured: true,
    partner: null,
    name: 'John Doe',
    emailVerified: false,
  },
  country: { id: 1, name: 'USA', status: 'Active', translatedName: 'United States' },
  employerEntity: { id: 1, name: 'ABC Company', description: 'Some description', hasHiredInternationally: true, sfId: '123', website: 'https://example.com' },
  exclusionList: {
    id: 1,
    name: 'Exclusion List',
    savedSearchSource: { id: 1 }, // SavedSearchRef example with only id
    fileJdLink: 'example.com/jd',
    fileJdName: 'JD File',
    fileJoiLink: 'example.com/joi',
    fileJoiName: 'JOI File',
    fileInterviewGuidanceLink: 'example.com/interview',
    fileInterviewGuidanceName: 'Interview File',
    folderlink: 'example.com/folder',
    folderjdlink: 'example.com/folder/jd',
    publishedDocLink: 'example.com/published',
    tcShortName: 'TC',
    sfJobCountry: 'USA',
    sfJobStage: 'Prospect',
    fixed: true, // Adding fixed property
    global: false, // Adding global property
    tasks: [{
      id: 1,
      name: 'Task 1',
      createdBy: new MockUser(), // User example with only id
      createdDate: new Date(),
      updatedBy: new MockUser(), // User example with only id
      updatedDate: new Date(),
      daysToComplete: 7,
      description: 'Task description',
      displayName: 'Task 1',
      optional: false,
      docLink: 'example.com/help',
      taskType: TaskType.Question,
      uploadType: UploadType.degree,
      uploadSubfolderName: 'subfolder',
      uploadableFileTypes: '.pdf',
      candidateAnswerField: 'answer',
      allowedAnswers: [{ name: 'Answer 1', displayName: 'Answer 1' }] // AllowedQuestionTaskAnswer example
    }]  },
  jobSummary: 'Job summary',
  publishedBy: {
    id: 2,
    username: 'janesmith',
    firstName: 'Jane',
    lastName: 'Smith',
    email: 'janesmith@example.com',
    role: 'admin',
    jobCreator: false,
    approver: null,
    purpose: 'Some purpose',
    readOnly: false,
    sourceCountries: [],
    status: 'Active',
    createdDate: Date.now(),
    createdBy: null,
    updatedDate: Date.now(),
    lastLogin: Date.now(),
    usingMfa: false,
    mfaConfigured: true,
    partner: null,
    name: 'Jane Smith',
    emailVerified: false,
  },
  publishedDate: new Date(),
  jobCreator: { id: 3, name: 'XYZ Partner', abbreviation: 'XYZ', websiteUrl: 'https://xyzpartner.com' },
  stage: JobOpportunityStage.prospect,
  starringUsers: [{ id: 4, username: 'alicejohnson', firstName: 'Alice', lastName: 'Johnson', email: 'alicejohnson@example.com', role: 'limited', jobCreator: false, approver: null, purpose: 'Some purpose', readOnly: false, sourceCountries: [], status: 'Active', createdDate: Date.now(), createdBy: null, updatedDate: Date.now(), lastLogin: Date.now(), usingMfa: true, mfaConfigured: false, partner: null, name: 'Alice Johnson',emailVerified: false,}],
  submissionDueDate: new Date(),
   jobOppIntake: { employerCostCommitment: 'High', recruitmentProcess: 'Some process', minSalary: 50000, occupationCode: '123', salaryRange: '50k - 100k', locationDetails: 'Some location details', location: 'Some location', visaPathways: 'Some pathways', benefits: 'Some benefits', educationRequirements: 'Some education requirements', languageRequirements: 'Some language requirements', employmentExperience: 'Some employment experience', skillRequirements: 'Some skill requirements' },
  createdBy: new MockUser()
};



