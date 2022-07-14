import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

export interface BrandingInfo {
  logo: string;
  partnerName: string;
  websiteUrl: string;
}

@Injectable({
  providedIn: 'root'
})
export class BrandingService {
  apiUrl: string = environment.apiUrl + '/branding';
  partnerAbbreviation: string;

  constructor(private http: HttpClient) { }

  getBrandingInfo(): Observable<BrandingInfo> {
    let url = `${this.apiUrl}`;
    if (this.partnerAbbreviation) {
      url += `?p=${this.partnerAbbreviation}`;
    }
    return this.http.get<BrandingInfo>(url);
  }

  setPartnerAbbreviation(partnerAbbreviation: string) {
    this.partnerAbbreviation = partnerAbbreviation;
  }
}
