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


import {
  Candidate,
  CandidateDestination,
  DrivingLicenseStatus,
  Exam,
  Gender,
  MaritalStatus,
  UnhcrStatus,
  YesNo,
  YesNoUnsure
} from "../model/candidate";
import {TaskAssignment} from "../model/task-assignment";
import {MockUser} from "./MockUser";
import {AttachmentType, CandidateAttachment} from "../model/candidate-attachment";
import {TaskType, UploadType} from "../model/task";
import {CandidateEducation} from "../model/candidate-education";
import {MockJob} from "./MockJob";
import {CandidateLanguage} from "../model/candidate-language";
import {CandidateOccupation} from "../model/candidate-occupation";
import {CandidateJobExperience} from "../model/candidate-job-experience";
import {Status} from "../model/base";

const mockUser = new MockUser();
export class MockCandidate implements Candidate {

  id: number = 1;
  candidateNumber: string = "123456";
  status: string = "active";
  muted: boolean = false;
  gender: string = Gender.male;
  dob: Date = new Date('1990-01-01');
  address1: string = "123 Main St";
  city: string = "New York";
  state: string = "NY";
  country: any = { id: 1, name: "United States", code: "US" };
  yearOfArrival: number = 2010;
  nationality: any = { id: 1, name: "United States", code: "US" };
  phone: string = "+1234567890";
  whatsapp: string = "+1234567890";
  externalId: string = "ABC123";
  externalIdSource: string = "Source";
  partnerRef: string = "Ref";
  unhcrRegistered: YesNoUnsure = YesNoUnsure.Yes;
  unhcrNumber: string = "UNHCR123";
  unhcrConsent: YesNo = YesNo.Yes;
  unrwaRegistered: YesNoUnsure = YesNoUnsure.No;
  unrwaNumber: string = "123";
  user: any = mockUser;
  candidateReviewStatusItems: any[] = [];
  migrationEducationMajor: any = { id: 1, name: "Computer Science" };
  additionalInfo: string = "Additional Information about candidate";
  linkedInLink: string = "";
  candidateMessage: string = "";
  maxEducationLevel: any = { id: 1, name: "Bachelor's Degree" };
  folderlink: string = "";
  sflink: string = "";
  videolink: string = "";
  regoPartnerParam: string = "";
  regoReferrerParam: string = "";
  regoUtmCampaign: string = "";
  regoUtmContent: string = "";
  regoUtmMedium: string = "";
  regoUtmSource: string = "";
  regoUtmTerm: string = "";
  shareableCv: any = { id: 1, filename: "cv.pdf", url: "https://example.com/cv.pdf" };
  shareableDoc: any = { id: 2, filename: "document.pdf", url: "https://example.com/document.pdf" };
  listShareableCv: any = { id: 3, filename: "list_cv.pdf", url: "https://example.com/list_cv.pdf" };
  listShareableDoc: any = { id: 4, filename: "list_document.pdf", url: "https://example.com/list_document.pdf" };
  shareableNotes: string = "";
  surveyType: any = { id: 1, name: "Survey" };
  surveyComment: string = "Referred by a friend";
  selected: boolean = false;
  createdDate: number = 19900101;
  updatedDate: number = 19900101;
  contextNote: string = "";
  maritalStatus: MaritalStatus = MaritalStatus.Single;
  drivingLicense: DrivingLicenseStatus = DrivingLicenseStatus.Valid;
  unhcrStatus: UnhcrStatus = UnhcrStatus.RegisteredAsylum;
  ieltsScore: string = "7.5";
  numberDependants: number = 2;
  relocatedAddress: string = "123 Sesame Street"
  relocatedCity: string = "Melbourne"
  relocatedState: string = "Victoria"
  relocatedCountry: any = { id: 1, name: "Australia", status: "active", translatedName: "Australia" }
  candidateExams: any[] = [
    { id: 1, exam: Exam.IELTSGen, score: "7.5", year: 2020 }
  ];
  candidateAttachments: CandidateAttachment[] = [
    {
      id: 1,
      type: AttachmentType.file,
      name: 'Attachment 1',
      location: 'location1',
      url: 'http://example.com/attachment1',
      createdBy: mockUser,
      createdDate: 1620000000000,
      updatedBy: mockUser,
      updatedDate: 1620000000000,
      migrated: false,
      cv: false,
      uploadType: UploadType.other,
      fileType: 'pdf'
    },
    {
      id: 2,
      type: AttachmentType.file,
      name: 'Attachment 2',
      location: 'location2',
      url: 'http://example.com/attachment2',
      createdBy: mockUser,
      createdDate: 1620000000000,
      updatedBy: mockUser,
      updatedDate: 1620000000000,
      migrated: false,
      cv: true,
      uploadType: UploadType.cv,
      fileType: 'pdf'
    }
  ];
  taskAssignments?: TaskAssignment[] = [
    {
      id: 1,
      abandonedDate: null,
      candidateNotes: '',
      completedDate: null,
      dueDate: new Date('2012-06-30'),
      status: Status.active,
      task: {
        id: 1,
        name: 'Submit CV',
        daysToComplete: 7,
        description: 'Submit your latest CV.',
        displayName: 'CV Submission',
        optional: false,
        docLink: 'http://example.com/cv-help',
        taskType: TaskType.Upload,
        uploadType: UploadType.cv,
        uploadSubfolderName: 'cvs',
        uploadableFileTypes: 'pdf,doc,docx',
        candidateAnswerField: 'N/A'
      },
      answer: 'Sample Answer'
    },
    {
      id: 2,
      abandonedDate: null,
      candidateNotes: '',
      completedDate: null,
      dueDate: new Date('2099-06-25'),
      status: Status.active,
      task: {
        id: 2,
        name: 'Complete Questionnaire',
        daysToComplete: 5,
        description: 'Answer the provided questions.',
        displayName: 'Questionnaire',
        optional: false,
        docLink: 'http://example.com/questionnaire-help',
        taskType: TaskType.Question,
        uploadType: UploadType.other,
        uploadSubfolderName: '',
        uploadableFileTypes: '',
        candidateAnswerField: 'Please answer the questions in the text box.'
      },
      answer: 'Sample Answer'
    },
    {
      id: 3,
      abandonedDate: null,
      candidateNotes: '',
      completedDate: null,
      dueDate: new Date('2099-07-05'),
      status: Status.active,
      task: {
        id: 3,
        name: 'Upload Passport',
        daysToComplete: 10,
        description: 'Upload a scan of your passport.',
        displayName: 'Passport Upload',
        optional: true,
        docLink: 'http://example.com/passport-help',
        taskType: TaskType.Upload,
        uploadType: UploadType.passport,
        uploadSubfolderName: 'passports',
        uploadableFileTypes: 'jpg,png,pdf',
        candidateAnswerField: 'N/A'
      },
      answer: 'Sample Answer'
    }
  ];
  candidateOpportunities: any[] = [];
  candidateProperties?: any[];
  mediaWillingness?:string = 'Yes, open to media';
  miniIntakeCompletedBy: any = { id: 1, username: "admin", email: "admin@example.com" };
  miniIntakeCompletedDate: number = 19900101;
  fullIntakeCompletedBy: any = { id: 1, username: "admin", email: "admin@example.com" };
  fullIntakeCompletedDate: number = 19900101;
  potentialDuplicate: boolean = false;
  candidateCertifications?: any[];
  candidateEducations?: CandidateEducation[] = [
    {
      id: 1,
      educationType: "Bachelor's Degree",
      country: MockJob.country,
      educationMajor: { id: 1, name: "Computer Science", status: "active" },
      lengthOfCourseYears: 4,
      institution: "University of Example",
      courseName: "B.Sc. in Computer Science",
      yearCompleted: "2012",
      incomplete: false,
    },
    {
      id: 2,
      educationType: "Master's Degree",
      country:  MockJob.country,
      educationMajor: { id: 1, name: "Management", status: "active" },
      lengthOfCourseYears: 2,
      institution: "Institute of Example",
      courseName: "M.Sc. in Software Engineering",
      yearCompleted: "2014",
      incomplete: false,
    },
  ];
  candidateJobExperiences?: CandidateJobExperience[] = [
    {
      id: 1,
      country: MockJob.country,
      companyName: 'Company A',
      role: 'Developer',
      startDate: '2020-01-01',
      endDate: '2021-01-01',
      fullTime: 'true',
      paid: 'true',
      description: 'Worked as a software developer in Company A',
      expanded: false,
    },
    {
      id: 2,
      country: MockJob.country,
      companyName: 'Company B',
      role: 'Project Manager',
      startDate: '2019-05-01',
      endDate: '2020-06-01',
      fullTime: 'false',
      paid: 'true',
      description: 'Managed multiple projects in Company B',
      expanded: false,
    },
  ];
  candidateLanguages?: CandidateLanguage[] = [
    {
      id: 1,
      candidate: {id:1} as Candidate,
      language: {
        id: 1,
        name: "English",
        status: "Active"
      },
      spokenLevel: {
        id: 1,
        name: "Fluent",
        level: 3,
        status: "Active"
      },
      writtenLevel: {
        id: 2,
        name: "Advanced",
        level: 1,
        status: "Active"
      },
      migrationLanguage: "French"
    }
  ];
  candidateOccupations: CandidateOccupation[] = [
    {
      id: 1,
      occupation: {
        id: 1,
        name: "Software Engineer",
        isco08Code: "1234",
        status: "active"
      },
      yearsExperience: 5,
      migrationOccupation: "Software Developer",
      createdBy: mockUser,
      createdDate: 2023,
      updatedBy: mockUser,
      updatedDate: 2024
    },
    {
      id: 2,
      occupation: {
        id: 2,
        name: "Data Scientist",
        isco08Code: "5678",
        status: "active"
      },
      yearsExperience: 3,
      migrationOccupation: "Data Analyst",
      createdBy: mockUser,
      createdDate: 2023,
      updatedBy: mockUser,
      updatedDate: 2024
    }
  ];
  candidateDestinations: CandidateDestination[] = [
    {
      id: 1,
      country: { id: 1, name: "Australia", status: "active", translatedName: "Australia" },
      interest: YesNoUnsure.Yes,
      notes: "I like this country."
    },
    {
      id: 1,
      country: { id: 2, name: "Canada", status: "active", translatedName: "Canada" },
      interest: YesNoUnsure.No,
      notes: "I do not like Toronto."
    },
  ];


  constructor() {}
}
