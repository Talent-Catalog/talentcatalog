import {Component, OnInit} from '@angular/core';
import {Candidate} from "../../../model/candidate";
import {CandidateService} from "../../../services/candidate.service";
import {US_AFGHAN_SURVEY_TYPE} from "../../../model/survey-type";

@Component({
  selector: 'app-view-candidate',
  templateUrl: './view-candidate.component.html',
  styleUrls: ['./view-candidate.component.scss']
})
export class ViewCandidateComponent implements OnInit {

  error;
  loading;
  activeTabId: string;
  candidate: Candidate;
  usAfghan: boolean;

  constructor(private candidateService: CandidateService) { }

  ngOnInit(): void {
    this.candidateService.getProfile().subscribe(
      (response) => {
        this.candidate = response;
        this.usAfghan = response.surveyType?.id === US_AFGHAN_SURVEY_TYPE;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

}
