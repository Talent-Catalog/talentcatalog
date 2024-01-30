import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNo} from "../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-monitoring-evaluation-consent',
  templateUrl: './monitoring-evaluation-consent.component.html',
  styleUrls: ['./monitoring-evaluation-consent.component.scss']
})
export class MonitoringEvaluationConsentComponent extends IntakeComponentBase implements OnInit {

  public consentOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      monitoringEvaluationConsent: [{value: this.candidateIntakeData?.monitoringEvaluationConsent, disabled: !this.editable}],
    });
  }

}
