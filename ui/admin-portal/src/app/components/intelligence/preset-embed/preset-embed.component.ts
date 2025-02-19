import {AfterViewInit, Component, Input} from '@angular/core';
import {PresetEmbedService} from "../../../services/preset-embed.service";

@Component({
  selector: 'app-preset-embed',
  templateUrl: './preset-embed.component.html',
  styleUrls: ['./preset-embed.component.scss']
})
export class PresetEmbedComponent implements AfterViewInit {
  @Input() dashboardId: string;

  error: any;
  loading = false;

  constructor(private presetEmbedService: PresetEmbedService) {}

  ngAfterViewInit(): void {
    if (!this.dashboardId) {
      this.error = "Dashboard ID is required";
      return;
    }

    this.loading = true;
    this.presetEmbedService.embedDashboard(
      this.dashboardId,
      'https://987e2e02.us2a.app.preset.io',
      document.getElementById('dashboard'),
    ).subscribe({
      next: () => this.loading = false,
      error: (err) => {
        this.loading = false;
        this.error = err;
      }
    });
  }
}
