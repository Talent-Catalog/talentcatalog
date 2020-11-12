import {Component, OnInit} from '@angular/core';
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {CandidateVisaCheck} from "../../../../model/candidate";

@Component({
  selector: 'app-visa-eligibility-check',
  templateUrl: './visa-eligibility-check.component.html',
  styleUrls: ['./visa-eligibility-check.component.scss']
})
export class VisaEligibilityCheckComponent extends IntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaId: [this.myRecord?.id],
      visaCountryId: [this.myRecord?.country?.id],
      visaCheckedById: [this.myRecord?.checkedBy?.id],
      visaCheckedDate: [this.myRecord?.checkedDate],
    });
  }

  private get myRecord(): CandidateVisaCheck {
    return this.candidateIntakeData.candidateVisaChecks ?
      this.candidateIntakeData.candidateVisaChecks[this.myRecordIndex]
      : null;
  }

  dateSelected($event) {
    this.form.controls['visaCheckedDate'].patchValue($event);
  }

  userSelected($event) {
    this.form.controls['visaCheckedById'].patchValue($event.id);
  }

}
