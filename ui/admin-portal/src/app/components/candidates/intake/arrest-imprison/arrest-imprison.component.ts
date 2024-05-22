import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNoUnsure} from "../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-arrest-imprison',
  templateUrl: './arrest-imprison.component.html',
  styleUrls: ['./arrest-imprison.component.scss']
})
export class ArrestImprisonComponent extends IntakeComponentBase implements OnInit {

  public arrestImprisonOptions: EnumOption[] = enumOptions(YesNoUnsure);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      arrestImprison: [{value: this.candidateIntakeData?.arrestImprison, disabled: !this.editable}],
      arrestImprisonNotes: [{value: this.candidateIntakeData?.arrestImprisonNotes, disabled: !this.editable}]
    });
    this.updateDataOnFieldChange("arrestImprison");
  }

  get arrestImprison(): string {
    return this.form.value?.arrestImprison;
  }

  get hasNotes(): boolean {
    let found: boolean = false;
    if (this.arrestImprison) {
      if (this.arrestImprison === 'Yes') {
        found = true
      }
      if (this.arrestImprison === 'Unsure') {
        found = true
      }
    }
    return found;
  }

}
