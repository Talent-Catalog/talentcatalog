import { Component, OnInit } from '@angular/core';
import { CandidateService } from '../../../services/candidate.service';
import { Candidate } from '../../../model/candidate';
import { SearchResults } from '../../../model/search-results';
import { FormBuilder, FormGroup } from '@angular/forms';

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

  searchForm: FormGroup;

  constructor(private candidateService: CandidateService,
              private fb: FormBuilder) { }

  ngOnInit() {
    this.pageNumber = 1;
    this.pageSize = 5;

    this.searchForm = this.fb.group({
      phrase: ['']
    });

    this.search();
  }

  search() {
    this.loading = true;

    let request = {
      pageNumber: this.pageNumber - 1,
      pageSize: this.pageSize
    };

    Object.assign(request, this.searchForm.value);

    this.candidateService.search(request).subscribe(results => {
      this.results = results;
      this.loading = false;
    });
  }

}
