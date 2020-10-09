import {Component, OnInit} from '@angular/core';
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {
  enumKeysToEnumOptions,
  enumMultiSelectSettings,
  EnumOption,
  enumOptions
} from "../../../../util/enum";
import {VisaIssue} from "../../../../model/candidate";
import {IDropdownSettings} from "ng-multiselect-dropdown";

@Component({
  selector: 'app-visa-issues',
  templateUrl: './visa-issues.component.html',
  styleUrls: ['./visa-issues.component.scss']
})
export class VisaIssuesComponent extends IntakeComponentBase implements OnInit {

  public dropdownSettings: IDropdownSettings = enumMultiSelectSettings;
  public visaIssueOptions: EnumOption[] = enumOptions(VisaIssue);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService)
  }

  ngOnInit(): void {
    const options: EnumOption[] =
      enumKeysToEnumOptions(this.candidateIntakeData?.visaIssues, VisaIssue);
    this.form = this.fb.group({
      visaIssues: [options],
      visaIssuesNotes: [this.candidateIntakeData?.visaIssuesNotes],
    });
  }

  get haveIssues(): boolean {
    return this.form.value.visaIssues?.length > 0;
  }
}
