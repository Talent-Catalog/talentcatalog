import {Component, OnInit} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';
import {NgbDateStruct} from '@ng-bootstrap/ng-bootstrap';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {generateYearArray} from '../../../../util/year-helper';

@Component({
  selector: 'app-host-entry-year',
  templateUrl: './host-entry-year.component.html',
  styleUrls: ['./host-entry-year.component.scss']
})
export class HostEntryYearComponent extends IntakeComponentBase implements OnInit {

  public hostBornOptions: EnumOption[] = enumOptions(YesNo);

  public maxDate: NgbDateStruct;
  public today: Date;
  years: number[];

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  // Year is converted to string using the ngb-date-adapter file in the util folder (see app module providers)
  ngOnInit(): void {
    this.today = new Date();
    this.maxDate = {year: this.today.getFullYear(), month: this.today.getMonth() + 1, day: this.today.getDate()};
    this.years = generateYearArray(1950, true);
    this.form = this.fb.group({
      hostBorn: [this.candidateIntakeData?.hostBorn],
      hostEntryYear: [this.candidateIntakeData?.hostEntryYear],
    });
  }

  get hostBorn(): string {
    return this.form.value?.hostBorn;
  }
}
