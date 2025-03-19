import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {embedDashboard, EmbeddedDashboard} from "@preset-sdk/embedded";
import {map} from "rxjs/operators";
import {EnvService} from "./env.service";

interface PresetGuestTokenResponse {
  payload: {
    token: string;
  };
}

@Injectable({
  providedIn: 'root'
})
export class PresetEmbedService {

  private readonly apiBaseUrl = environment.apiUrl + '/preset';

  constructor(private http: HttpClient, private envService: EnvService) { }

  /**
   * Embeds a Preset dashboard in the calling component.
   *
   * This method initializes and embeds a Preset dashboard into the specified
   * mount point using the `embedDashboard` function provided by the Preset embed SDK. It retrieves
   * the necessary guest token and applies default UI configurations.
   *
   * @param dashboardId - The unique identifier of the dashboard, obtained from the Preset dashboard UI.
   * @param mountPoint - An HTML element that serves as the container for the embedded iframe,
   * typically a `<div>`.
   * @returns A `Promise` resolving to an `EmbeddedDashboard` instance, which provides methods to
   * interact with the embedded dashboard.
   */
  public embedDashboard(
    dashboardId: string,
    mountPoint: HTMLElement
  ): Promise<EmbeddedDashboard> {
    return embedDashboard({
      id: dashboardId,
      supersetDomain: this.presetDomain,
      mountPoint,
      fetchGuestToken: () => this.fetchGuestToken(dashboardId),
      dashboardUiConfig: {
        hideTitle: false,
        hideChartControls: false,
        hideTab: true,
        filters: { expanded: true },
        urlParams: {}
      },
    });
  }

  /**
   * Fetches a guest token from TC server, which connects to Preset API.
   * @param dashboardId - The unique identifier of the dashboard, obtained from the Preset dashboard UI.
   * @return A Promise resolving to a PresetGuestTokenResponse object containing a guest token.
   * @private
   */
  private fetchGuestToken(dashboardId: string): Promise<string> {
    return this.http
        .post<PresetGuestTokenResponse>(`${this.apiBaseUrl}/${dashboardId}/guest-token`, null)
        .pipe(map(response => response.payload.token))
        .toPromise();
  }

  private get presetDomain(): string {
    return `https://${this.envService.presetWorkspaceId}.us2a.app.preset.io`;
  }

}
