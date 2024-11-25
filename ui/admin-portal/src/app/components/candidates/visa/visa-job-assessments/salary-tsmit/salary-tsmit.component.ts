import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {YesNo} from '../../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-salary-tsmit',
  templateUrl: './salary-tsmit.component.html',
  styleUrls: ['./salary-tsmit.component.scss']
})
export class SalaryTsmitComponent extends VisaCheckComponentBase implements OnInit {
  public salaryRequirementOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobSalaryTsmit: [this.visaJobCheck?.salaryTsmit],
    });
  }

}
