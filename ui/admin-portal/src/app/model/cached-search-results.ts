import {SearchResults} from "./search-results";
import {Candidate} from "./candidate";

export interface CachedSearchResults {
  searchID: number;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<Candidate>;
  timestamp: number;
}
