import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {SavedSearchService} from "../../../../services/saved-search.service";
import {SavedSearch} from "../../../../model/saved-search";
import {Candidate} from "../../../../model/candidate";
import {SearchCandidateRequest} from "../../../../model/search-candidate-request";

@Component({
  selector: 'app-update-search',
  templateUrl: './update-search.component.html',
  styleUrls: ['./update-search.component.scss']
})

export class UpdateSearchComponent implements OnInit {

  error;
  savedSearch: SavedSearch;
  searchCandidateRequest: SearchCandidateRequest;
  updating: boolean;

  constructor(private activeModal: NgbActiveModal,
              private savedSearchService: SavedSearchService) {
  }

  ngOnInit() {
  }

  cancel() {
    this.activeModal.dismiss();
  }

  confirm() {
    this.updating = true;

    this.savedSearch.searchCandidateRequest = this.searchCandidateRequest;
    this.savedSearchService.update(this.savedSearch).subscribe(
      (savedSearch) => {
        this.updating = false;
        this.activeModal.close();
      },
      (error) => {
        this.error = error;
        this.updating = false;
      });
  }
}
