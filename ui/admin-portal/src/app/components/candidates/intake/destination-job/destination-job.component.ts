import {Component, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-destination-job',
  templateUrl: './destination-job.component.html',
  styleUrls: ['./destination-job.component.scss']
})
export class DestinationJobComponent extends IntakeComponentBase implements OnInit {

  public destJobOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      destJob: [this.candidateIntakeData?.destJob],
      destJobNotes: [this.candidateIntakeData?.destJobNotes],
    });
  }

  get destJob(): string {
    return this.form.value?.destJob;
  }
}
