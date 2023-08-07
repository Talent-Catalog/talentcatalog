import {Component, Input, OnInit} from '@angular/core';
import {CandidateVisaJobCheck, YesNo} from "../../../../../model/candidate";
import {EnumOption, enumOptions} from "../../../../../util/enum";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../../services/candidate.service";
import {IntakeComponentBase} from "../../../../util/intake/IntakeComponentBase";

@Component({
  selector: 'app-age-requirement',
  templateUrl: './age-requirement.component.html',
  styleUrls: ['./age-requirement.component.scss']
})
export class AgeRequirementComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedJobCheck: CandidateVisaJobCheck;
  public ageRequirementOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.selectedJobCheck?.id],
      visaJobAgeRequirement: [this.selectedJobCheck?.ageRequirement],
    });
  }
}
