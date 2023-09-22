import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {YesNo} from '../../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-regional-area',
  templateUrl: './regional-area.component.html',
  styleUrls: ['./regional-area.component.scss']
})
export class RegionalAreaComponent extends VisaCheckComponentBase implements OnInit {
  public regionalAreaOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobRegional: [this.visaJobCheck?.regional],
    });
  }

}
