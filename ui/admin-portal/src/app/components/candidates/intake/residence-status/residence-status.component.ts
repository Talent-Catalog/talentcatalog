import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {ResidenceStatus} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-residence-status',
  templateUrl: './residence-status.component.html',
  styleUrls: ['./residence-status.component.scss']
})
export class ResidenceStatusComponent extends IntakeComponentBase implements OnInit {

  public residenceStatusOptions: EnumOption[] = enumOptions(ResidenceStatus);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      residenceStatus: [this.candidateIntakeData?.residenceStatus],
    });
  }

}
