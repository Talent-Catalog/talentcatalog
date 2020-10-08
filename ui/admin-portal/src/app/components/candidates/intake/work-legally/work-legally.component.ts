import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-work-legally',
  templateUrl: './work-legally.component.html',
  styleUrls: ['./work-legally.component.scss']
})
export class WorkLegallyComponent extends IntakeComponentBase implements OnInit {

  public workLegallyOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      workLegally: [this.candidateIntakeData?.workLegally],
    });
  }

}
