import {Component, Input, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {CandidateVisaJobCheck, YesNo} from '../../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-qualification-relevant',
  templateUrl: './qualification-relevant.component.html',
  styleUrls: ['./qualification-relevant.component.scss']
})
export class QualificationRelevantComponent extends IntakeComponentBase implements OnInit {

  @Input() selectedJobCheck: CandidateVisaJobCheck;
  public relevantQualificationOptions: EnumOption[] = enumOptions(YesNo);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaJobId: [this.selectedJobCheck?.id],
      visaJobQualification: [this.selectedJobCheck?.qualification],
      visaJobQualificationNotes: [this.selectedJobCheck?.qualificationNotes],
    });
  }

}
