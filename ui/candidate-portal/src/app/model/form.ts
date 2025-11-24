/*
 * Copyright (c) 2025 Talent Catalog.
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

import {Country} from "./country";
import {DependantRelations, Gender} from "./candidate";

export interface MyFirstFormData {
  city?: string;
  hairColour?: string;
}

export enum TravelDocType {
  Passport = 'PASSPORT',
  NationalId = 'NATIONAL_ID',
  RefugeeCertificate = 'REFUGEE_CERTIFICATE',
}
export interface TravelDocFormData {
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: string;
  birthCountry: Country | null;
  placeOfBirth: string;
  travelDocType: TravelDocType;
  travelDocNumber: string;
  travelDocIssuedBy: string;
  travelDocIssueDate: string;
  travelDocExpiryDate: string;
}

export interface FamilyMemberDoc {
  docType: TravelDocType;
  docNumber: string;
  issuer: string;
  issuedOn: string;
  expiresOn: string;
}

export interface RelocatingFamilyMember {
  relationship: DependantRelations;
  dependantRelationOther?: string;

  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: Gender;
  countryOfBirth: string;
  placeOfBirth?: string;

  healthConcerns?: 'Yes' | 'No' | null;
  healthNotes?: string;
  registered?: 'Yes' | 'No' | null;
  registeredNumber?: string;
  registeredNotes?: string;

  travelDoc: FamilyMemberDoc;
}

export interface FamilyDocFormData {
  noEligibleFamilyMembers: boolean;
  noEligibleNotes?: string;
  familyMembersJson: string;
}


export interface FamilyRsdEvidenceEntry {
  memberKey: string;
  firstName?: string;
  lastName?: string;
  dateOfBirth?: string;
  displayName?: string;
  refugeeStatus?: string;
  documentType?: string;
  documentNumber?: string;
  attachmentId?: number;
  attachmentName?: string;
  attachmentLocation?: string;
}

export interface FamilyRsdEvidenceFormData {
  familyRsdEvidenceJson?: string;
}

export enum RsdRefugeeStatus {
  RecognizedByUnhcr = 'RECOGNIZED_BY_UNHCR',
  RecognizedByHostCountry = 'RECOGNIZED_BY_HOST_COUNTRY',
  Pending = 'PENDING',
}

export enum RsdEvidenceDocumentType {
  UnhcrCertificate = 'UNHCR_CERTIFICATE',
  HostCountryId = 'HOST_COUNTRY_ID',
  OfficialCampRegistration = 'OFFICIAL_CAMP_REGISTRATION',
}

export interface RsdEvidenceFormData {
  refugeeStatus: RsdRefugeeStatus | null;
  documentType: RsdEvidenceDocumentType | null;
  documentNumber: string;
}
