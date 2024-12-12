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



import {User} from "../model/user";
import {Country} from "../model/country";
import {MockPartner} from "./MockPartner";

 export class MockUser implements User {
   id: number = 1;
   username: string = "mockuser";
   firstName: string = "John";
   lastName: string = "Doe";
   email: string = "john.doe@example.com";
   role: string = "Limited";
   jobCreator: boolean = true;
   approver: User | undefined;
   purpose: string = "Mock purpose";
   readOnly: boolean = false;
   sourceCountries: Country[] = [{ id: 1, name: "Mock Country", status: "Active", translatedName: "Mock Country" }];
   status: string = "Active";
   createdDate: number;
   createdBy: User | undefined;
   updatedDate: number;
   lastLogin: number;
   usingMfa: boolean = false;
   mfaConfigured: boolean = false;
   partner:  MockPartner = new MockPartner();
   name: string = "Mock User";

   constructor() {
     // Set the date properties to a specific date value
     const staticDate = new Date("2024-05-01").getTime(); // Change the date as needed
     this.createdDate = staticDate;
     this.updatedDate = staticDate;
     this.lastLogin = staticDate;
   }
 }


