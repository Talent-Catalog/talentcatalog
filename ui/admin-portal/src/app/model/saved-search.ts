import {SearchCandidateRequest} from "./search-candidate-request";

export enum SavedSearchType {
  profession = 'profession',
  job = 'job',
  other = 'other'
}

export interface SavedSearchJoin {
  savedSearchId: number;
  name: string;
  searchType: 'and' | 'or';
  childSavedSearch: SavedSearch;
}

/**
 * This is what saved searches look like when received from the server.
 * todo - there are clearly different types depending on the call: get, load or search
 * todo - for example, with search occupation ids are strings - not array of numbers
 * todo no paging info
 */
export interface SavedSearch extends SearchCandidateRequest {
  id: number;
  name: string;
  type: SavedSearchType;
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
  type?: SavedSearchType;

  searchCandidateRequest?: SearchCandidateRequest;
}

export interface SavedSearchRunRequest {
  savedSearchId: number;
  reviewStatus?: string;
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
  savedSearchRequest.type = savedSearch.type;
  savedSearchRequest.searchCandidateRequest = searchCandidateRequest;
  return savedSearchRequest;
}
