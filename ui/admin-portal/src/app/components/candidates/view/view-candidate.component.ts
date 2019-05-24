import { Component, OnInit } from '@angular/core';
import { CandidateService } from '../../../services/candidate.service';
import { Candidate } from '../../../model/candidate';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-view-candidate',
  templateUrl: './view-candidate.component.html',
  styleUrls: ['./view-candidate.component.scss']
})
export class ViewCandidateComponent implements OnInit {

  loading: boolean;
  candidate: Candidate;

  constructor(private candidateService: CandidateService,
              private route: ActivatedRoute) { }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      let candidateId = +params.get('candidateId');
      this.loading = true;
      this.candidateService.get(candidateId).subscribe(candidate => {
        this.candidate = candidate;
        this.loading = false;
      });
    });
  }
}
