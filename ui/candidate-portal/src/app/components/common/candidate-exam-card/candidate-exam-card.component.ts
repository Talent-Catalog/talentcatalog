import {
  Component,
  EventEmitter,
  Input,
  Output,
  OnChanges,
  SimpleChanges
} from '@angular/core';
import {CandidateExam} from '../../../model/candidate';
import {Exam} from "../../../model/candidate";

@Component({
  selector: 'app-candidate-exam-card',
  templateUrl: './candidate-exam-card.component.html',
  styleUrls: ['./candidate-exam-card.component.scss']
})
export class CandidateExamCardComponent implements OnChanges {

  @Input() candidateExam: CandidateExam;
  @Input() candidateExams: CandidateExam[];
  @Input() disabled: boolean = false;
  @Output() onDelete = new EventEmitter<number>();

  constructor() {}

  ngOnChanges(changes: SimpleChanges) {

  }
  delete() {
    this.onDelete.emit();
  }
  getExamName(exam: string): string {
    return Exam[exam as keyof typeof Exam] || 'Unknown';
  }
}
