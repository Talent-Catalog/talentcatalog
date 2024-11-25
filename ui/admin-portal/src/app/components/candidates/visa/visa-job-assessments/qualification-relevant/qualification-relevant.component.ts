import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {YesNo} from '../../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {VisaCheckComponentBase} from "../../../../util/intake/VisaCheckComponentBase";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";

@Component({
  selector: 'app-qualification-relevant',
  templateUrl: './qualification-relevant.component.html',
  styleUrls: ['./qualification-relevant.component.scss']
})
export class QualificationRelevantComponent extends VisaCheckComponentBase implements OnInit {
  public relevantQualificationOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: UntypedFormBuilder, candidateVisaCheckService: CandidateVisaCheckService) {
    super(fb, candidateVisaCheckService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.visaJobCheck?.id],
      visaJobQualification: [this.visaJobCheck?.qualification],
      visaJobQualificationNotes: [this.visaJobCheck?.qualificationNotes],
    });
  }

}
