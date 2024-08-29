import {
  Component,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';
import {CandidateExam} from '../../../model/candidate';
import {Exam} from "../../../model/candidate";
import {generateYearArray} from "../../../util/year-helper";

@Component({
  selector: 'app-candidate-exam-card',
  templateUrl: './candidate-exam-card.component.html',
  styleUrls: ['./candidate-exam-card.component.scss']
})
export class CandidateExamCardComponent {

  @Input() exam: CandidateExam;
  @Input() preview: boolean = false;
  @Input() disabled: boolean = false;
  @Output() onDelete = new EventEmitter<CandidateExam>();
  @Output() onEdit = new EventEmitter<CandidateExam>();

  years:number[];

  constructor() {
    this.years=generateYearArray(1950,true);
  }

  deleteExam() {
    this.onDelete.emit(this.exam);
  }

  editExam() {
    this.onEdit.emit(this.exam);
  }
  getExamName(exam: string): string {
    return Exam[exam as keyof typeof Exam] || 'Unknown';
  }
}
