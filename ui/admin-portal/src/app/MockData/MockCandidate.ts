/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
  DrivingLicenseStatus, Exam,
  Gender,
  MaritalStatus, UnhcrStatus,
  YesNo,
  YesNoUnsure
} from "../model/candidate";
import {TaskAssignment} from "../model/task-assignment";
import {MockUser} from "./MockUser";
import {AttachmentType, CandidateAttachment} from "../model/candidate-attachment";
import {UploadType} from "../model/task";
import {CandidateEducation} from "../model/candidate-education";
import {MockJob} from "./MockJob";

export class MockCandidate implements Candidate {

  id: number = 1;
  candidateNumber: string = "123456";
  status: string = "active";
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
  unrwaNumber: string = "";
  user: any = new MockUser();
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
  surveyComment: string = "";
  selected: boolean = false;
  createdDate: number = 19900101;
  updatedDate: number = 19900101;
  contextNote: string = "";
  maritalStatus: MaritalStatus = MaritalStatus.Single;
  drivingLicense: DrivingLicenseStatus = DrivingLicenseStatus.Valid;
  unhcrStatus: UnhcrStatus = UnhcrStatus.RegisteredAsylum;
  ieltsScore: string = "7.5";
  numberDependants: number = 2;
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
      createdBy: new MockUser(),
      createdDate: 1620000000000,
      updatedBy: new MockUser(),
      updatedDate: 1620000000000,
      migrated: false,
      cv: false,
      uploadType: UploadType.cv,
      fileType: 'pdf'
    }
  ];
  taskAssignments?: TaskAssignment[] = [];
  candidateOpportunities: any[] = [];
  candidateProperties?: any[];
  mediaWillingness?: string;
  miniIntakeCompletedBy: any = { id: 1, username: "admin", email: "admin@example.com" };
  miniIntakeCompletedDate: number = 19900101;
  fullIntakeCompletedBy: any = { id: 1, username: "admin", email: "admin@example.com" };
  fullIntakeCompletedDate: number = 19900101;
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
  candidateJobExperiences?: any[];
  candidateLanguages?: any[];
  candidateOccupations?: any[];

  constructor() {}
}
