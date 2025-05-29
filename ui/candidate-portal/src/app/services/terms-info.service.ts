import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";
import {TermsInfoDto, TermsType} from "../model/terms-info-dto";

@Injectable({
  providedIn: 'root'
})
export class TermsInfoService {
  //todo Can we just go through one api - like chat. see special environment api
  private apiUrl: string = environment.termsInfoApiUrl + '/terms-info';

  constructor(private http: HttpClient) { }

  getCurrentByType(type: TermsType): Observable<TermsInfoDto> {
    let typeName = TermsType[type];
    return this.http.get<TermsInfoDto>(`${this.apiUrl}/type/${typeName}`);
  }

}
