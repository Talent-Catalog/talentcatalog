import {Component, Input, OnInit} from '@angular/core';
import {Candidate, CandidateExam, CandidateIntakeData} from '../../../../model/candidate';
import {CandidateExamService} from '../../../../services/candidate-exam.service';

@Component({
  selector: 'app-exams',
  templateUrl: './exams.component.html',
  styleUrls: ['./exams.component.scss']
})
export class ExamsComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  error: boolean;
  saving: boolean;

  constructor(
    private candidateExamService: CandidateExamService
  ) {}

  ngOnInit(): void {
  }

  addRecord() {
    this.saving = true;
    const candidateExam: CandidateExam = {};
    this.candidateExamService.create(this.candidate.id, candidateExam).subscribe(
      (exam) => {
        this.candidateIntakeData.candidateExams.push(exam)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  deleteRecord(i: number) {
    this.candidateIntakeData.candidateExams.splice(i, 1);
  }

}
