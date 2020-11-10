import {Component, OnInit} from '@angular/core';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNoUnsure} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';

@Component({
  selector: 'app-visa-reject',
  templateUrl: './visa-reject.component.html',
  styleUrls: ['./visa-reject.component.scss']
})
export class VisaRejectComponent extends IntakeComponentBase implements OnInit {

  public visaRejectOptions: EnumOption[] = enumOptions(YesNoUnsure);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaReject: [this.candidateIntakeData?.visaReject],
    });
  }

}
