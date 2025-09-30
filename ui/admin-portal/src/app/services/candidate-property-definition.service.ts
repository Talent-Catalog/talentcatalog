import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {SpringDataCandidatePropertyDefinitionsPage} from "../model/candidate-property-definition";
import {environment} from "../../environments/environment";


@Injectable({
  providedIn: 'root'
})
export class CandidatePropertyDefinitionService {
  private readonly apiUrl = environment.halApiUrl + '/candidate-property-definitions';

  constructor(private http: HttpClient) { }

  get(page = 0, size = 20): Observable<SpringDataCandidatePropertyDefinitionsPage> {
    let params = new HttpParams()
    .set('page', page)
    .set('size', size);
    return this.http.get<SpringDataCandidatePropertyDefinitionsPage>(this.apiUrl, {params: params});
  }
}
