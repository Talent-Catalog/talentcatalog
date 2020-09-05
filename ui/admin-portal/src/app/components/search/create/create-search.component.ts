import {Component, OnInit} from '@angular/core';
import {
  AbstractControl,
  AsyncValidatorFn,
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
import {salesforceUrlPattern} from "../../../model/base";
import {Observable, of} from "rxjs";
import {SalesforceService} from "../../../services/salesforce.service";
import {catchError, map, tap} from "rxjs/operators";

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
  update;

  get nameControl(): AbstractControl {
    return this.form.get('name')
  }
  get sfJoblink() { return this.form.get('sfJoblink'); }

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
      sfJoblink: [this.savedSearch?.sfJoblink,
        [Validators.pattern(salesforceUrlPattern)], //Sync validators
        [this.sfJoblinkValidator()] //Async validators
      ],
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

  private sfJoblinkValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      const url: string = control.value;
      let retval;

      this.error = null;

      if (url == null || url.length === 0) {
        //Empty url always validates
        retval = of(null)
      } else {
        this.jobName = null;

        //See if we have name for a job corresponding to this url
        retval = this.salesforceService.findSfJobName(url).pipe(

          //As side effect populate the job name
          tap(opportunity =>
            this.jobName = opportunity === null ? null : opportunity.name),

          //Null names turn into validation error - otherwise no error
          map(opportunity => opportunity === null ? {'invalidSfJoblink': true} : null),

          //Problems connecting to server will be displayed but we won't
          //treat it as a validation error
          catchError(err => {
            this.error = err;
            return of(null);
          })
        );
      }

      return retval;
    };
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

}
