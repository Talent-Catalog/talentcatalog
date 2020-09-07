import {Component, OnInit} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {
  SavedSearchService,
  SavedSearchTypeInfo,
  SavedSearchTypeSubInfo
} from "../../../services/saved-search.service";
import {
  convertToSavedSearchRequest,
  SavedSearch,
  SavedSearchType
} from "../../../model/saved-search";
import {SearchCandidateRequest} from "../../../model/search-candidate-request";
import {JoblinkValidationEvent} from "../../util/joblink/joblink.component";

@Component({
  selector: 'app-create-update-search',
  templateUrl: './create-update-search.component.html',
  styleUrls: ['./create-update-search.component.scss']
})

export class CreateUpdateSearchComponent implements OnInit {

  /**
   * Indicates whether component is being used to create a new object or
   * update an existing one.
   */
  private create: boolean;

  error = null;
  form: FormGroup;
  jobName: string;
  saving: boolean;
  private _savedSearch: SavedSearch;
  searchCandidateRequest: SearchCandidateRequest;
  savedSearchTypeInfos: SavedSearchTypeInfo[];
  savedSearchTypeSubInfos: SavedSearchTypeSubInfo[];
  sfJoblink: string;

  //Control access to savedSearch so that we can keep track of the object we
  //are working on. See code in set method which determines whether or not
  //we are in create mode.
  get savedSearch(): SavedSearch {
    return this._savedSearch;
  }

  set savedSearch(savedSearch: SavedSearch) {
    this._savedSearch = savedSearch;

    //If we are working on a default search or one with a 0 id, we are in
    //create mode.
    this.create = savedSearch.defaultSearch || savedSearch.id === 0;
  }

  get title(): string {
    return this.create ? "Make New Saved Search"
      : "Update Existing Saved Search";
  }

  get nameControl(): AbstractControl {
    return this.form.get('name')
  }

  get savedSearchTypeControl(): AbstractControl {
    return this.form.get('savedSearchType');
  }

  get savedSearchType(): number {
    const val = this.form.get('savedSearchType').value;
    //Convert value to number - or null if not populated
    return val ? +val : null;
  }

  get savedSearchSubtype(): number {
    const val = this.form.get('savedSearchSubtype').value;
    //Convert value to number - or null if not populated
    return val ? +val : null;
  }

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private savedSearchService: SavedSearchService) {
    this.savedSearchTypeInfos = savedSearchService.getSavedSearchTypeInfos();
  }

  ngOnInit() {

    this.form = this.fb.group({
      name: [this.savedSearch.defaultSearch ? null : this.savedSearch.name,
        Validators.required],
      savedSearchType: [this.savedSearch.defaultSearch ? null
        : this.savedSearch.savedSearchType, Validators.required],
      savedSearchSubtype: [this.savedSearch.defaultSearch ? null
        : this.savedSearch.savedSearchSubtype, this.subtypeRequiredValidator()],
      reviewable: [this.savedSearch?.reviewable,
        Validators.required],
    });

    if (this.savedSearch) {
      //Copy the form values in so that they can be displayed in any summary
      //(Otherwise we just see the unmodified search values)
      this.savedSearch = Object.assign(this.savedSearch, this.searchCandidateRequest);

      this.onSavedSearchTypeChange();
    }
  }

  private subtypeRequiredValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      //If there are subtypes associated with the currently selected type,
      //as indicated by a non null savedSearchTypeSubInfos, the subtype control
      //is required, ie must have a non empty value.
      return this.savedSearchTypeSubInfos && (control.value === undefined) ?
        { 'subtypeRequired': true } : null;
    };
  };

  cancel() {
    this.activeModal.dismiss(false);
  }

  save() {
    this.saving = true;

    if (this.create) {
      this.doCreate()
    } else {
      this.doUpdate();
    }
  }

  doCreate() {
    const formValues = this.form.value;
    this.savedSearch = {
      id: 0,
      name: this.nameControl.value,
      savedSearchType: this.savedSearchType,
      savedSearchSubtype: this.savedSearchSubtype,
      sfJoblink: this.sfJoblink,
      fixed: false,
      defaultSearch: false,
      reviewable: formValues.reviewable
    };

    //And create a SavedSearchRequest from the SavedSearch and the search request
    this.savedSearchService.create(
      convertToSavedSearchRequest(this.savedSearch, this.searchCandidateRequest)
    ).subscribe(
      (savedSearch) => {
        this.activeModal.close(savedSearch);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  doUpdate() {
    const formValues = this.form.value;
    this.savedSearch.name = this.nameControl.value;
    this.savedSearch.savedSearchType = this.savedSearchType;
    this.savedSearch.savedSearchSubtype = this.savedSearchSubtype;
    this.savedSearch.sfJoblink = this.sfJoblink;
    this.savedSearch.fixed = false;
    this.savedSearch.reviewable = formValues.reviewable;

    //Create a SavedSearchRequest from the SavedSearch and the search request
    this.savedSearchService.update(
      convertToSavedSearchRequest(this.savedSearch, this.searchCandidateRequest)
    ).subscribe(
      (savedSearch) => {
        this.saving = false;
        this.activeModal.close(savedSearch);
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  onSavedSearchTypeChange() {
    const formValues = this.form.value;
    const selectedSavedSearchType = formValues.savedSearchType;
    if (selectedSavedSearchType == null) {
      this.savedSearchTypeSubInfos = null;
    } else {
      this.savedSearchTypeSubInfos =
        this.savedSearchTypeInfos[selectedSavedSearchType].categories;
      if (!this.savedSearchTypeSubInfos) {
        this.form.controls["savedSearchSubtype"].patchValue(null);
      }
    }
  }

  onJoblinkValidation(jobOpportunity: JoblinkValidationEvent) {
    if (jobOpportunity.valid) {
      this.sfJoblink = jobOpportunity.sfJoblink;
      this.jobName = jobOpportunity.jobname;

      //If existing name and search type control are empty, auto copy into them
      if (!this.nameControl.value) {
        this.nameControl.patchValue(this.jobName);
        this.savedSearchTypeControl.patchValue(SavedSearchType.job);
      }
    } else {
      this.sfJoblink = null;
      this.jobName = null;
    }
  }
}
