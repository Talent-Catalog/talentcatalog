import {SavedSearchJoin} from "../model/saved-search";
import {MockSavedSearch} from "./MockSavedSearch";

export class MockSavedJoin implements SavedSearchJoin {
  savedSearchId: number;
  name: string;
  searchType: 'and' | 'or';
  childSavedSearch: MockSavedSearch;

  constructor(childSavedSearch: MockSavedSearch) {
    this.savedSearchId = 2;
    this.name = 'Mock Join 1';
    this.searchType = 'and';
    this.childSavedSearch = childSavedSearch;
  }
}
