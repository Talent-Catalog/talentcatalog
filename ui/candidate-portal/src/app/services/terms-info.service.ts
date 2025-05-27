import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";
import {TermsInfo, TermsType} from "../model/terms-info";

@Injectable({
  providedIn: 'root'
})
export class TermsInfoService {
  //todo Can we just go through one api - like chat. see special environment api
  private apiUrl: string = environment.termsInfoApiUrl + '/terms-info';

  constructor(private http: HttpClient) { }

  getCurrentByType(type: TermsType): Observable<TermsInfo> {
    return this.http.get<TermsInfo>(`${this.apiUrl}/type/${type}`);
  }

}
