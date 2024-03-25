import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {HelpLink, SearchHelpLinkRequest, UpdateHelpLinkRequest} from "../model/help-link";

@Injectable({
  providedIn: 'root'
})
export class HelpLinkService {
  private apiUrl: string = environment.apiUrl + '/help-link';

  constructor(private http: HttpClient) { }

  search(request: SearchHelpLinkRequest): Observable<HelpLink[]> {
    return this.http.post<HelpLink[]>(`${this.apiUrl}/search`, request);
  }

  searchPaged(request: SearchHelpLinkRequest): Observable<SearchResults<HelpLink>> {
    return this.http.post<SearchResults<HelpLink>>(`${this.apiUrl}/search-paged`, request);
  }

  create(request: UpdateHelpLinkRequest): Observable<HelpLink> {
    return this.http.post<HelpLink>(`${this.apiUrl}`, request);
  }

  update(id: number, request: UpdateHelpLinkRequest): Observable<HelpLink>  {
    return this.http.put<HelpLink>(`${this.apiUrl}/${id}`, request);
  }
}
