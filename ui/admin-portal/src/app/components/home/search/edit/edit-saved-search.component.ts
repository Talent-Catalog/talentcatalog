import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {SavedSearch} from '../../../../model/saved-search';
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
  savedSearch;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private savedSearchService: SavedSearchService) {
  }

  ngOnInit() {
    this.loading = true;
    this.savedSearchService.get(this.savedSearchId).subscribe(savedSearch => {
      this.savedSearch = savedSearch;
      this.savedSearchForm = this.fb.group({
        name: [savedSearch.name, Validators.required],
        type: [savedSearch.type, Validators.required]
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.savedSearchService.update(this.savedSearchId, this.savedSearchForm.value).subscribe(
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
