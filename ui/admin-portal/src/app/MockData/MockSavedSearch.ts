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

import {SavedSearch, SavedSearchSubtype, SavedSearchType} from "../model/saved-search";
import {ExportColumn} from "../model/saved-list";
import {OpportunityIds} from "../model/opportunity";
import {MockUser} from "./MockUser";
import {MockSavedJoin} from "./MockSavedJoin";

export class MockSavedSearch implements SavedSearch {
  defaultSearch: boolean = true;
  reviewable: boolean = false;
  savedSearchType: SavedSearchType = SavedSearchType.profession;
  savedSearchSubtype: SavedSearchSubtype = SavedSearchSubtype.engineering;
  name: string = 'Mock Saved Search';
  description: string = 'Mock description';
  displayedFieldsLong: string[] = ['Field 1', 'Field 2'];
  displayedFieldsShort: string[] = ['Short Field 1', 'Short Field 2'];
  exportColumns: ExportColumn[] = [
    {
      key: 'Key1',
      properties: {
        header: 'Header 1',
        constant: 'Constant Value 1'
      }
    },
    {
      key: 'Key2',
      properties: {
        header: 'Header 2',
        constant: 'Constant Value 2'
      }
    },
  ];
  fixed: boolean = true;
  global: boolean = false;
  sfJobOpp: OpportunityIds = {
    id: 123,
    sfId: 'sfId123', // Assign the sfId property if needed
  };
  users: MockUser[] = [new MockUser()];
  watcherUserIds: number[] = [1, 2, 3];
  simpleQueryString: string = 'mock query string';
  keyword: string = 'mock keyword';
  gender: string = 'Male';
  regoReferrerParam: string = 'Mock referrer param';
  statuses: string[] = ['Status 1', 'Status 2'];
  occupationIds: number[] = [1, 2, 3];
  orProfileKeyword: string = 'Mock profile keyword';
  partnerIds: number[] = [1, 2];
  nationalityIds: number[] = [1, 2];
  nationalitySearchType: string = 'Search Type';
  countryIds: number[] = [1, 2];
  countrySearchType: string = 'Country Search Type';
  englishMinWrittenLevel: number = 80;
  englishMinSpokenLevel: number = 90;
  otherLanguageId: number = 1;
  otherMinWrittenLevel: number = 70;
  otherMinSpokenLevel: number = 80;
  lastModifiedFrom: string = '2024-05-01';
  lastModifiedTo: string = '2024-05-31';
  createdFrom: string = '2024-01-01';
  createdTo: string = '2024-04-30';
  minAge: number = 25;
  maxAge: number = 40;
  minEducationLevel: number = 5;
  educationMajorIds: number[] = [1, 2];
  surveyTypeIds: number[] = [1, 2];
  countryNames: string[] = ['Country 1', 'Country 2'];
  partnerNames: string[] = ['Partner 1', 'Partner 2'];
  nationalityNames: string[] = ['Nationality 1', 'Nationality 2'];
  vettedOccupationNames: string[] = ['Occupation 1', 'Occupation 2'];
  occupationNames: string[] = ['Occupation Name 1', 'Occupation Name 2'];
  educationMajors: string[] = ['Major 1', 'Major 2'];
  englishWrittenLevel: string = 'B2';
  englishSpokenLevel: string = 'C1';
  otherWrittenLevel: string = 'B1';
  otherSpokenLevel: string = 'A2';
  minEducationLevelName: string = 'Bachelor';
  includeDraftAndDeleted: boolean = true;
  searchJoins: MockSavedJoin[];
  exclusionListId: number = 1;
  miniIntakeCompleted: boolean = true;
  fullIntakeCompleted: boolean = false;
  unhcrStatuses = [];
  listAnyIds = [];
  listAnySearchType = null;
  listAllIds =  [];
  listAllSearchType = null;


  constructor(searchJoins: MockSavedJoin[] = []) {
    // Initialize other properties
    this.searchJoins = searchJoins;
  }
}
