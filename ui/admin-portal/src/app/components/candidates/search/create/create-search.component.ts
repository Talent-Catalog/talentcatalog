import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {SavedSearchService} from "../../../../services/saved-search.service";
import {SavedSearch} from "../../../../model/saved-search";

@Component({
  selector: 'app-create-search',
  templateUrl: './create-search.component.html',
  styleUrls: ['./create-search.component.scss']
})

export class CreateSearchComponent implements OnInit {

  form: FormGroup;
  error;
  public replacing: boolean;
  saving: boolean;
  savedSearch;
  searchCandidateRequest;
  update;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private savedSearchService: SavedSearchService) {
  }

  ngOnInit() {
    this.replacing = false;
    this.form = this.fb.group({
      name: [null, Validators.required],
      type: [null, Validators.required],
      update: [!this.replacing, Validators.required],
      searchCandidateRequest: [this.searchCandidateRequest]
    });
    if (this.savedSearch) {
      this.savedSearch.searchCandidateRequest = this.searchCandidateRequest;
    }
  }


  save() {
    this.saving = true;
    const request = this.form.value;

    this.savedSearchService.create(request).subscribe(
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
