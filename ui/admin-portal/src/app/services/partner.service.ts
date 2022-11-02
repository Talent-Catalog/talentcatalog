import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {SearchPartnerRequest} from "../model/base";
import {Partner, UpdatePartnerRequest} from "../model/partner";

@Injectable({
  providedIn: 'root'
})
export class PartnerService {

  private apiUrl: string = environment.apiUrl + '/partner';

  constructor(private http: HttpClient) { }


  search(request: SearchPartnerRequest): Observable<Partner[]> {
    return this.http.post<Partner[]>(`${this.apiUrl}/search`, request);
  }

  searchPaged(request: SearchPartnerRequest): Observable<SearchResults<Partner>> {
    return this.http.post<SearchResults<Partner>>(`${this.apiUrl}/search-paged`, request);
  }

  create(request: UpdatePartnerRequest): Observable<Partner> {
    return this.http.post<Partner>(`${this.apiUrl}`, request);
  }

  listPartners(): Observable<Partner[]> {
    //If we already have the data return it, otherwise get it.
    return this.http.get<Partner[]>(`${this.apiUrl}`);
 }

  update(id: number, request: UpdatePartnerRequest): Observable<Partner>  {
    return this.http.put<Partner>(`${this.apiUrl}/${id}`, request);
  }
}
