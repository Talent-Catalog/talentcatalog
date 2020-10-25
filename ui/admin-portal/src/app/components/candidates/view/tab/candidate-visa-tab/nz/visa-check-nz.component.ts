import {Component, Input} from '@angular/core';
import {IntakeComponentTabBase} from "../../../../../util/intake/IntakeComponentTabBase";
import {CandidateService} from "../../../../../../services/candidate.service";
import {NationalityService} from "../../../../../../services/nationality.service";

@Component({
  selector: 'app-visa-check-nz',
  templateUrl: './visa-check-nz.component.html',
  styleUrls: ['./visa-check-nz.component.scss']
})
export class VisaCheckNzComponent extends IntakeComponentTabBase {
  @Input() selectedIndex: number;
  constructor(candidateService: CandidateService,
              nationalityService: NationalityService) {
    super(candidateService, nationalityService)
  }
}
