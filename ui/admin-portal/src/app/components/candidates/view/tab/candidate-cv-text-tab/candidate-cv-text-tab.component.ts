import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../../../../model/candidate";
import {CandidateAttachmentService} from "../../../../../services/candidate-attachment.service";

@Component({
  selector: 'app-candidate-cv-text-tab',
  templateUrl: './candidate-cv-text-tab.component.html',
  styleUrls: ['./candidate-cv-text-tab.component.scss']
})
export class CandidateCvTextTabComponent implements OnInit {
  @Input() candidate: Candidate;
  cvText: string;

  constructor(private candidateAttachmentService: CandidateAttachmentService) {
  }

  ngOnInit(): void {
    this.candidateAttachmentService.getCandidateCvText(this.candidate).subscribe(
      {
        next: (cvText: string) => {this.cvText = cvText;},
        error: (error) => {},
      }
    )
  }

}
