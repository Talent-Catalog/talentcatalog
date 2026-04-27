import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";
import {TermsInfoDto, TermsType} from "../model/terms-info-dto";
import {AuthenticationService} from "./authentication.service";

@Injectable({
  providedIn: 'root'
})
export class TermsInfoService {
  private apiUrl: string = environment.termsInfoApiUrl + '/terms-info';

  constructor(
    private http: HttpClient,
    private authenticationService: AuthenticationService
  ) { }

  getCurrentCandidatePolicy(): Observable<TermsInfoDto> {
    let termsType = this.authenticationService.getCandidatePolicyType();
    return this.getCurrentByType(termsType);
  }

  getCurrentByType(type: TermsType): Observable<TermsInfoDto> {
    let typeName = TermsType[type];
    return this.http.get<TermsInfoDto>(`${this.apiUrl}/type/${typeName}`);
  }

}
