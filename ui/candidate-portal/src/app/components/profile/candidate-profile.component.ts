import {Component, OnInit} from '@angular/core';
import {CandidateService} from "../../services/candidate.service";
import {Candidate} from "../../model/candidate";

@Component({
  selector: 'app-candidate-profile',
  templateUrl: './candidate-profile.component.html',
  styleUrls: ['./candidate-profile.component.scss']
})
export class CandidateProfileComponent implements OnInit {

  error: any;
  loading: boolean;

  candidate: Candidate;

  constructor(private candidateService: CandidateService) { }

  ngOnInit() {
    this.loading = true;
    this.candidateService.getProfile().subscribe(
      (response) => {
        this.candidate = response;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

}
