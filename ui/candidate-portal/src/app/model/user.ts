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
import {Partner, ShortPartner} from "./partner";

export class User {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  jobCreator: boolean;
  approver: User;
  purpose: string;
  readOnly: boolean;
  sourceCountries: Country[];
  status: string;
  createdDate: number;
  createdBy: User;
  updatedDate: number;
  lastLogin: number;
  usingMfa: boolean;
  mfaConfigured: boolean;
  partner: Partner;

  //Can be populated after upload
  name: string;
}

export interface ShortUser {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  partner: ShortPartner;
}

export interface DisplayUser {
  id: number;
  displayName: string;
}

export interface UpdateUserRequest {
  email: string;
  firstName: string;
  jobCreator: boolean;
  lastName: string;
  partnerId: number;
  password?: string;
  readOnly: boolean;
  role: string;
  approverId: number;
  purpose: string;
  sourceCountries: Country[];
  status: string;
  username: string;
  usingMfa: boolean;
}

export enum Role {
  systemadmin = "System Admin",
  admin = "Full Admin",
  partneradmin = "Partner Admin",
  semilimited = "Semi Limited",
  limited = "Limited"
}

export function roleGreaterThan(role1: Role, role2: Role): boolean {

  //Populate this array with roles that are greater than role2
  let greaterRoles: Role[];
  switch (role2) {
    case Role.systemadmin:
      greaterRoles = [];
      break;

    case Role.admin:
      greaterRoles = [Role.systemadmin]
      break;

    case Role.partneradmin:
      greaterRoles = [Role.admin, Role.systemadmin]
      break;

    case Role.semilimited:
      greaterRoles = [Role.partneradmin, Role.admin, Role.systemadmin]
      break;

    case Role.limited:
      greaterRoles = [Role.semilimited, Role.partneradmin, Role.admin, Role.systemadmin]
      break;
  }

  return greaterRoles.includes(role1);
}
