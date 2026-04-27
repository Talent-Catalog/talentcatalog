import {Component, Input} from '@angular/core';
import {PresetEmbedService} from "../../../services/preset-embed.service";

@Component({
  selector: 'app-preset-embed',
  templateUrl: './preset-embed.component.html',
  styleUrls: ['./preset-embed.component.scss']
})
export class PresetEmbedComponent {
  @Input() dashboardId: string;
  private dashboardContainer: HTMLElement;

  error: any;
  loading = false;

  constructor(private presetEmbedService: PresetEmbedService) { }

  ngOnInit(): void {
    this.loading = true;
    this.dashboardContainer = document.getElementById('dashboard');

    if (!this.dashboardId) {
      this.error = 'Dashboard embedding requires a valid dashboard ID from the parent component.';
      this.loading = false;
      return;
    }

    if (!this.dashboardContainer) {
      this.error = 'Dashboard embedding requires an element with ID "dashboard" as a mount point.';
      this.loading = false;
      return;
    }

    this.presetEmbedService.embedDashboard(this.dashboardId, this.dashboardContainer)
      .then(() => this.setDashboardIframeSize())
      .catch((err) => this.error = err)
      .finally(() => this.loading = false);
  }

  /**
   * Sets the iframe to fill the available screen size, ensuring that the bottom LH 'Apply filters'
   * and 'Clear all' buttons are visible, which was otherwise prone to cause confusion.
   * @private
   */
  private setDashboardIframeSize(): void {
    const dashboardIframe =
      this.dashboardContainer.querySelector('iframe');
    if (dashboardIframe) {
      const yOffset = dashboardIframe.getBoundingClientRect().top;
      dashboardIframe.style.width = '100%';
      dashboardIframe.style.height = (window.innerHeight - yOffset) + 'px';
    }
  }

}
