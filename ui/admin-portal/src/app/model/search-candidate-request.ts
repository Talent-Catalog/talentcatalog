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

import {SavedSearchJoin} from './saved-search';

export interface SearchCandidateRequest {
  useOldSearch?: boolean;
  useFastSearch?: boolean;
  simpleQueryString?: string;
  keyword?: string;
  gender?: string;
  regoReferrerParam?: string;
  regoUtmCampaign?: string;
  regoUtmSource?: string;
  regoUtmMedium?: string;
  statuses?: string[];
  occupationIds?: number[];
  orProfileKeyword?: string;
  partnerIds?: number[];
  nationalityIds?: number[];
  nationalitySearchType?: string;
  countryIds?: number[];
  countrySearchType?: string;
  englishMinWrittenLevel?: number;
  englishMinSpokenLevel?: number;
  otherLanguageId?: number;
  otherMinWrittenLevel?: number;
  otherMinSpokenLevel?: number;
  lastModifiedFrom?: string;
  lastModifiedTo?: string;
  createdFrom?: string;
  createdTo?: string;
  minAge?: number;
  maxAge?: number;
  minEducationLevel?: number;
  educationMajorIds?: number[];
  surveyTypeIds?: number[];
  countryNames?: string[];
  partnerNames?: string[];
  nationalityNames?: string[];
  vettedOccupationNames?: string[];
  occupationNames?: string[];
  educationMajors?: string[];
  englishWrittenLevel?: string;
  englishSpokenLevel?: string;
  otherWrittenLevel?: string;
  otherSpokenLevel?: string;
  minEducationLevelName?: string;
  includeDraftAndDeleted?: boolean;
  includePendingTermsCandidates?: boolean;
  searchJoins?: SavedSearchJoin[];
  exclusionListId?: number;
  miniIntakeCompleted?: boolean;
  fullIntakeCompleted?: boolean;
  potentialDuplicate?: boolean;
  unhcrStatuses?: string[];
  listAnyIds?: number[];
  listAnySearchType?: string;
  listAllIds?: number[];
  listAllSearchType?: string;
  candidateNumbers?: string[];
}
