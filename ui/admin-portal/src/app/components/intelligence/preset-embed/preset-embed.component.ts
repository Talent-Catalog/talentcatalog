import {Component, Input} from '@angular/core';
import {PresetEmbedService} from "../../../services/preset-embed.service";

@Component({
  selector: 'app-preset-embed',
  templateUrl: './preset-embed.component.html',
  styleUrls: ['./preset-embed.component.scss']
})
export class PresetEmbedComponent {
  @Input() private dashboardId: string;
  private dashboardContainer: HTMLElement;

  error: any;
  loading = false;

  constructor(private presetEmbedService: PresetEmbedService) { }

  ngOnInit(): void {
    this.loading = true;
    this.dashboardContainer = document.getElementById('dashboard');

    this.presetEmbedService.embedDashboard(
      this.dashboardId,
      this.dashboardContainer,
    ).then((dashboard) => {
      this.loading = false;
      this.setDashboardIframeSize();
    }).catch((err) => {
      this.loading = false;
      this.error = err;
    });
  }

  private setDashboardIframeSize(): void {
    const dashboardIframe =
      this.dashboardContainer.querySelector('iframe');
    if (dashboardIframe) {
      dashboardIframe.style.width = '100%'; // Set the width as needed
      dashboardIframe.style.height = '1000px'; // Set the height as needed
    }
  }

}
