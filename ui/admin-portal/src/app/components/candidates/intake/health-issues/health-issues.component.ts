import {Component, OnInit} from '@angular/core';
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNo} from "../../../../model/candidate";

@Component({
  selector: 'app-health-issues',
  templateUrl: './health-issues.component.html',
  styleUrls: ['./health-issues.component.scss']
})
export class HealthIssuesComponent extends IntakeComponentBase implements OnInit {

  public healthIssuesOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      healthIssues: [{value: this.candidateIntakeData?.healthIssues, disabled: !this.editable}],
      healthIssuesNotes: [{value: this.candidateIntakeData?.healthIssuesNotes, disabled: !this.editable}],
    });
    this.updateDataOnFieldChange("healthIssues");
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.form.value.healthIssues) {
      if (this.form.value.healthIssues === 'Yes') {
        found = true
      }
      if (this.form.value.healthIssues === 'No') {
        found = true
      }
    }
    return found;
  }

}
