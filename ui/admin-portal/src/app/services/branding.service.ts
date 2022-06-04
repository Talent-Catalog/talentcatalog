import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

export interface BrandingInfo {
  logo: string;
  websiteUrl: string;
}

@Injectable({
  providedIn: 'root'
})
export class BrandingService {
  apiUrl: string = environment.apiUrl + '/branding';

  constructor(private http: HttpClient) { }

  getBrandingInfo(): Observable<BrandingInfo> {
    return this.http.get<BrandingInfo>(`${this.apiUrl}`);
  }
}
