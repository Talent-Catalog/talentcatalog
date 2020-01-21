import {SearchCandidateRequest} from "./search-candidate-request";


export enum SavedSearchType {
  profession = 'profession',
  job = 'job',
  other = 'other'
}

export interface SavedSearch {
  id: number;
  name: string;
  type: SavedSearchType;

  searchCandidateRequest: SearchCandidateRequest;
}

export interface SavedSearchJoin {
  savedSearchId: number;
  name: string;
  searchType: 'and' | 'or';
  childSavedSearch: SavedSearch;
}
