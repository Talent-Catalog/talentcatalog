import {Component, OnInit} from '@angular/core';
import {CandidateService} from "../../services/candidate.service";
import {Candidate, CandidateStatus} from "../../model/candidate";
import {User} from "../../model/user";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  loading: boolean;
  error: any;

  candidate: Candidate;
  user: User;

  constructor(private candidateService: CandidateService) {
  }

  ngOnInit() {
    this.candidateService.getStatus().subscribe(
      (candidate) => {
        this.candidate = candidate || ({status: CandidateStatus.draft} as Candidate);
        this.user = this.candidate.user;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }
}

