import { Component, ChangeDetectionStrategy } from '@angular/core';
import {EnvService} from "../../services/env.service";

@Component({
  selector: 'app-intelligence',
  templateUrl: './intelligence.component.html',
  styleUrls: ['./intelligence.component.scss'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class IntelligenceComponent {

  readonly allCandidatesDashboardId: string;

  constructor(private envService: EnvService) {
    this.allCandidatesDashboardId = this.envService.allCandidatesDashboardId;
  }

}
