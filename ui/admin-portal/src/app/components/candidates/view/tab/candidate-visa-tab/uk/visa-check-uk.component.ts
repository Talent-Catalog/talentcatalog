import {Component, Input} from '@angular/core';
import {IntakeComponentTabBase} from "../../../../../util/intake/IntakeComponentTabBase";
import {CandidateService} from "../../../../../../services/candidate.service";
import {NationalityService} from "../../../../../../services/nationality.service";

@Component({
  selector: 'app-visa-check-uk',
  templateUrl: './visa-check-uk.component.html',
  styleUrls: ['./visa-check-uk.component.scss']
})
export class VisaCheckUkComponent extends IntakeComponentTabBase {
  @Input() selectedIndex: number;
  constructor(candidateService: CandidateService,
              nationalityService: NationalityService) {
    super(candidateService, nationalityService)
  }
}

