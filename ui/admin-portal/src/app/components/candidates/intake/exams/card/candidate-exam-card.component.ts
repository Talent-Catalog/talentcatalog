import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {CandidateExam, Exam} from '../../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {CandidateExamService} from '../../../../../services/candidate-exam.service';
import {generateYearArray} from '../../../../../util/year-helper';

@Component({
  selector: 'app-candidate-exam-card',
  templateUrl: './candidate-exam-card.component.html',
  styleUrls: ['./candidate-exam-card.component.scss']
})
export class CandidateExamCardComponent extends IntakeComponentBase implements OnInit {

  @Output() delete = new EventEmitter();

  //Drop down values for enumeration
  examOptions: EnumOption[] = enumOptions(Exam);
  years: number[];

  constructor(fb: FormBuilder, candidateService: CandidateService,
              private candidateExamService: CandidateExamService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      examId: [this.myRecord?.id],
      examType: [this.myRecord?.exam],
      otherExam: [this.myRecord?.otherExam],
      examScore: [this.myRecord?.score],
      examYear: [this.myRecord?.year],
    });

    this.years = generateYearArray(1950, true);

  }

  get isOtherExam(): boolean {
    let other: boolean = false;
    if (this.form?.value) {
      if (this.form.value.examType === 'Other') {
        other = true;
      }
    }
    return other;
  }

  get hasSelectedExam(): boolean {
    let found: boolean = false;
    if (this.form?.value) {
      if (this.form.value.examType !== null) {
        found = true;
      } else if (this.form.value.examType === '') {
        found = true
      }
    }
    return found;
  }


  private get myRecord(): CandidateExam {
    return this.candidateIntakeData.candidateExams ?
      this.candidateIntakeData.candidateExams[this.myRecordIndex]
      : null;
  }

  doDelete() {
    this.candidateExamService.delete(this.myRecord.id)
      .subscribe(
        ret => {
        },
        error => {
          this.error = error;
        }
      );
    this.delete.emit();
  }

}
