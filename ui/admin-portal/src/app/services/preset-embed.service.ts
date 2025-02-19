import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import { embedDashboard } from "@preset-sdk/embedded";

@Injectable({
  providedIn: 'root'
})
export class PresetEmbedService {

  private apiBaseUrl = environment.apiUrl + '/preset';

  constructor(private http: HttpClient) { }

  public fetchGuestToken(dashboardId: string): Observable<string> {
    return this.http.post<string>(`${this.apiBaseUrl}/${dashboardId}/guest-token`, null);
  }

  embedDashboard(
    dashboardId: string,
    presetDomain: string,
    mountPoint: HTMLElement
  ): Observable<void> {
    return this.fetchGuestToken(dashboardId).pipe(
      map(token => {
        embedDashboard({
          id: dashboardId,
          supersetDomain: presetDomain,
          mountPoint,
          fetchGuestToken: () => token["token"],
          dashboardUiConfig: {
            hideTitle: true,
            hideChartControls: true,
            hideTab: true,
            filters: { expanded: true },
            urlParams: {}
          },
        });
      })
    );
  }

}
