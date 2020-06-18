import {SearchCandidateRequest} from "./search-candidate-request";
import {SavedSearchTypeInfo} from "../services/saved-search.service";
import {Auditable, CandidateSource} from "./base";

export enum ReviewedStatus {
  pending,
  verified,
  rejected
}

export enum SearchBy {
  type,
  all,
  mySearches,
  sharedWithMe
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

export interface SavedSearch extends CandidateSource, SearchCandidateRequest {
  reviewable: boolean;
  savedSearchType: SavedSearchType;
  savedSearchSubtype: SavedSearchSubtype;
}

export function getSavedSearchBreadcrumb(savedSearch: SavedSearch, infos: SavedSearchTypeInfo[]): string {
  let subtypeTitle: string = '';
  if (savedSearch) {
    if (savedSearch.savedSearchSubtype != null) {
      const savedSearchTypeSubInfos = infos[savedSearch.savedSearchType].categories;
      if (savedSearchTypeSubInfos) {
        const savedSearchTypeSubInfo = savedSearchTypeSubInfos.find(
          info => info.savedSearchSubtype === savedSearch.savedSearchSubtype);
        if (savedSearchTypeSubInfo) {
          subtypeTitle = savedSearchTypeSubInfo.title;
        }
      }
    }
  }

  return savedSearch && savedSearch.savedSearchType != null ?
    (infos[savedSearch.savedSearchType].title +
    (subtypeTitle ? "/" + subtypeTitle : "") + ': ' + savedSearch.name)
    : 'Search';
}

export function indexOfAuditable(id: number, auditables: Auditable[]): number {
  for (let i = 0; i < auditables.length; i++) {
    if (auditables[i].id === id) {
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

export interface SaveSelectionRequest {
  //User making the selections
  userId: number;

  //List to save to - 0 if new list
  savedListId: number;

  //Name of new list to be created (if any - only used if savedListId = 0
  newListName: string;

  //If true any existing contents of list are replace, otherwise contents are
  //added (merged).
  replace: boolean;
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
 * Create a SavedSearchRequest from a SavedSearch and a search request.
 * @param savedSearch Saved search
 * @param searchCandidateRequest Search request
 */
export function convertToSavedSearchRequest
(savedSearch: SavedSearch, searchCandidateRequest: SearchCandidateRequest):
  SavedSearchRequest {
  const savedSearchRequest: SavedSearchRequest = {};
  savedSearchRequest.id = savedSearch.id;
  savedSearchRequest.name = savedSearch.name;
  savedSearchRequest.fixed = savedSearch.fixed;
  savedSearchRequest.reviewable = savedSearch.reviewable;
  savedSearchRequest.savedSearchType = savedSearch.savedSearchType;
  savedSearchRequest.savedSearchSubtype = savedSearch.savedSearchSubtype;
  savedSearchRequest.searchCandidateRequest = searchCandidateRequest;
  return savedSearchRequest;
}
