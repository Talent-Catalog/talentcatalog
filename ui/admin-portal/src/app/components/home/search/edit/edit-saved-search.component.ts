//todo Is this still used anywhere?

import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {
  convertToSavedSearchRequest,
  SavedSearch
} from '../../../../model/saved-search';
import {SavedSearchService} from '../../../../services/saved-search.service';

@Component({
  selector: 'app-edit-saved-search',
  templateUrl: './edit-saved-search.component.html',
  styleUrls: ['./edit-saved-search.component.scss']
})
export class EditSavedSearchComponent implements OnInit {

  savedSearchId: number;
  savedSearchForm: FormGroup;
  error;
  loading: boolean;
  saving: boolean;
  savedSearch: SavedSearch;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private savedSearchService: SavedSearchService) {
  }

  ngOnInit() {
    this.loading = true;

    //Retrieve full SavedSearch so that we can display details in dialog
    this.savedSearchService.get(this.savedSearchId).subscribe(savedSearch => {
      this.savedSearch = savedSearch;
      this.savedSearchForm = this.fb.group({
        name: [savedSearch.name, Validators.required],
        type: [savedSearch.savedSearchType, Validators.required]
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;

    //Populate name and type
    this.savedSearch.name = this.savedSearchForm.value.name;
    this.savedSearch.savedSearchType = this.savedSearchForm.value.type;
    //Create a SavedSearchRequest from the SavedSearch and the search request
    this.savedSearchService.update(
      convertToSavedSearchRequest(this.savedSearch, null)
    ).subscribe(
      (savedSearch) => {
        this.closeModal(savedSearch);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(savedSearch: SavedSearch) {
    this.activeModal.close(savedSearch);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
