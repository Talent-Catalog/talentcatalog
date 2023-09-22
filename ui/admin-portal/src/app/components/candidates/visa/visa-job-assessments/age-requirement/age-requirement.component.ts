import {Component, OnInit} from '@angular/core';
import {YesNo} from "../../../../../model/candidate";
import {EnumOption, enumOptions} from "../../../../../util/enum";
import {FormBuilder} from "@angular/forms";
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-age-requirement',
  templateUrl: './age-requirement.component.html',
  styleUrls: ['./age-requirement.component.scss']
})
export class AgeRequirementComponent extends VisaCheckComponentBase implements OnInit {
  public ageRequirementOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobAgeRequirement: [this.visaJobCheck?.ageRequirement],
    });
  }
}
