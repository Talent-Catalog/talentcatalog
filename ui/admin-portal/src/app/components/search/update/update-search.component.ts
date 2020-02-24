import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {
  SavedSearchService,
  SavedSearchTypeInfo,
  SavedSearchTypeSubInfo
} from "../../../services/saved-search.service";
import {
  convertToSavedSearchRequest,
  SavedSearch
} from "../../../model/saved-search";
import {SearchCandidateRequest} from "../../../model/search-candidate-request";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-update-search',
  templateUrl: './update-search.component.html',
  styleUrls: ['./update-search.component.scss']
})

export class UpdateSearchComponent implements OnInit {

  error;
  savedSearch: SavedSearch;
  savedSearchTypeInfos: SavedSearchTypeInfo[];
  savedSearchTypeSubInfos: SavedSearchTypeSubInfo[];
  searchCandidateRequest: SearchCandidateRequest;
  updating: boolean;
  form: FormGroup;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private savedSearchService: SavedSearchService) {
    this.savedSearchTypeInfos = savedSearchService.getSavedSearchTypeInfos();
  }

  ngOnInit() {
    this.form = this.fb.group({
      name: [this.savedSearch.name, Validators.required],
      savedSearchType: [this.savedSearch.savedSearchType],
      savedSearchSubtype: [this.savedSearch.savedSearchSubtype],
    });
    //Copy the form values in so that they can be displayed in any summary
    //(Otherwise we just see the unmodified search values)
    this.savedSearch = Object.assign(this.savedSearch, this.searchCandidateRequest);

    this.onSavedSearchTypeChange();
  }

  cancel() {
    this.activeModal.dismiss();
  }

  confirm() {
    this.updating = true;

    const formValues = this.form.value;
    this.savedSearch.name = formValues.name;

    //Use parseInt rather than +, because + returns zero when null or undefined.
    this.savedSearch.savedSearchType = parseInt(formValues.savedSearchType);
    this.savedSearch.savedSearchSubtype = parseInt(formValues.savedSearchSubtype);

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

  onSavedSearchTypeChange() {
    const formValues = this.form.value;
    const selectedSavedSearchType = formValues.savedSearchType;
    if (selectedSavedSearchType === undefined) {
      this.savedSearchTypeSubInfos = null;
    } else {
      this.savedSearchTypeSubInfos =
        this.savedSearchTypeInfos[selectedSavedSearchType].categories;
      if (!this.savedSearchTypeSubInfos) {
        this.form.controls["savedSearchSubtype"].patchValue(null);
      }
    }
  }
}
