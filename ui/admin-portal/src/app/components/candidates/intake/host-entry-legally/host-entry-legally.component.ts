import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-host-entry-legally',
  templateUrl: './host-entry-legally.component.html',
  styleUrls: ['./host-entry-legally.component.scss']
})
export class HostEntryLegallyComponent extends IntakeComponentBase implements OnInit {

  public hostEntryLegallyOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      hostEntryLegally: [this.candidateIntakeData?.hostEntryLegally],
    });
  }
  get workAbroad(): string {
    return this.form.value?.workAbroad;
  }
}
