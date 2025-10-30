import {Component, Input, OnInit} from '@angular/core';
import {Candidate} from "../../../../../model/candidate";
import {CandidateAttachmentService} from "../../../../../services/candidate-attachment.service";
import {CvText} from "../../../../../model/cv-text";

@Component({
  selector: 'app-candidate-cv-text-tab',
  templateUrl: './candidate-cv-text-tab.component.html',
  styleUrls: ['./candidate-cv-text-tab.component.scss']
})
export class CandidateCvTextTabComponent implements OnInit {
  @Input() candidate: Candidate;
  cvText: string;
  error: string;

  constructor(private candidateAttachmentService: CandidateAttachmentService) {
  }

  ngOnInit(): void {
    this.error = null;
    this.candidateAttachmentService.getCandidateCvText(this.candidate?.id).subscribe(
      {
        next: (cvsText: CvText[]) => {this.cvText = this.concatenateCvText(cvsText);},
        error: (error) => {this.error = error;},
      }
    )
  }

  private concatenateCvText(cvsText: CvText[]): string {
    let s = "";
    if (cvsText && cvsText.length > 0) {
      s = cvsText.map((cvText) => cvText.text).join('||\n');
    }
    return s;
  }
}
