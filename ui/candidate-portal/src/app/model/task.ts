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

import {Auditable} from "./base";

class AllowedQuestionTaskAnswer {
  name: string;
  displayName: string;
}

export interface Task extends Auditable {
  id: number;
  name: string;
  candidateForm?: CandidateForm;
  daysToComplete: number;
  description: string;
  displayName: string;
  optional: boolean;
  docLink: string;
  taskType: TaskType;
  uploadType: UploadType;
  uploadSubfolderName: string;
  uploadableFileTypes: string;
  candidateAnswerField: string;
  allowedAnswers?: AllowedQuestionTaskAnswer[];

}

export interface CandidateForm {
  name: string;
  description: string;
}

export enum TaskType {
  Form = "Form",
  Question = "Question",
  Simple = "Simple",
  Upload = "Upload",
  YesNoQuestion = "YesNoQuestion"
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
