import {Component, Input, OnInit, SimpleChanges} from '@angular/core';
import {Candidate, CandidateExam} from "../../../../model/candidate";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateExamService} from "../../../../services/candidate-exam.service";

@Component({
  selector: 'app-view-candidate-exam',
  templateUrl: './view-candidate-exam.component.html',
  styleUrls: ['./view-candidate-exam.component.scss']
})
export class ViewCandidateExamComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() editable: boolean;
  @Input() adminUser: boolean;

  candidateExams: CandidateExam[];
  candidateExam: CandidateExam;
  loading: boolean;
  error;

  constructor(private candidateExamService: CandidateExamService,
              private modalService: NgbModal) {
  }

  ngOnInit() {

  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.doSearch();
    }
  }

  doSearch() {
    this.loading = true;
    this.candidateExamService.list(this.candidate.id).subscribe(
      candidateExams => {
        this.candidateExams = candidateExams;
        console.log(candidateExams)
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      })
    ;
  }

}
