import {Component, OnInit} from '@angular/core';
import {NgbDateStruct} from '@ng-bootstrap/ng-bootstrap';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-asylum-year',
  templateUrl: './asylum-year.component.html',
  styleUrls: ['./asylum-year.component.scss']
})
export class AsylumYearComponent extends IntakeComponentBase implements OnInit {

  public maxDate: NgbDateStruct;
  public today: Date;

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  // Year is converted to string using the ngb-date-adapter file in the util folder (see app module providers)
  ngOnInit(): void {
    this.today = new Date();
    this.maxDate = {year: this.today.getFullYear(), month: this.today.getMonth() + 1, day: this.today.getDate()};
    this.form = this.fb.group({
      asylumYear: [this.candidateIntakeData?.asylumYear],
    });
  }

}
