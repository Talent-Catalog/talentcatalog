import {Component, OnInit} from '@angular/core';
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNo} from "../../../../model/candidate";

@Component({
  selector: 'app-professional-license',
  templateUrl: './professional-license.component.html',
  styleUrls: ['./professional-license.component.scss']
})
export class ProfessionalLicenseComponent extends IntakeComponentBase implements OnInit {

  public professionalLicenseOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService)
  }

  ngOnInit(): void {

    this.form = this.fb.group({
      professionalLicense: [this.candidateIntakeData?.professionalLicense],
      professionalLicenseNotes: [this.candidateIntakeData?.professionalLicenseNotes],
    });
  }

  get professionalLicense(): string {
    return this.form.value?.professionalLicense;
  }

}
