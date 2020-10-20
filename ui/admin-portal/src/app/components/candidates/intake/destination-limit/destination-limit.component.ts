import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-destination-limit',
  templateUrl: './destination-limit.component.html',
  styleUrls: ['./destination-limit.component.scss']
})
export class DestinationLimitComponent extends IntakeComponentBase implements OnInit {

  public destLimitOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      destLimit: [this.candidateIntakeData?.destLimit],
      destLimitNotes: [this.candidateIntakeData?.destLimitNotes],
    });
  }

  get destLimit(): string {
    return this.form.value?.destLimit;
  }

}
