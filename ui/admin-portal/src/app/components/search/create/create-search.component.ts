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
  SavedSearch
} from "../../../model/saved-search";
import {SearchCandidateRequest} from "../../../model/search-candidate-request";
import {SalesforceService} from "../../../services/salesforce.service";
import {JoblinkValidationEvent} from "../../util/joblink/joblink.component";

@Component({
  selector: 'app-create-search',
  templateUrl: './create-search.component.html',
  styleUrls: ['./create-search.component.scss']
})

export class CreateSearchComponent implements OnInit {

  error;
  form: FormGroup;
  jobName: string;
  saving: boolean;
  savedSearch: SavedSearch;
  searchCandidateRequest: SearchCandidateRequest;
  savedSearchTypeInfos: SavedSearchTypeInfo[];
  savedSearchTypeSubInfos: SavedSearchTypeSubInfo[];
  sfJoblink; string
  update;

  get nameControl(): AbstractControl {
    return this.form.get('name')
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
              private savedSearchService: SavedSearchService,
              private salesforceService: SalesforceService) {
    this.savedSearchTypeInfos = savedSearchService.getSavedSearchTypeInfos();
  }

  ngOnInit() {

    this.form = this.fb.group({
      name: [null, Validators.required],
      savedSearchType: [null, Validators.required],
      savedSearchSubtype: [null, this.subtypeRequiredValidator()],
      reviewable: [false, Validators.required],
    });

    if (this.savedSearch) {
      //Copy the form values in so that they can be displayed in any summary
      //(Otherwise we just see the unmodified search values)
      this.savedSearch = Object.assign(this.savedSearch, this.searchCandidateRequest);
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

  save() {
    this.saving = true;

    //Update the saved search with the name and value from the form.
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

  onSavedSearchTypeChange($event: Event) {
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

  onJoblinkValidation(jobOpportunity: JoblinkValidationEvent) {
    if (jobOpportunity.valid) {
      this.sfJoblink = jobOpportunity.sfJoblink;
      this.jobName = jobOpportunity.jobname;
    } else {
      this.sfJoblink = null;
      this.jobName = null;
    }
  }
}
