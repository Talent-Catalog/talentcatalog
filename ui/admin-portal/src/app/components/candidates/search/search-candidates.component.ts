import { Component, OnInit } from '@angular/core';
import { CandidateService } from '../../../services/candidate.service';
import { Candidate } from '../../../model/candidate';
import { SearchResults } from '../../../model/search-results';

@Component({
  selector: 'app-search-candidates',
  templateUrl: './search-candidates.component.html',
  styleUrls: ['./search-candidates.component.scss']
})
export class SearchCandidatesComponent implements OnInit {

  loading: boolean;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<Candidate>;

  constructor(private candidateService: CandidateService) { }

  ngOnInit() {
    this.pageNumber = 1;
    this.pageSize = 5;
    this.search();
  }

  search() {
    this.loading = true;
    let request = {
      pageNumber: this.pageNumber - 1,
      pageSize: this.pageSize
    };
    this.candidateService.search(request).subscribe(results => {
      this.results = results;
      this.loading = false;
    });
  }

}
