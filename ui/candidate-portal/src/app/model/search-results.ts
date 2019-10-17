export class SearchResults<ResultType> {
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  content: ResultType[];
}


