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

import {Candidate} from './candidate';
import {User} from './user';

export enum AttachmentType {
  googlefile = 'googlefile',
  file = 'file',
  link = 'link'
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


export interface CandidateAttachment {
  id?: number;
  name: string;
  location: string;
  fileType: string;
  type: AttachmentType;
  migrated: boolean;
  cv: boolean;
  candidate?: Candidate;
  createdBy: User;
  createdDate: string;
  uploadType: UploadType;

}
