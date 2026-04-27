import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class LinkedinPremiumCouponService {
  private readonly provider: string = 'LINKEDIN';
  private readonly serviceCode: string = 'PREMIUM_MEMBERSHIP';
  private apiBaseUrl = environment.apiUrl + '/services';

  constructor(private http: HttpClient) { }

  /**
   * Import coupons from a CSV file
   * @param file - CSV file containing coupons
   */
  importCoupons(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<any>(
      `${this.apiBaseUrl}/${this.provider}/${this.serviceCode}/import`, formData,
      {headers: new HttpHeaders({ 'enctype': 'multipart/form-data' })}
    );
  }

  /**
   * Get the count of available coupons
   */
  countAvailableCoupons(): Observable<any> {
    return this.http.get<any>(
      `${this.apiBaseUrl}/${this.provider}/${this.serviceCode}/available/count`
    );
  }

}
