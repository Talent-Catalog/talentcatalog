import {Component, Input} from '@angular/core';
import { embedDashboard } from "@preset-sdk/embedded";
import {Observable} from "rxjs";
import {PresetEmbedService} from "../../../services/preset-embed.service";

@Component({
  selector: 'app-preset-embed',
  templateUrl: './preset-embed.component.html',
  styleUrls: ['./preset-embed.component.scss']
})
export class PresetEmbedComponent {
  @Input("dashboardId") dashboardId: string;

  error: any;
  loading: boolean;

  constructor(private presetEmbedService: PresetEmbedService) { }

  embedDashboard(): Observable<void> {
    this.loading = true;
    return new Observable((observer) => {
      this.presetEmbedService.fetchGuestToken(this.dashboardId).subscribe(
        (token) => {
          embedDashboard({
            id: this.dashboardId,
            supersetDomain: 'http://localhost:8088', // TODO from the embedded dialog
            mountPoint: document.getElementById('dashboard'),
            fetchGuestToken: () => token["token"],
            dashboardUiConfig: {
              hideTitle: true,
              hideChartControls: true,
              hideTab: true,
              filters: {
                expanded: true,
              },
              urlParams: { }
            },
          });
          this.loading = false;
          observer.next();
          observer.complete();
        },
        (error) => {
          this.loading = false;
          this.error = error;
        }
      );
    });
  }

}
