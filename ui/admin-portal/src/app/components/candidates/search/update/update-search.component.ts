import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {SavedSearchService} from "../../../../services/saved-search.service";
import {
  convertToSavedSearchRequest,
  SavedSearch
} from "../../../../model/saved-search";
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
    //Copy the form values in so that they can be displayed in any summary
    //(Otherwise we just see the unmodified search values)
    this.savedSearch = Object.assign(this.savedSearch, this.searchCandidateRequest);
  }

  cancel() {
    this.activeModal.dismiss();
  }

  confirm() {
    this.updating = true;

    //Create a SavedSearchRequest from the SavedSearch and the search request
    this.savedSearchService.update(
      convertToSavedSearchRequest(this.savedSearch, this.searchCandidateRequest)
    ).subscribe(
      () => {
        this.updating = false;
        this.activeModal.close();
      },
      (error) => {
        this.error = error;
        this.updating = false;
      });
  }
}
