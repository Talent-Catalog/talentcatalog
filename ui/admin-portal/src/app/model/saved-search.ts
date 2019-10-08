
export interface SavedSearch {
  id: number;
  name: string;
  //todo filters
}

export interface SavedSearchJoin {
  savedSearchId: number;
  name: string;
  searchType: 'and' | 'or';
}
