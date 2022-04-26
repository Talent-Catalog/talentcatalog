/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

import {Auditable} from "./base";

class AllowedQuestionTaskAnswer {
  name: string;
  displayName: string;
}

export interface Task extends Auditable {
  id: number;
  name: string;
  daysToComplete: number;
  description: string;
  displayName: string;
  optional: boolean;
  helpLink: string;
  taskType: TaskType;
  uploadType: UploadType;
  uploadSubfolderName: CandidateSubfolderType;
  uploadableFileTypes: string;
  candidateAnswerField: string;
  allowedAnswers?: AllowedQuestionTaskAnswer[];
}

export enum CandidateSubfolderType {
  address = "Address",
  character = "Character",
  employer = "Employer",
  engagement = "Engagement",
  experience = "Experience",
  family = "Family",
  identity = "Identity",
  immigration = "Immigration",
  language = "Language",
  medical = "Medical",
  qualification = "Qualification",
  registration = "Registration"
}

export enum TaskType {
  Question = "Question",
  Simple = "Simple",
  Upload = "Upload"
}

export enum UploadType {
  conductEmployer = "conductEmployer",
  conductEmployerTrans = "conductEmployerTrans",
  conductMinistry = "conductMinistry",
  conductMinistryTrans = "conductMinistryTrans",
  cos = "cos",
  cv = "cv",
  degree = "degree",
  degreeTranscript = "degreeTranscript",
  degreeTranscriptTrans = "degreeTranscriptTrans",
  englishExam = "englishExam",
  licencing = "licencing",
  licencingTrans = "licencingTrans",
  offer = "offer",
  otherId = "otherId",
  otherIdTrans = "otherIdTrans",
  passport = "passport",
  policeCheck = "policeCheck",
  policeCheckTrans = "policeCheckTrans",
  proofAddress = "proofAddress",
  proofAddressTrans = "proofAddressTrans",
  references = "references",
  residenceAttest = "residenceAttest",
  residenceAttestTrans = "residenceAttestTrans",
  studiedInEnglish = "studiedInEnglish",
  other = "other",
  vaccination = "vaccination",
  vaccinationTran = "vaccinationTrans"
}
