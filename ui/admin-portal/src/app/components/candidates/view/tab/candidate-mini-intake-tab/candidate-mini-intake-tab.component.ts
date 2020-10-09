import {Component, OnInit} from '@angular/core';
import {CandidateService} from '../../../../../services/candidate.service';
import {IntakeComponentTabBase} from "../../../../util/intake/IntakeComponentTabBase";
import {NationalityService} from "../../../../../services/nationality.service";

@Component({
  selector: 'app-candidate-mini-intake-tab',
  templateUrl: './candidate-mini-intake-tab.component.html',
  styleUrls: ['./candidate-mini-intake-tab.component.scss']
})
export class CandidateMiniIntakeTabComponent
  extends IntakeComponentTabBase implements OnInit {

  constructor(candidateService: CandidateService,
              nationalityService: NationalityService) {
    super(candidateService, nationalityService)
  }

}
