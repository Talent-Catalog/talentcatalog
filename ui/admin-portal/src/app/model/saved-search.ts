
export interface SavedSearch {
  id: number;
  name: string;
  keyword: string;
  gender: string;
  statuses: string;
  //todo filters
}

export interface SavedSearchJoin {
  savedSearchId: number;
  name: string;
  searchType: 'and' | 'or';
}
