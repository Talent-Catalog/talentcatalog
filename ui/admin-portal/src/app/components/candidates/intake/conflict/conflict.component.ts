import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-conflict',
  templateUrl: './conflict.component.html',
  styleUrls: ['./conflict.component.scss']
})
export class ConflictComponent extends IntakeComponentBase implements OnInit {

  public conflictOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      conflict: [this.candidateIntakeData?.conflict],
      conflictNotes: [this.candidateIntakeData?.conflictNotes],
    });
  }

  get conflict(): string {
    return this.form.value?.conflict;
  }

}
