import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {SearchPartnerRequest, Status} from "../model/base";
import {Partner, UpdatePartnerJobContactRequest, UpdatePartnerRequest} from "../model/partner";
import {Job} from "../model/job";

@Injectable({
  providedIn: 'root'
})
export class PartnerService {

  private apiUrl: string = environment.apiUrl + '/partner';

  constructor(private http: HttpClient) { }

  listPartners(): Observable<Partner[]> {
    return this.http.get<Partner[]>(`${this.apiUrl}`);
  }

  search(request: SearchPartnerRequest): Observable<Partner[]> {
    return this.http.post<Partner[]>(`${this.apiUrl}/search`, request);
  }

  searchPaged(request: SearchPartnerRequest): Observable<SearchResults<Partner>> {
    return this.http.post<SearchResults<Partner>>(`${this.apiUrl}/search-paged`, request);
  }

  create(request: UpdatePartnerRequest): Observable<Partner> {
    return this.http.post<Partner>(`${this.apiUrl}`, request);
  }

  listSourcePartners(jobContext?: Job): Observable<Partner[]> {
    const request: SearchPartnerRequest = {
      contextJobId: jobContext?.id,
      sourcePartner: true,
      status: Status.active,
      sortFields: ["name"],
      sortDirection: "ASC"
    }
    return this.search(request);
 }

  update(id: number, request: UpdatePartnerRequest): Observable<Partner>  {
    return this.http.put<Partner>(`${this.apiUrl}/${id}`, request);
  }

  updateJobContact(id: number, request: UpdatePartnerJobContactRequest): Observable<Partner>  {
    return this.http.put<Partner>(`${this.apiUrl}/${id}/update-job-contact`, request);
  }
}
