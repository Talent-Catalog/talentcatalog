import {Component, Input} from '@angular/core';
import {PresetEmbedService} from "../../../services/preset-embed.service";

@Component({
  selector: 'app-preset-embed',
  templateUrl: './preset-embed.component.html',
  styleUrls: ['./preset-embed.component.scss']
})
export class PresetEmbedComponent {
  @Input() dashboardId: string;

  error: any;
  loading = false;

  constructor(private presetEmbedService: PresetEmbedService) {}

  ngOnInit(): void {
    if (!this.dashboardId) {
      this.error = "Dashboard ID is required";
      return;
    }

    this.loading = true;
    this.presetEmbedService.embedDashboard(
      this.dashboardId,
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
