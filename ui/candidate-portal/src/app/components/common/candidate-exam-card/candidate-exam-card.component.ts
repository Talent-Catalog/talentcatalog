import {
  Component,
  EventEmitter,
  Input,
  Output,
  OnChanges,
  SimpleChanges, OnInit
} from '@angular/core';
import {CandidateExam} from '../../../model/candidate';
import {Exam} from "../../../model/candidate";
import {generateYearArray} from "../../../util/year-helper";

@Component({
  selector: 'app-candidate-exam-card',
  templateUrl: './candidate-exam-card.component.html',
  styleUrls: ['./candidate-exam-card.component.scss']
})
export class CandidateExamCardComponent implements OnChanges,OnInit {

  @Input() candidateExam: CandidateExam;
  @Input() candidateExams: CandidateExam[];
  @Input() disabled: boolean = false;
  @Output() onDelete = new EventEmitter<number>();
  years:number[];
  constructor() {}

  ngOnInit() {
    this.years=generateYearArray(1950,true);
  }

  ngOnChanges(changes: SimpleChanges) {

  }

  delete() {
    this.onDelete.emit();
  }
  getExamName(exam: string): string {
    return Exam[exam as keyof typeof Exam] || 'Unknown';
  }
}
