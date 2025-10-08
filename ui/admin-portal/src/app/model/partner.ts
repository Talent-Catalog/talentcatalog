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
import {Country} from "./country";
import {User} from "./user";
import {JobChat} from "./chat";

/*
  MODEL - extending interfaces, update request extend object attributes, mapping enums

  - Avoid duplicating lists of attributes by using inheritance (ie extends).
  - Link update request to partner attributes (ie don't duplicate attributes)
*/

export interface PartnerSimpleAttributes {
  abbreviation: string;
  autoAssignable: boolean;
  defaultPartnerRef: boolean;
  employerSflink?: string;
  jobContact?: User;
  jobCreator: boolean;
  logo: string;
  name: string;
  notificationEmail: string;
  publicApiAccess: boolean;
  publicApiAuthorities: string[];
  publicApiKey?: string;
  registrationLandingPage: string;
  sflink: string;
  sourcePartner: boolean;

  //Note that status is a Java Status enum on the server, but it maps to the string value of
  //enum by the JSON processing (Javascript does not know about enums).
  //However we do create a matching Typescript Status enum, and use that to process this
  //value in a type safe way. See other MODEL's for details.
  status: string;
  websiteUrl: string;
  redirectPartner?: ShortPartner;
}

export enum PublicApiAuthority {
  READ_CANDIDATE_DATA = "READ_CANDIDATE_DATA",
  SUBMIT_JOB_MATCHES = "SUBMIT_JOB_MATCHES",
  OFFER_CANDIDATE_SERVICES = "OFFER_CANDIDATE_SERVICES",
  REGISTER_CANDIDATES = "REGISTER_CANDIDATES",
  ADMIN = "ADMIN"
}

export interface Employer {
  id: number;
  name: string;
  description: string;
  hasHiredInternationally: boolean;
  sfId: string;
  website: string;
}

export interface ShortPartner {
  id: number;
  name: string;
  abbreviation: string;
  websiteUrl: string;
}

export interface Partner extends PartnerSimpleAttributes {
  id: number;
  defaultContact?: User;
  defaultJobCreator: boolean;
  defaultSourcePartner: boolean;
  employer?: Employer;
  sourceCountries: Country[];

  //Temporary place to store job chat associated with partner - will depend on current job context.
  //The underscore naming is a convention to distinguish this field from those that are
  //uploaded from the server - not set according to context on the browser.
  _jobChat?: JobChat;
  // DPA fields for versioned acceptance management
  acceptedDataProcessingAgreementId?: string;
  acceptedDataProcessingAgreementDate?: string;
  firstDpaSeenDate?: string;
}

export function sourceCountriesAsString(partner: Partner): string {
  let s = '';
  const countries: Country[] = partner.sourceCountries;
  if (countries != null) {
    s = countries.map(c => c.name).join(",");
  }
  return s;
}

export interface UpdatePartnerRequest extends PartnerSimpleAttributes {
  defaultContactId?: number,
  sourceCountryIds: number[];
  redirectPartnerId?: number;
}

export interface UpdatePartnerJobContactRequest {
  jobId: number;
  userId: number;
}

