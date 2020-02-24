import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable, of} from "rxjs";
import {SearchResults} from "../model/search-results";
import {
  SavedSearch,
  SavedSearchRequest,
  SavedSearchSubtype, SavedSearchType
} from "../model/saved-search";
import {map} from "rxjs/operators";

export interface SavedSearchTypeInfo {
  savedSearchType?: SavedSearchType;
  title: string;
  categories?: SavedSearchTypeSubInfo[];
}

export interface SavedSearchTypeSubInfo {
  savedSearchSubtype?: SavedSearchSubtype;
  title: string;
}

@Injectable({
  providedIn: 'root'
})
export class SavedSearchService {

  private apiUrl: string = environment.apiUrl + '/saved-search';

  private readonly savedSearchTypeInfos: SavedSearchTypeInfo[] = [];

  constructor(private http: HttpClient) {
    const profCategories: SavedSearchTypeSubInfo[] = [
      {savedSearchSubtype: SavedSearchSubtype.business, title: 'Category 1 - Business / Finance'},
      {savedSearchSubtype: SavedSearchSubtype.agriculture, title: 'Category 2 - Agriculture & Livestock'},
      {savedSearchSubtype: SavedSearchSubtype.healthcare, title: 'Category 3 - Healthcare'},
      {savedSearchSubtype: SavedSearchSubtype.engineering, title: 'Category 4 - Engineering & Architecture'},
      {savedSearchSubtype: SavedSearchSubtype.food, title: 'Category 5 - Food Related'},
      {savedSearchSubtype: SavedSearchSubtype.education, title: 'Category 6 - Education'},
      {savedSearchSubtype: SavedSearchSubtype.labourer, title: 'Category 7 - Construction/laborers'},
      {savedSearchSubtype: SavedSearchSubtype.trade, title: 'Category 8 - Skilled Trades (construction related) '},
      {savedSearchSubtype: SavedSearchSubtype.arts, title: 'Category 9 - Arts / Design'},
      {savedSearchSubtype: SavedSearchSubtype.it, title: 'Category 10 - IT / Tech'},
      {savedSearchSubtype: SavedSearchSubtype.social, title: 'Category 11 - Social / Humanitarian Related'},
      {savedSearchSubtype: SavedSearchSubtype.science, title: 'Category 12 - Science Related'},
      {savedSearchSubtype: SavedSearchSubtype.law, title: 'Category 13 - Law'},
      {savedSearchSubtype: SavedSearchSubtype.other, title: 'Category 14 - Other'},
    ];

    //Maybe use these as categories in future
    const jobCategories: SavedSearchTypeSubInfo[] = [
      {savedSearchSubtype: SavedSearchSubtype.au, title: 'Australia'},
      {savedSearchSubtype: SavedSearchSubtype.ca, title: 'Canada'},
      {savedSearchSubtype: SavedSearchSubtype.uk, title: 'UK'},
    ];
    this.savedSearchTypeInfos[SavedSearchType.profession] =
      {savedSearchType: SavedSearchType.profession,
        title: 'Professions',
        categories: profCategories
      };

    this.savedSearchTypeInfos[SavedSearchType.job] =
      {savedSearchType: SavedSearchType.job,
        title: 'Jobs',
      };

    this.savedSearchTypeInfos[SavedSearchType.other] =
      {savedSearchType: SavedSearchType.other,
        title: 'Other'
      };
  }

  getSavedSearchTypeInfos(): SavedSearchTypeInfo[] {
    return this.savedSearchTypeInfos;
  }

  search(request): Observable<SearchResults<SavedSearch>> {
    return this.http.post<SearchResults<SavedSearch>>(`${this.apiUrl}/search`, request)
      .pipe(
        map(results => this.processPostResults(results))
      );
  }

  processPostResults(results: SearchResults<SavedSearch>): SearchResults<SavedSearch> {
    for (let savedSearch of results.content) {
      savedSearch = SavedSearchService.convertSavedSearchEnums(savedSearch);
    }
    return results;
  };

  load(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}/load`);
  }

  get(id: number): Observable<SavedSearch> {
    return this.http.get<SavedSearch>(`${this.apiUrl}/${id}`)
      .pipe(
        map(savedSearch => SavedSearchService.convertSavedSearchEnums(savedSearch))
      );
  }

  create(savedSearchRequest: SavedSearchRequest): Observable<SavedSearch>  {
    return this.http.post<SavedSearch>(`${this.apiUrl}`, savedSearchRequest);
  }

  update(savedSearchRequest: SavedSearchRequest): Observable<SavedSearch>  {
    return this.http.put<SavedSearch>(`${this.apiUrl}/${savedSearchRequest.id}`, savedSearchRequest);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

  private static convertSavedSearchEnums(savedSearch: any): SavedSearch {
    if (typeof savedSearch.savedSearchType === "string") {
      savedSearch.savedSearchType = SavedSearchType[savedSearch.savedSearchType];
    }
    if (typeof savedSearch.savedSearchSubtype === "string") {
      savedSearch.savedSearchSubtype = SavedSearchSubtype[savedSearch.savedSearchSubtype];
    }
    return savedSearch;
  }

}
