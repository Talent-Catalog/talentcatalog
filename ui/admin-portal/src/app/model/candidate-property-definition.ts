/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
/**
 * Candidate Property Definition
 */
export interface CandidatePropertyDefinition {
  name: string;
  label: string;
  definition: string;
  type: string;
}

/**
 * Spring Data Candidate Property Definitions Page.
 * <p>
 * See the Spring Data REST documentation.
 * https://docs.spring.io/spring-data/rest/reference/paging-and-sorting.html
 */
export interface SpringDataCandidatePropertyDefinitionsPage {
  _embedded: {
    candidatePropertyDefinitions: CandidatePropertyDefinition[];
  };
  page: {
    size: number;
    totalElements: number;
    totalPages: number;
    number: number;
  };
}
