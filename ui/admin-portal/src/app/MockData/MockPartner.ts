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

import {Partner} from "../model/partner";
import {Country} from "../model/country";
import {MockUser} from "./MockUser";
import {MockJobChat} from "./MockJobChat";
import {MockEmployer} from "./MockEmployer";

export class MockPartner implements Partner {
  id: number = 1;
  abbreviation: string = "MP";
  autoAssignable: boolean = true;
  defaultPartnerRef: boolean = false;
  jobCreator: boolean = true;
  logo: string = "mock_logo.png";
  name: string = "Mock Partner";
  notificationEmail: string = "partner@example.com";
  registrationLandingPage: string = "https://example.com/register";
  sflink: string = "https://example.com/sf";
  sourcePartner: boolean = true;
  status: string = "Active";
  websiteUrl: string = "https://example.com";
  defaultJobCreator: boolean = true;
  defaultSourcePartner: boolean = true;
  sourceCountries: Country[] = [{ id: 1, name: "Mock Country", status: "Active", translatedName: "Mock Country" }];
  _jobChat?: MockJobChat;
  defaultContact?: MockUser;
  employer?: MockEmployer;
}
