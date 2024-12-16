/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, OnInit} from '@angular/core';
import {AbstractControl, UntypedFormBuilder, UntypedFormGroup, ValidationErrors, ValidatorFn, Validators} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {SavedSearchService, SavedSearchTypeInfo, SavedSearchTypeSubInfo} from '../../../services/saved-search.service';
import {convertToSavedSearchRequest, SavedSearch, SavedSearchType} from '../../../model/saved-search';
import {SearchCandidateRequest} from '../../../model/search-candidate-request';
import {SalesforceService} from "../../../services/salesforce.service";

@Component({
  selector: 'app-create-update-search',
  templateUrl: './create-update-search.component.html',
  styleUrls: ['./create-update-search.component.scss']
})

export class CreateUpdateSearchComponent implements OnInit {

  error = null;
  form: UntypedFormGroup;
  jobName: string;
  jobId: number;
  saving: boolean;
  savedSearch: SavedSearch;
  searchCandidateRequest: SearchCandidateRequest;
  savedSearchTypeInfos: SavedSearchTypeInfo[];
  savedSearchTypeSubInfos: SavedSearchTypeSubInfo[];
  copy: boolean = false;
  newSavedSearch: SavedSearch;


  /**
   * Indicates whether component is being used to create a new object or
   * update an existing one.
   */
  get create(): boolean {
    //If we are working on a default search or one with a 0 id, we are in
    //create mode.
    return this.savedSearch?.defaultSearch || this.savedSearch?.id === 0
  }

  get copyCreate(): boolean {
    return this.copy;
  }

  get title(): string {
    let title;
    if (this.create) {
      title = "Make New Saved Search"
    } else if (this.copyCreate) {
      title = "Copy Search"
    } else {
      title = "Update Existing Saved Search";
    }
    return title;
  }

  get nameControl(): AbstractControl {
    return this.form.get('name')
  }

  get savedSearchTypeControl(): AbstractControl {
    return this.form.get('savedSearchType');
  }

  get savedSearchType(): number {
    const val = this.form.get('savedSearchType').value?.toString();
    //Convert string value to number - or null if not populated.
    //Note: Int 0 (profession) is null so returns null, by converting to a string it is no longer null.
    //todo look at other way to avoid int 0 null
    return val ? +val : null;
  }

  get savedSearchSubtype(): number {
    const val = this.form.get('savedSearchSubtype').value?.toString();
    //Convert value to number - or null if not populated. Same as above.
    return val ? +val : null;
  }

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              public salesforceService:SalesforceService,
              private savedSearchService: SavedSearchService) {
    this.savedSearchTypeInfos = savedSearchService.getSavedSearchTypeInfos();
  }

  ngOnInit() {

    //Note that we now only support creation of "Other" search types. That is hardcoded in form.
    this.form = this.fb.group({
      name: [this.savedSearch.defaultSearch ? null : this.savedSearch.name,
        Validators.required],
      savedSearchType: [SavedSearchType.other],
      savedSearchSubtype: [null],
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
      return this.savedSearchTypeSubInfos && (control.value == null) ?
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
    } else if (this.copyCreate) {
      this.doCopyAndCreate()
    } else {
      this.doUpdate();
    }
  }

  doCreate() {
    const formValues = this.form.value;
    this.newSavedSearch = {
      id: 0,
      name: this.nameControl.value,
      savedSearchType: this.savedSearchType,
      savedSearchSubtype: this.savedSearchSubtype,
      fixed: false,
      defaultSearch: false,
      reviewable: formValues.reviewable,
      global: false,
    };

    //And create a SavedSearchRequest from the SavedSearch and the search request
    this.savedSearchService.create(
      convertToSavedSearchRequest(this.newSavedSearch, this.jobId, this.searchCandidateRequest)
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

  doCopyAndCreate() {
    this.savedSearchService.load(this.savedSearch.id).subscribe(
      (request: SearchCandidateRequest) => {
        this.searchCandidateRequest = request;
        this.doCreate();
      }, (error) => {
        this.error = error;
      }
    )
  }

  doUpdate() {
    const formValues = this.form.value;
    this.savedSearch.name = this.nameControl.value;
    this.savedSearch.savedSearchType = this.savedSearchType;
    this.savedSearch.savedSearchSubtype = this.savedSearchSubtype;
    this.savedSearch.reviewable = formValues.reviewable;

    //Create a SavedSearchRequest from the SavedSearch and the search request
    this.savedSearchService.update(
      convertToSavedSearchRequest(this.savedSearch, this.jobId, this.searchCandidateRequest)
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
}
