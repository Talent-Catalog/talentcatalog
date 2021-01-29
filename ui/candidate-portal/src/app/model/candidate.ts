/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

import {CandidateOccupation} from "./candidate-occupation";
import {CandidateEducation} from "./candidate-education";
import {Country} from "./country";
import {Nationality} from "./nationality";
import {CandidateJobExperience} from "./candidate-job-experience";
import {CandidateCertification} from "./candidate-certification";
import {CandidateLanguage} from "./candidate-language";
import {EducationLevel} from "./education-level";
import {User} from "./user";
import {SurveyType} from "./survey-type";

export interface Candidate {
  id: number;
  user: User;
  candidateNumber: string;
  phone: string;
  whatsapp: string;
  gender: string;
  dob: string;
  candidateOccupations: CandidateOccupation[];
  country: Country;
  city: string;
  yearOfArrival: number;
  nationality: Nationality;
  registeredWithUN: boolean;
  candidateJobExperiences: CandidateJobExperience[];
  candidateCertifications: CandidateCertification[];
  candidateLanguages: CandidateLanguage[];
  registrationId: string;
  maxEducationLevel: EducationLevel;
  candidateEducations: CandidateEducation[];
  additionalInfo: string;
  candidateMessage: string;
  surveyType: SurveyType;
  surveyComment: string;
  status: CandidateStatus;
}

export enum CandidateStatus {
  draft = 'draft',
  active = 'active',
  inactive = 'inactive',
  pending = 'pending',
  incomplete = 'incomplete',
  employed = 'employed',
  deleted = 'deleted'
}

export class BaseCandidateContactRequest {
  id: number;
  email: string;
  phone: string;
  whatsapp: string;
}

export class RegisterCandidateRequest extends BaseCandidateContactRequest {
  username: string;
  password: string;
  passwordConfirmation: string;
  reCaptchaV3Token: string;
}

export class LoginRequest {
  username: string;
  password: string;
  reCaptchaV3Token: string;
}

export class SendResetPasswordEmailRequest {
  email: string;
  reCaptchaV3Token: string;
}

