import {Component, OnInit} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';
import {NgbDatepickerConfig, NgbDateStruct} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-host-entry-year',
  templateUrl: './host-entry-year.component.html',
  styleUrls: ['./host-entry-year.component.scss']
})
export class HostEntryYearComponent extends IntakeComponentBase implements OnInit {

  public maxDate: NgbDateStruct;
  public today: Date;

  constructor(fb: FormBuilder, candidateService: CandidateService, private datepickerConfig: NgbDatepickerConfig) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.today = new Date();
    this.maxDate = {year: this.today.getFullYear(), month: this.today.getMonth() + 1, day: this.today.getDate()};
    this.form = this.fb.group({
      hostEntryYear: [this.candidateIntakeData?.hostEntryYear],
    });
  }
}
