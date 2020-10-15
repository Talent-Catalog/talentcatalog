import {Component, OnInit} from '@angular/core';
import {CandidateService} from "../../../../../services/candidate.service";
import {NationalityService} from "../../../../../services/nationality.service";
import {IntakeComponentTabBase} from "../../../../util/intake/IntakeComponentTabBase";

@Component({
  selector: 'app-candidate-visa-tab',
  templateUrl: './candidate-visa-tab.component.html',
  styleUrls: ['./candidate-visa-tab.component.scss']
})
export class CandidateVisaTabComponent
  extends IntakeComponentTabBase implements OnInit {

  selectedVisa: string;

  constructor(candidateService: CandidateService,
              nationalityService: NationalityService) {
    super(candidateService, nationalityService)
  }


  ngOnInit() {
    super.ngOnInit();

    //todo debug
    this.selectedVisa = "Australia";
  }
}
