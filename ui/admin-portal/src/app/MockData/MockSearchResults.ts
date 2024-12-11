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

import {SearchResults} from "../model/search-results";
import {MockCandidateSource} from "./MockCandidateSource";

export class MockSearchResults implements SearchResults<MockCandidateSource> {
  number: number = 1;
  size: number = 10;
  totalElements: number = 1;
  totalPages: number = 1;
  first: boolean = true;
  last: boolean = true;
  content: MockCandidateSource[] = [new MockCandidateSource()]; // Use the MockCandidateSource as content
}


// Create an instance of MockSearchResults
const mockResults: MockSearchResults = new MockSearchResults();
