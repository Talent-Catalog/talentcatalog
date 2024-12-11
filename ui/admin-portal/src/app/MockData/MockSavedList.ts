
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

// Mock implementation for SavedList
import {SavedList} from "../model/saved-list";
import {TaskType, UploadType} from "../model/task";
import {MockUser} from "./MockUser";

export const MockSavedList: SavedList = {
  name: "Test SavedList",
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
  tbbShortName: 'TBB',
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
    helpLink: 'example.com/help',
    taskType: TaskType.Question,
    uploadType: UploadType.degree,
    uploadSubfolderName: 'subfolder',
    uploadableFileTypes: '.pdf',
    candidateAnswerField: 'answer',
    allowedAnswers: [{ name: 'Answer 1', displayName: 'Answer 1' }] // AllowedQuestionTaskAnswer example
  }]
};
