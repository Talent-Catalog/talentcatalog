
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
