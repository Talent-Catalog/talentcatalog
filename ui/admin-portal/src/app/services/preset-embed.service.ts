import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";
import {embedDashboard, EmbeddedDashboard} from "@preset-sdk/embedded";
import {map} from "rxjs/operators";

interface PresetGuestTokenResponse {
  payload: {
    token: string;
  };
}

@Injectable({
  providedIn: 'root'
})
export class PresetEmbedService {

  private apiBaseUrl = environment.apiUrl + '/preset';
  private presetDomain: string = 'https://' + environment.presetWorkspaceId + '.us2a.app.preset.io';

  constructor(private http: HttpClient) { }

  private fetchGuestToken(dashboardId: string): Promise<string> {
    return this.http
        .post<PresetGuestTokenResponse>(`${this.apiBaseUrl}/${dashboardId}/guest-token`, null)
        .pipe(map(response => response.payload.token))
        .toPromise();
  }

  embedDashboard(
    dashboardId: string,
    mountPoint: HTMLElement
  ): Observable<void> {
    embedDashboard({
      id: dashboardId,
      supersetDomain: this.presetDomain,
      mountPoint,
      fetchGuestToken: () => this.fetchGuestToken(dashboardId),
      dashboardUiConfig: {
        hideTitle: true,
        hideChartControls: true,
        hideTab: true,
        filters: { expanded: true },
        urlParams: {}
      },
    });
  }

}
