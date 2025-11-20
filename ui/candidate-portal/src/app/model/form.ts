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
export interface TravelInfoFormData {
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

/**
 * This is what the JSON is extracted into.
 * The field names must match the field names in the JSON.
 * And the field names in the JSON must match the field keys or property names
 * in the allColumnInfosMap in the Angular PublishedDocColumnService.
 *
 * This is because when a list is published into columns, data is fetched from the JSON
 * into each column based on the field key or property name of the column.
 */
export interface RelocatingDependant {
  relationship: DependantRelations;
  dependantRelationOther?: string;

  'user.firstName': string;
  'user.lastName': string;
  dob: string;
  gender: Gender;
  'birthCountry.name': string;
  placeOfBirth?: string;

  //todo Where are these fields stored for normal candidates
  //todo We can't display these values in a column without a defined column
  //todo definition linked to a standard candidate field key or property
  //todo Family members aren't special creatures - they are refugees like the primary candidate and
  //todo they can't have special fields that aren't also applicable to any candidate
  healthConcerns?: 'Yes' | 'No' | null;
  healthNotes?: string;
  registered?: 'Yes' | 'No' | null;
  registeredNumber?: string;
  registeredNotes?: string;

  TRAVEL_DOC_TYPE?: string,
  TRAVEL_DOC_NUMBER: string,
  TRAVEL_DOC_ISSUED_BY: string,
  TRAVEL_DOC_ISSUE_DATE: string,
  TRAVEL_DOC_EXPIRY_DATE: string

  REFUGEE_STATUS?: string;
  REFUGEE_STATUS_EVIDENCE_DOCUMENT_TYPE?: string;
  REFUGEE_STATUS_EVIDENCE_DOCUMENT_NUMBER?: string;
}

export interface DependantsInfoFormData {
  dependantsJson: string;
  noEligibleDependants: boolean;
  noEligibleNotes?: string;
}

export enum RsdRefugeeStatus {
  RecognizedByUnhcr = 'RECOGNIZED_BY_UNHCR',
  RecognizedByHostCountry = 'RECOGNIZED_BY_HOST_COUNTRY',
  Pending = 'PENDING',
}

export enum RefugeeStatusEvidenceDocumentType {
  UnhcrCertificate = 'UNHCR_CERTIFICATE',
  HostCountryId = 'HOST_COUNTRY_ID',
  OfficialCampRegistration = 'OFFICIAL_CAMP_REGISTRATION',
}

export interface RefugeeStatusInfoFormData {
  refugeeStatus: RsdRefugeeStatus | null;
  documentType: RefugeeStatusEvidenceDocumentType | null;
  documentNumber: string;
}
