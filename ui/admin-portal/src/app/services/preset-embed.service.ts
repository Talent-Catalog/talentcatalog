import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class PresetEmbedService {

  private apiBaseUrl = environment.apiUrl + '/preset';

  constructor(private http: HttpClient) { }

  public fetchGuestToken(dashboardId: string): Observable<string> {
    return this.http.post<string>(`${this.apiBaseUrl}/${dashboardId}/guest-token`, null);
  }
}
