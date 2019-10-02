
export interface SavedSearch {
  id: number;
  name: string;
  //todo filters
}

export interface SavedSearchJoin {
  savedSearchId: number;
  searchType: 'and' | 'or';
}
