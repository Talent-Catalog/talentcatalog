import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';

export interface VerifyPlusScanResult {
  unhcrNumber: string;
  duplicate: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class VerifyPlusService {
  private apiBaseUrl = environment.apiUrl + '/verify-plus';

  constructor(private http: HttpClient) {
  }

  submitScan(rawPayload: string): Observable<VerifyPlusScanResult> {
    return this.http.post<VerifyPlusScanResult>(this.apiBaseUrl, {rawPayload});
  }
}
