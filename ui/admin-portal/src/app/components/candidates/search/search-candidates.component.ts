import { Component, OnInit } from '@angular/core';
import { CandidateService } from '../../../services/candidate.service';
import { Candidate } from '../../../model/candidate';

@Component({
  selector: 'app-search-candidates',
  templateUrl: './search-candidates.component.html',
  styleUrls: ['./search-candidates.component.scss']
})
export class SearchCandidatesComponent implements OnInit {

  loading: boolean;
  candidates: Candidate[];

  constructor(private candidateService: CandidateService) { }

  ngOnInit() {
    this.loading = true;
    this.candidateService.search().subscribe(results => {
      this.candidates = results;
      this.loading = false;
    });
  }

}
