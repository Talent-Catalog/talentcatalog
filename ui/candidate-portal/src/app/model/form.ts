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

export interface MyFirstFormData {
  city?: string;
  hairColour?: string;
}

export enum ItalyCandidateTravelDocType {
  Passport = 'PASSPORT',
  NationalId = 'NATIONAL_ID',
  RefugeeCertificate = 'REFUGEE_CERTIFICATE',
}
export interface ItalyCandidateTravelDocFormData {
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: string;
  birthCountry: Country | null;
  placeOfBirth: string;
  travelDocType: ItalyCandidateTravelDocType;
  travelDocNumber: string;
  travelDocIssuedBy: string;
  travelDocIssueDate: string;
  travelDocExpiryDate: string;
}
