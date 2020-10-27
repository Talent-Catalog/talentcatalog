import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {CandidateExam, Exam} from '../../../../../model/candidate';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {CandidateExamService} from '../../../../../services/candidate-exam.service';

@Component({
  selector: 'app-candidate-exam-card',
  templateUrl: './candidate-exam-card.component.html',
  styleUrls: ['./candidate-exam-card.component.scss']
})
export class CandidateExamCardComponent extends IntakeComponentBase implements OnInit {

  @Output() delete = new EventEmitter();

  //Drop down values for enumeration
  examOptions: EnumOption[] = enumOptions(Exam);

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
    });

    //Subscribe to changes on the nationality id so that we can keep local
    //intake data up to date - used to filter ids on new records so that we
    //don't get duplicates.
    //Even though the change has been saved on the server and is reflected
    //on the html form, it is not stored in the local copy of the candidate
    //intake data. We could refresh the whole page which will reload all
    //candidate intake data with the saved values - but more efficient just
    //to update it here.
    // this.form.controls['citizenNationalityId']?.valueChanges.subscribe(
    //   change => {
    //     //Update my existingRecord
    //     this.myRecord.exam = {id: +change};
    //   }
    // );
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
