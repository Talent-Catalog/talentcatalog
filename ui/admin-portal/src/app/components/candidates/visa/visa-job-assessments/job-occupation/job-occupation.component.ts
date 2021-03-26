import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';
import {Occupation} from '../../../../../model/occupation';
import {CandidateVisaJobCheck} from '../../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';

@Component({
  selector: 'app-job-occupation',
  templateUrl: './job-occupation.component.html',
  styleUrls: ['./job-occupation.component.scss']
})
export class JobOccupationComponent extends IntakeComponentBase implements OnInit, OnChanges {

  @Input() occupations: Occupation[];
  @Input() selectedIndex: number;
  @Input() selectedJobCheck: CandidateVisaJobCheck;

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    console.log(this.visaCheckRecord)
    this.form = this.fb.group({
      visaJobId: [this.selectedJobCheck?.id],
      visaJobOccupationId: [this.selectedJobCheck?.occupation?.id],
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.selectedJobCheck && changes.selectedJobCheck.previousValue !== changes.selectedJobCheck.currentValue) {
      // this.selectedJobCheck = changes.selectedJobCheck.currentValue;
      // console.log(this.selectedJobCheck);
      // todo don't want to change the form data, want to reload the data.
      //this.form?.controls?.visaJobOccupationId?.patchValue(this.selectedJobCheck?.occupation?.id);
    }
  }

  get occupationId(): number {
    return this.selectedJobCheck?.occupation?.id;
  }

}
