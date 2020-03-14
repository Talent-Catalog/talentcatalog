import {SearchCandidateRequest} from "./search-candidate-request";

export enum ReviewedStatus {
  pending,
  verified,
  rejected
}

export const defaultReviewStatusFilter: string[] = [
  ReviewedStatus[ReviewedStatus.pending],
  ReviewedStatus[ReviewedStatus.verified]
];

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

export interface SavedSearchJoin {
  savedSearchId: number;
  name: string;
  searchType: 'and' | 'or';
  childSavedSearch: SavedSearch;
}

export interface SavedSearch extends SearchCandidateRequest {
  id: number;
  name: string;
  savedSearchType: SavedSearchType;
  savedSearchSubtype: SavedSearchSubtype;
}

export function indexOfSavedSearch(savedSearchID: number, savedSearches: SavedSearch[]): number {
  for (let i = 0; i < savedSearches.length; i++) {
    if (savedSearches[i].id == savedSearchID) {
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
  savedSearchType?: SavedSearchType;
  savedSearchSubtype?: SavedSearchSubtype;

  searchCandidateRequest?: SearchCandidateRequest;
}

export interface SavedSearchRunRequest {
  savedSearchId: number;
  shortlistStatus?: string[];
  pageNumber?: number;
  pageSize?: number;
  sortFields?: string[];
  sortDirection?: string;
}

/**
 * Create a SavedSearchRequest from a SavedSearch and a search request.
 * @param savedSearch
 * @param searchCandidateRequest
 */
export function convertToSavedSearchRequest
(savedSearch: SavedSearch, searchCandidateRequest: SearchCandidateRequest):
  SavedSearchRequest {
  const savedSearchRequest: SavedSearchRequest = {};
  savedSearchRequest.id = savedSearch.id;
  savedSearchRequest.name = savedSearch.name;
  savedSearchRequest.savedSearchType = savedSearch.savedSearchType;
  savedSearchRequest.savedSearchSubtype = savedSearch.savedSearchSubtype;
  savedSearchRequest.searchCandidateRequest = searchCandidateRequest;
  return savedSearchRequest;
}
