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
import {Status} from "./base";
import {Country} from "./country";

/*
  MODEL - latest best practice on this kind of file

  - Avoid duplicating lists of attributes by using inheritance (ie extends).
  - Link update request to partner attributes (ie don't duplicate attributes)
*/
export interface PartnerSimpleAttributes {
  abbreviation: string;
  logo: string;
  name: string;
  partnerType: string;
  registrationLandingPage: string;
  registrationUrl: string;
  status: Status;
  websiteUrl: string;
}

export interface Partner extends PartnerSimpleAttributes {
  id: number;
  sourceCountries: Country[];
}

export interface UpdatePartnerRequest extends PartnerSimpleAttributes {
  sourceCountryIds: number[];
}

