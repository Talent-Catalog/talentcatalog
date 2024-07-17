import {SearchResults} from "../model/search-results";
import {MockCandidateSource} from "./MockCandidateSource";

export class MockSearchResults implements SearchResults<MockCandidateSource> {
  number: number = 1;
  size: number = 10;
  totalElements: number = 1;
  totalPages: number = 1;
  first: boolean = true;
  last: boolean = true;
  content: MockCandidateSource[] = [new MockCandidateSource()]; // Use the MockCandidateSource as content
}


// Create an instance of MockSearchResults
const mockResults: MockSearchResults = new MockSearchResults();
