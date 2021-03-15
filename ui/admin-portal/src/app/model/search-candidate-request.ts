/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

import {Language} from './language';
import {SavedSearchJoin} from './saved-search';

export interface SearchCandidateRequest {
  simpleQueryString?: string;
  keyword?: string;
  gender?: string;
  statuses?: string;
  occupationIds?: number[];
  orProfileKeyword?: string;
  verifiedOccupationIds?: number[];
  verifiedOccupationSearchType?: string;
  nationalityIds?: number[];
  nationalitySearchType?: string;
  countryIds?: number[];
  englishMinWrittenLevel?: number;
  englishMinSpokenLevel?: number;
  otherLanguage?: Language;
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
  countryNames?: string[];
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
  searchJoins?: SavedSearchJoin[];
}
