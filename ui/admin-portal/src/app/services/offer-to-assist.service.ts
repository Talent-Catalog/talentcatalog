import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {OfferToAssist} from "../model/offer-to-assist";
import {KeywordPagedSearchRequest} from "../model/base";

@Injectable({
  providedIn: 'root'
})

export class OfferToAssistService {

  private apiUrl: string = environment.apiUrl + '/ota';

  constructor(private http: HttpClient) { }

  search(request: KeywordPagedSearchRequest): Observable<SearchResults<OfferToAssist>> {
    return this.http.post<SearchResults<OfferToAssist>>(`${this.apiUrl}/search`, request);
  }
}
