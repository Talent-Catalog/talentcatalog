import {SearchCandidateRequest} from "./search-candidate-request";
import {SavedSearchTypeInfo} from "../services/saved-search.service";
import {
  Auditable,
  CandidateSource,
  PagedSearchRequest,
  SearchCandidateSourcesRequest
} from "./base";
import {Router} from "@angular/router";
import {Location} from "@angular/common";
import {TargetListSelection} from "../components/list/select/select-list.component";
import {getExternalHref} from "../util/url";

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

export interface SavedSearch extends CandidateSource, SearchCandidateRequest {
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

export function getCandidateSourceBreadcrumb(candidateSource: CandidateSource): string {
  const sourceType = getCandidateSourceType(candidateSource);
  return candidateSource != null ?
    (sourceType + ': ' + candidateSource.name) : sourceType;
}

export function getSavedSearchBreadcrumb(savedSearch: SavedSearch, infos: SavedSearchTypeInfo[]): string {
  let breadcrumb: string = "";
  if (savedSearch) {
    breadcrumb += getCandidateSourceType(savedSearch) + ": ";

    if (savedSearch.defaultSearch) {
      breadcrumb += "Unsaved"
    } else {
      if (savedSearch.savedSearchType != null) {
        breadcrumb += infos[savedSearch.savedSearchType].title;
      }
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

export interface ClearSelectionRequest {
  //User making the selections
  userId: number;
}

export interface SaveSelectionRequest extends TargetListSelection{
  //User making the selections
  userId?: number;
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
