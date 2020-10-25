import {Component, Input} from '@angular/core';
import {IntakeComponentTabBase} from "../../../../../util/intake/IntakeComponentTabBase";
import {CandidateService} from "../../../../../../services/candidate.service";
import {NationalityService} from "../../../../../../services/nationality.service";

@Component({
  selector: 'app-visa-check-au',
  templateUrl: './visa-check-au.component.html',
  styleUrls: ['./visa-check-au.component.scss']
})
export class VisaCheckAuComponent extends IntakeComponentTabBase {
  @Input() selectedIndex: number;
  constructor(candidateService: CandidateService,
              nationalityService: NationalityService) {
    super(candidateService, nationalityService)
  }
}
