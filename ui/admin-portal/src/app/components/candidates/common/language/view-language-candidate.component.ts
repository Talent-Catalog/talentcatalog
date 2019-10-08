import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from "../../../../model/candidate";
import {CandidateLanguage} from "../../../../model/candidate-language";
import {CandidateLanguageService} from "../../../../services/candidate-language.service";

@Component({
  selector: 'app-view-language-candidate',
  templateUrl: './view-language-candidate.component.html',
  styleUrls: ['./view-language-candidate.component.scss']
})
export class ViewLanguageCandidateComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;

  candidateLanguages: CandidateLanguage[];
  loading: boolean;
  error;

  constructor(private candidateLanguageService: CandidateLanguageService) {
  }

  ngOnInit() {

  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.loading = true;
      this.candidateLanguageService.list(this.candidate.id).subscribe(
        candidateLanguages => {
          this.candidateLanguages = candidateLanguages;
          this.loading = false;
        },
        error => {
          this.error = error;
          this.loading = false;
        })
      ;
    }
  }


}
