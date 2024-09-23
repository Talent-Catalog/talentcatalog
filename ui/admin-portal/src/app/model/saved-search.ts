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

import {SearchCandidateRequest} from './search-candidate-request';
import {SavedSearchTypeInfo} from '../services/saved-search.service';
import {CandidateSource, HasId, PagedSearchRequest, SearchCandidateSourcesRequest} from './base';
import {Router} from '@angular/router';
import {Location} from '@angular/common';
import {getExternalHref} from '../util/url';

export enum SavedSearchType {
  profession,
  job ,
  other
}

export enum SavedSearchSubtype {
   //Profession subtypes
   business,
   agriculture,
   healthcare,
   engineering,
   food,
   education,
   labourer,
   trade,
   arts,
   it,
   social,
   science,
   law,
   other,

   //Job subtypes
   au,
   ca,
   uk
}

export class SavedSearchGetRequest extends PagedSearchRequest {
  reviewStatusFilter: string[];
}

export interface SearchCandidateRequestPaged extends SearchCandidateRequest, SavedSearchGetRequest {

}

export interface SavedSearchJoin {
  savedSearchId: number;
  name: string;
  searchType: 'and' | 'or';
  childSavedSearch: SavedSearch;
}

export interface SavedSearchRef extends HasId {
}

export interface SavedSearch extends CandidateSource, SearchCandidateRequest, SavedSearchRef {
  defaultSearch: boolean;
  reviewable: boolean;
  savedSearchType: SavedSearchType;
  savedSearchSubtype: SavedSearchSubtype;
}

export class SearchSavedSearchRequest extends SearchCandidateSourcesRequest {
  savedSearchType: SavedSearchType;
  savedSearchSubtype: SavedSearchSubtype;
}

export function getCandidateSourceNavigation(source: CandidateSource): any[] {
  const urlSelector: string = isSavedSearch(source) ? 'search' : 'list';
  return [urlSelector, source.id];
}

export function getCandidateSourceStatsNavigation(source: CandidateSource): any[] {
  const statsUrl = "infographics";
  const urlSelector: string = isSavedSearch(source) ? 'search' : 'list';
  return [statsUrl, urlSelector, source.id];
}

export function getSavedSourceNavigation(source: SavedSearchRef): any[] {
  return ['search', source.id];
}

export function getCandidateSourceExternalHref(
  router: Router, location: Location, source: CandidateSource): string {
  return getExternalHref(router, location, getCandidateSourceNavigation(source));
}

export function getCandidateSourceType(source: CandidateSource) {
  return isSavedSearch(source) ? "Search" : "List";
}

export function isSavedSearch(source: CandidateSource): source is SavedSearch {
  return source ? 'savedSearchType' in source : false;
}

export function getSavedSearchBreadcrumb(savedSearch: SavedSearch, infos: SavedSearchTypeInfo[]): string {
  let breadcrumb: string = "";
  if (savedSearch) {
    breadcrumb += getCandidateSourceType(savedSearch) + ": ";

    if (savedSearch.defaultSearch) {
      breadcrumb += "Unsaved"
    } else {
      let subtypeTitle: string = '';
      if (savedSearch.savedSearchSubtype != null) {
        const savedSearchTypeSubInfos = infos[savedSearch.savedSearchType].categories;
        if (savedSearchTypeSubInfos) {
          const savedSearchTypeSubInfo = savedSearchTypeSubInfos.find(
            info => info.savedSearchSubtype === savedSearch.savedSearchSubtype);
          if (savedSearchTypeSubInfo) {
            subtypeTitle = savedSearchTypeSubInfo.title;
          }
          breadcrumb += " " + subtypeTitle + ": ";
        }
      }

      breadcrumb += savedSearch.name;
    }
  }
  return breadcrumb;
}

export function indexOfHasId(id: number, hasIds: HasId[]): number {
  for (let i = 0; i < hasIds.length; i++) {
    if (hasIds[i].id === id) {
      return i;
    }
  }
  return -1;
}

/**
 * This is what saved searches look like when they are sent to the server
 * (as create or update requests).
 * <p/>
 * Note that the actual search fields are broken out as a separate object field.
 */
export interface SavedSearchRequest {
  id?: number;
  name?: string;
  fixed?: boolean;
  reviewable?: boolean;
  jobId?: number;
  savedSearchType?: SavedSearchType;
  savedSearchSubtype?: SavedSearchSubtype;

  searchCandidateRequest?: SearchCandidateRequest;
}

export interface ClearSelectionRequest {
  //User making the selections
  userId: number;
}

export interface UpdateSharingRequest {
  savedSearchId: number;
}

export interface SelectCandidateInSearchRequest {
  userId: number;
  candidateId: number;
  selected: boolean;
}

/**
 * Create a SavedSearchRequest from a SavedSearch an optional Salesforce job link, and a search request.
 * @param savedSearch Saved search
 * @param jobId ID of job - can be null
 * @param searchCandidateRequest Search request
 */
export function convertToSavedSearchRequest
(savedSearch: SavedSearch, jobId: number, searchCandidateRequest: SearchCandidateRequest):
  SavedSearchRequest {
  const savedSearchRequest: SavedSearchRequest = {};
  savedSearchRequest.id = savedSearch.id;
  savedSearchRequest.name = savedSearch.name;
  savedSearchRequest.fixed = savedSearch.fixed;
  savedSearchRequest.jobId = jobId;
  savedSearchRequest.reviewable = savedSearch.reviewable;
  savedSearchRequest.savedSearchType = savedSearch.savedSearchType;
  savedSearchRequest.savedSearchSubtype = savedSearch.savedSearchSubtype;
  savedSearchRequest.searchCandidateRequest = searchCandidateRequest;
  return savedSearchRequest;
}
