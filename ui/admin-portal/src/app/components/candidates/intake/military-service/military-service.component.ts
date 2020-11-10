import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-military-service',
  templateUrl: './military-service.component.html',
  styleUrls: ['./military-service.component.scss']
})
export class MilitaryServiceComponent extends IntakeComponentBase implements OnInit {

  public militaryServiceOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      militaryService: [this.candidateIntakeData?.militaryService],
    });
  }
}
