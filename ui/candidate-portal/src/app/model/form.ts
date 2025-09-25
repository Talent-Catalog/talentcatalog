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

export interface MySecondFormData {
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

  dependantHealthConcerns?: 'Yes' | 'No' | null;
  dependantHealthNotes?: string;
  dependantRegistered?: 'Yes' | 'No' | null;
  dependantRegisteredNumber?: string;
  dependantRegisteredNotes?: string;

  travelDoc: FamilyMemberDoc;
}

export interface FamilyDocFormData {
  // Stored as candidate properties on the server
  noEligibleFamilyMembers: boolean;
  noEligibleNotes?: string;
  // I persist the members array as a JSON string (keeps backend simple like MySecondForm)
  familyMembersJson: string; // JSON.stringify(RelocatingFamilyMember[])
}
