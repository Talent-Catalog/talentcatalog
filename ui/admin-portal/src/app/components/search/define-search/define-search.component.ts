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

import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild
} from '@angular/core';

import {Candidate, CandidateStatus, Gender, UnhcrStatus} from '../../../model/candidate';
import {Country} from '../../../model/country';
import {CountryService} from '../../../services/country.service';
import {Language} from '../../../model/language';
import {LanguageService} from '../../../services/language.service';
import {SearchResults} from '../../../model/search-results';

import {NgbDate, NgbDateStruct, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {
  AbstractControl,
  UntypedFormArray,
  UntypedFormBuilder,
  UntypedFormGroup
} from '@angular/forms';
import {SearchSavedSearchesComponent} from '../load-search/search-saved-searches.component';
import {CreateUpdateSearchComponent} from '../create-update/create-update-search.component';
import {SavedSearchService} from '../../../services/saved-search.service';
import {forkJoin} from 'rxjs';
import {JoinSavedSearchComponent} from '../join-search/join-saved-search.component';
import {EducationLevel} from '../../../model/education-level';
import {EducationLevelService} from '../../../services/education-level.service';
import {EducationMajor} from '../../../model/education-major';
import {EducationMajorService} from '../../../services/education-major.service';
import {Occupation} from '../../../model/occupation';
import {CandidateOccupationService} from '../../../services/candidate-occupation.service';
import {
  emptyLanguageLevelFormControlModel,
  LanguageLevelFormControlModel
} from '../../util/form/language-proficiency/language-level-form-control-model';
import * as moment from 'moment-timezone';
import {LanguageLevel} from '../../../model/language-level';
import {LanguageLevelService} from '../../../services/language-level.service';
import {
  DateRangePickerComponent
} from '../../util/form/date-range-picker/date-range-picker.component';
import {
  LanguageLevelFormControlComponent
} from '../../util/form/language-proficiency/language-level-form-control.component';
import {Router} from '@angular/router';
import {
  ClearSelectionRequest,
  getCandidateSourceNavigation,
  getSavedSearchBreadcrumb,
  SavedSearch,
  SearchCandidateRequestPaged
} from '../../../model/saved-search';
import {CandidateSource, CandidateSourceType} from '../../../model/base';
import {ConfirmationComponent} from '../../util/confirm/confirmation.component';
import {User} from '../../../model/user';
import {AuthorizationService} from '../../../services/authorization.service';
import {enumKeysToEnumOptions, EnumOption, enumOptions, isEnumOption} from "../../../util/enum";
import {SearchCandidateRequest} from "../../../model/search-candidate-request";
import {SurveyTypeService} from "../../../services/survey-type.service";
import {SurveyType} from "../../../model/survey-type";
import {SavedListService} from "../../../services/saved-list.service";
import {Partner} from "../../../model/partner";
import {PartnerService} from "../../../services/partner.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {SearchQueryService} from "../../../services/search-query.service";
import {first} from "rxjs/operators";

/**
 * This component contains all the search fields for saved and unsaved searches. It communicates
 * with the parent component candidates-search which contains the results part of the search.
 * Communicating with the parent allows us trigger the unsaved-changes router guard used on saved searches.
 * Some of the search fields are simple form controls, others are child components which pass values
 * to the form control. When patchValue is called it doesn't mark the form as dirty,
 * so this must be done manually. The unsaved changes router guard requires the dirty state, so this
 * needs to be emitted to the parent component. We automate emit of the dirty status if the
 * form value changes(via patchValue or user form changes). However, if we are updating the form values in
 * a different way (eg. add/delete base search) we need to make sure to emit the dirty status.
 * After a saved search is initially loaded into the form controls, we manually set the form status
 * to pristine. This will override any places the form controls have been marked as dirty when
 * populating as we don't want to trigger the unsaved changes guard after a search load, and we want
 * the search button disabled as no user changes have been made to the form.
 */
@Component({
  selector: 'app-define-search',
  templateUrl: './define-search.component.html',
  styleUrls: ['./define-search.component.scss']
})
export class DefineSearchComponent implements OnInit, OnChanges, AfterViewInit {
  @ViewChild('modifiedDate', {static: true}) modifiedDatePicker: DateRangePickerComponent;
  @ViewChild('englishLanguage', {static: true}) englishLanguagePicker: LanguageLevelFormControlComponent;
  @ViewChild('otherLanguage', {static: true}) otherLanguagePicker: LanguageLevelFormControlComponent;
  @ViewChild('formWrapper', {static: true}) formWrapper: ElementRef;
  @ViewChild('downloadCsvErrorModal', {static: true}) downloadCsvErrorModal;

  @Input() savedSearch: SavedSearch;
  @Input() pageNumber: number;
  @Input() pageSize: number;

  @Output() onFormChange = new EventEmitter<boolean>();

  error: any;
  loading: boolean;
  pgOnlySqlSearch: boolean;
  searchForm: UntypedFormGroup;
  showSearchRequest: boolean = false;
  results: SearchResults<Candidate>;
  savedSearchId;

  searchRequest: SearchCandidateRequestPaged;
  sortField = 'id';
  sortDirection = 'DESC';

  /* DATA - these are all drop down options for each select field*/
  nationalities: Country[];
  countries: Country[];
  partners: Partner[];
  languages: Language[];
  educationLevels: EducationLevel[];
  educationMajors: EducationMajor[];
  candidateOccupations: Occupation[];
  languageLevels: LanguageLevel[];
  surveyTypes: SurveyType[];

  notElastic;

  candidateStatusOptions: EnumOption[] = enumOptions(CandidateStatus);
  genderOptions: EnumOption[] = enumOptions(Gender);
  selectedCandidate: Candidate;
  selectedCandidates: Candidate[];
  englishLanguageModel: LanguageLevelFormControlModel;
  otherLanguageModel: LanguageLevelFormControlModel;
  loggedInUser: User;
  unhcrStatusOptions: EnumOption[] = enumOptions(UnhcrStatus);

  selectedBaseJoin;
  storedBaseJoin;
  /**
   * Will be true whenever there is any text in the Keyword Search input - can be used to hide filter
   * that doesn't have ES capability, though the practice should generally be avoided.
   */
  searchIsElastic: boolean = false;

  constructor(private fb: UntypedFormBuilder,
              private countryService: CountryService,
              private languageService: LanguageService,
              private partnerService: PartnerService,
              private savedSearchService: SavedSearchService,
              private educationLevelService: EducationLevelService,
              private educationMajorService: EducationMajorService,
              private candidateOccupationService: CandidateOccupationService,
              private surveyTypeService: SurveyTypeService,
              private languageLevelService: LanguageLevelService,
              private modalService: NgbModal,
              private router: Router,
              private savedListService: SavedListService,
              private authorizationService: AuthorizationService,
              private authenticationService: AuthenticationService,
              private searchQueryService: SearchQueryService
              ) {
    /* SET UP FORM */
    //todo For fixing this deprecation see https://stackoverflow.com/questions/65155217/formbuilder-group-is-deprecated
    this.searchForm = this.fb.group({
      savedSearchId: [null],
      simpleQueryString: [null],
      keyword: [null],
      statuses: [[]],
      gender: [null],
      occupationIds: [[]],
      minYrs: [null],
      maxYrs: [null],
      partnerIds: [[]],
      nationalityIds: [[]],
      nationalitySearchType: ['or'],
      countryIds: [[]],
      countrySearchType: ['or'],
      englishMinWrittenLevel: [null],
      englishMinSpokenLevel: [null],
      otherLanguageId: [null],
      otherMinWrittenLevel: [null],
      otherMinSpokenLevel: [null],
      lastModifiedFrom: [null],
      lastModifiedTo: [null],
      createdFrom: [null],
      createdTo: [null],
      timezone: moment.tz.guess(),
      minAge: [null],
      maxAge: [null],
      minEducationLevel: [],
      educationMajorIds: [[]],
      surveyTypeIds: [[]],
      miniIntakeCompleted: [null],
      fullIntakeCompleted: [null],
      searchJoinRequests: this.fb.array([]),
      unhcrStatuses: [[]],
      //for display purposes
      occupations: [[]],
      countries: [[]],
      partners: [[]],
      educationMajors: [[]],
      nationalities: [[]],
      regoReferrerParam: [null],
      statusesDisplay: [[]],
      surveyTypes: [[]],
      exclusionListId: [null],
      listAnyIds: [[]],
      listAnySearchType: [null],
      listAllIds: [[]],
      listAllSearchType: [null],
      unhcrStatusesDisplay: [[]],
      includeUploadedFiles: [false],
      potentialDuplicate: [null]
    }, {validator: this.validateDuplicateSearches('savedSearchId')});

    // Subscribe to changes in Keyword Search
    this.searchForm.get('simpleQueryString')?.statusChanges.subscribe(() => {
      this.searchIsElastic = this.searchForm.get('simpleQueryString')?.dirty &&
        this.searchForm.get('simpleQueryString')?.value !== '';
    });
  }

  ngOnInit() {
    this.selectedCandidate = null;
    this.loggedInUser = this.authenticationService.getLoggedInUser();
    this.storedBaseJoin = null;
    this.notElastic = {
      readonly: this.elastic()
    }
    this.loading = true;
    this.error = null;

    this.searchForm.get('simpleQueryString').valueChanges.pipe(
        first()
    ).subscribe(initialValue => {
      this.searchQueryService.changeSearchQuery(initialValue || '');
    });

    forkJoin({
      'nationalities': this.countryService.listCountries(),
      'countriesRestricted': this.countryService.listCountriesRestricted(),
      'languages': this.languageService.listLanguages(),
      'languageLevels': this.languageLevelService.listLanguageLevels(),
      'educationLevels': this.educationLevelService.listEducationLevels(),
      'majors': this.educationMajorService.listMajors(),
      'partners': this.partnerService.listSourcePartners(),
      'occupations': this.candidateOccupationService.listOccupations(),
      'surveyTypes': this.surveyTypeService.listSurveyTypes()
    }).subscribe(results => {
      this.loading = false;
      this.nationalities = results['nationalities'];
      this.countries = results['countriesRestricted'];
      this.languages = results['languages'];
      this.partners = results['partners'];
      this.languageLevels = results['languageLevels'];
      this.educationLevels = results['educationLevels'];
      this.educationMajors = results['majors'];
      this.candidateOccupations = results['occupations'];
      this.surveyTypes = results['surveyTypes'];

      const englishLanguageObj = this.languages.find(l => l.name.toLowerCase() === 'english');
      this.englishLanguageModel = Object.assign(emptyLanguageLevelFormControlModel, {languageId: englishLanguageObj.id || null});

      this.otherLanguageModel = Object.assign(emptyLanguageLevelFormControlModel)

      if (this.savedSearch) {
        this.loadSavedSearch(this.savedSearch.id);

        //By default, hide the saved search's request details unless we are
        //showing the default search (ie an unsaved search).
        this.showSearchRequest = this.savedSearch.defaultSearch;
      }

    }, error => {
      this.loading = false;
      this.error = error;
    });
    // Listen to form changes and emit form dirty status to candidate search component.
    // The unsaved changes guard is implemented on the saved search route, see app-routing.module.ts.
    // This guard will throw confirmation modal if navigating away with unsaved search fields, which
    // is determined if the form is dirty or not.
    this.searchForm.valueChanges.subscribe(() => {
      this.onFormChange.emit(this.searchForm.dirty);
    });
  }

  // Stops Keyword Search tooltip from opening on keydown.enter in inputs
  ngAfterViewInit() {
    const inputs: NodeList = document.querySelectorAll('input')
    inputs.forEach(input => {
      input.addEventListener('keydown', (event: KeyboardEvent) => {
        if (event.key === 'Enter') {
          event.preventDefault();
        }
      })
    })
  }

  get simpleQueryString(): string {
    return this.searchForm.value.simpleQueryString;
  }

  elastic(): boolean {
    return this.simpleQueryString != null && this.simpleQueryString.length > 0;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.savedSearch) {
      if (changes.savedSearch.previousValue !== changes.savedSearch.currentValue) {
        if (this.savedSearch) {
          this.savedSearchId = this.savedSearch.id;

          //The very first change will be loaded after ngInit finishes.
          if (!changes.savedSearch.isFirstChange()) {
            this.loadSavedSearch(this.savedSearchId);
          }
        }
      }
    }
  }

  validateDuplicateSearches(id: string) {
    return (group: UntypedFormGroup): { [key: string]: any } => {
      const savedSearchId = group.controls[id].value;
      if (this.selectedBaseJoin){
        const baseJoinId = this.selectedBaseJoin.savedSearchId;
        if (savedSearchId && baseJoinId && savedSearchId === baseJoinId) {
          return {
            error: "Can't select same base search as saved search."
          };
        }
        return {};
      }
    }
  }

  getError(){
    if (this.error) {
      return this.error;
    } else if (this.searchForm.hasError('error')){
      return this.searchForm.getError('error');
    } else {
      return null;
    }
  }

  onSubmit() {
   //checkSelectionsAndApply
   // If there are candidates selected, run a check before applying search.
    if (this.selectedCandidates.length > 0) {
      this.confirmClearSelectionAndApply();
    } else {
      this.apply();
    }
  }

  apply() {
    //Initialize a search request from the modified formData
    const request: SearchCandidateRequestPaged =
      this.getIdsMultiSelect(this.searchForm.value)

    //A new search request has to clear page number. Old page number no longer
    //relevant with new search.
    request.pageNumber = 0;
    request.pageSize = this.pageSize;
    request.sortFields = [this.sortField];
    request.sortDirection = this.sortDirection;

    request.pgOnlySqlSearch = this.pgOnlySqlSearch;

    //Note that just changing searchRequest triggers the display of the results
    //See the html of this component, for which app-show-candidates takes
    //searchRequest as an input.
    this.searchRequest = request;

    this.searchQueryService.changeSearchQuery(this.searchForm.value.simpleQueryString || '');
  }

  /**
   * Replaces arrays of objects with their corresponding ids.
   * @param request Form data
   */
  getIdsMultiSelect(request): SearchCandidateRequestPaged {
    if (request.countries != null) {
      request.countryIds = request.countries.map(c => c.id);
      delete request.countries;
    }

    if (request.partners != null) {
      request.partnerIds = request.partners.map(n => n.id);
      delete request.partners;
    }

    if (request.nationalities != null) {
      request.nationalityIds = request.nationalities.map(n => n.id);
      delete request.nationalities;
    }

    if (request.occupations != null) {
      request.occupationIds = request.occupations.map(o => o.id);
      delete request.occupations;
    }

    if (request.educationMajors != null) {
      request.educationMajorIds = request.educationMajors.map(o => o.id);
      delete request.educationMajors;
    }

    if (request.statusesDisplay != null) {
      //Pick up key values from EnumOptions
      request.statuses = request.statusesDisplay.map(s => s.key);
      delete request.statusesDisplay;
    }

    if (request.surveyTypes != null) {
      request.surveyTypeIds = request.surveyTypes.map(s => s.id);
      delete request.surveyTypes;
    }

    if (request.unhcrStatusesDisplay != null) {
      //Pick up key values from EnumOptions
      request.unhcrStatuses = request.unhcrStatusesDisplay.map(s => s.key);
      delete request.unhcrStatusesDisplay;
    }

    return request;
  }

  clearForm() {
    this.searchForm.reset();
    // We need to add back the saved search id because we are always working with a saved search object,
    // either it's a default saved search or saved search.
    this.searchForm.controls['savedSearchId'].patchValue(this.savedSearchId);

    this.searchForm.controls['countrySearchType'].patchValue('or');
    this.searchForm.controls['nationalitySearchType'].patchValue('or');
    this.searchForm.controls['listAllSearchType'].patchValue(null);
    this.searchForm.controls['listAnySearchType'].patchValue(null);

    while (this.searchJoinArray.length) {
      this.searchJoinArray.removeAt(0); // Clear the form array
    }
    this.selectedBaseJoin = null;
    this.storedBaseJoin = null;

    this.modifiedDatePicker.clearDates();
    this.englishLanguagePicker.clearProficiencies();
    this.otherLanguagePicker.form.reset();
    this.searchForm.markAsDirty();
  }

  newSearch() {
    this.router.navigate(['search']);
  }

  confirmClearSelectionAndApply() {
    const clearSelectionModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    clearSelectionModal.componentInstance.title = "Your selections will be cleared";
    clearSelectionModal.componentInstance.message = "Changing the search filters will clear any candidate's selected. " +
      "If you would like to keep your selections please save selections to a list before searching. Or to proceed without saving selections just click OK.";

    return clearSelectionModal.result
      .then((confirmation) => {
        if (confirmation == true) {
          this.clearSelection();
        }
      })
      .catch(() => { /* Isn't possible */
      });
  }

  clearSelection() {
    const request: ClearSelectionRequest = {
      userId: this.loggedInUser.id,
    };
    this.savedSearchService.clearSelection(this.savedSearch.id, request).subscribe(
      () => {
        this.selectedCandidates = [];
        this.apply();
      },
      err => {
        this.error = err;
      });
  }

  getBreadcrumb() {
    const infos = this.savedSearchService.getSavedSearchTypeInfos();
    return getSavedSearchBreadcrumb(this.savedSearch, infos);
  }

  loadSavedSearch(id) {
    this.loading = true;
    this.searchForm.controls['savedSearchId'].patchValue(id);

    // Clear the search join array and remove base search
    if (this.searchJoinArray.length) {
      this.searchJoinArray.removeAt(0);
    }
    this.selectedBaseJoin = null;

    this.savedSearchService.load(id).subscribe(
      (request) => {
        this.populateFormWithSavedSearch(request);
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      });
  }

  onExclusionListSelected(list: CandidateSource) {
    this.exclusionListIdControl.markAsDirty();
    this.exclusionListIdControl.patchValue(list?.id);
  }

  onListAnySelected(lists: CandidateSource[]) {
    //Update form value
    let ids: number[] = lists.map(s => s.id);
    this.listAnyIdsControl.markAsDirty();
    this.listAnyIdsControl.patchValue(ids);
  }

  onListAllSelected(lists: CandidateSource[]) {
    //Update form value
    let ids: number[] = lists.map(s => s.id);
    this.listAllIdsControl.markAsDirty();
    this.listAllIdsControl.patchValue(ids);
  }

  showSavedSearches() {
    const showSavedSearchesModal = this.modalService.open(SearchSavedSearchesComponent, {
      centered: true,
      backdrop: 'static'
    });

    showSavedSearchesModal.result
      .then((savedSearch) => {
        this.savedSearch = savedSearch;
        this.loadSavedSearch(savedSearch.id);
      })
      .catch(() => { /* Isn't possible */
      });
  }

  createNewSavedSearchModal() {
    this.openSavedSearchModal(true);
  }

  // Update coming from this define search component
  updateSavedSearchModal() {
    this.openSavedSearchModal(false);
  }

  openSavedSearchModal(create: boolean) {
    const showSaveModal = this.modalService.open(CreateUpdateSearchComponent);
    showSaveModal.componentInstance.savedSearch = this.savedSearch;

    //Load search parameters.
    // Convert ids as we do for searches
    const request = this.searchForm.value;
    showSaveModal.componentInstance.searchCandidateRequest =
      this.getIdsMultiSelect(request);

    showSaveModal.result
      .then((savedSearch) => {
        if (create) {
          //If it is a create we want to navigate away from the default
          //saved search to the newly created search.
          const urlCommands = getCandidateSourceNavigation(savedSearch);
          this.router.navigate(urlCommands);
        }
        // After updating we want to reset the form so it's no longer dirty, this will allow users to bypass the
        // unsaved changes guard.
        this.populateFormWithSavedSearch(this.searchForm.value);
        this.searchForm.markAsPristine();
      })
      .catch(() => {
      });
  }

  deleteSavedSearchModal() {
    const deleteSavedSearchModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteSavedSearchModal.componentInstance.message =
      'Are you sure you want to delete "' + this.savedSearch.name + '"';

    deleteSavedSearchModal.result
      .then((result) => {
        if (result === true) {
          this.savedSearchService.delete(this.savedSearch.id).subscribe(
            () => {
              this.router.navigate(['search']);
              this.loading = false;
            },
            (error) => {
              this.error = error;
              this.loading = false;
            });
        }
      })
      .catch(() => { });
  }

  populateFormWithSavedSearch(request: SearchCandidateRequest) {
    /* Do a blanket patch of all form fields */
    Object.keys(this.searchForm.controls).forEach(name => {
      this.searchForm.controls[name].patchValue(request[name]);
    });

    /* DEFAULTS */
    let searchType = request.countrySearchType;
    if (searchType == null) {
      searchType = 'or';
    }
    this.searchForm.controls['countrySearchType'].patchValue(searchType);

    searchType = request.nationalitySearchType;
    if (searchType == null) {
      searchType = 'or';
    }
    this.searchForm.controls['nationalitySearchType'].patchValue(searchType);

    searchType = request.listAllSearchType;
    if (searchType == null) {
      searchType = 'and';
    }
    this.searchForm.controls['listAllSearchType'].patchValue(searchType);

    searchType = request.listAnySearchType;
    if (searchType == null) {
      searchType = 'or';
    }
    this.searchForm.controls['listAnySearchType'].patchValue(searchType);


    // For the multiselects we have to set the corresponding id/name object by searching for the
    // values in the given search request in the complete set of drop down options for that field.

    /* STATUSES */

    let statuses: EnumOption[] = [];
    if (request.statuses) {
      statuses = enumKeysToEnumOptions(request.statuses, CandidateStatus);
    }
    this.searchForm.controls['statusesDisplay'].patchValue(statuses);

    /* ENGLISH PROFICIENCY */
    this.englishLanguagePicker.patchModel({
      writtenLevel: request.englishMinWrittenLevel,
      spokenLevel: request.englishMinSpokenLevel
    });

    /* OCCUPATIONS */
    let occupations = [];
    if (request.occupationIds && this.candidateOccupations) {
      occupations = this.candidateOccupations
        .filter(c => request.occupationIds.indexOf(c.id) !== -1);
    }
    this.searchForm.controls['occupations'].patchValue(occupations);

    /* MODIFIED DATES */
    if (request.lastModifiedFrom) {
      const [y, m, d] = request.lastModifiedFrom.split('-');
      const date: NgbDate = new NgbDate(Number(y), Number(m), Number(d));
      this.modifiedDatePicker.selectDate(date);
    }
    if (request.lastModifiedTo) {
      const [y, m, d] = request.lastModifiedTo.split('-');
      const date: NgbDate = new NgbDate(Number(y), Number(m), Number(d));
      this.modifiedDatePicker.selectDate(date);
    }

    /* PARTNERS */
    let partners = [];
    if (request.partnerIds && this.partners) {
      partners = this.partners.filter(c => request.partnerIds.indexOf(c.id) !== -1);
    }
    this.searchForm.controls['partners'].patchValue(partners);

    /* EDUCATION MAJORS */
    let educationMajors = [];
    if (request.educationMajorIds && this.educationMajors) {
      educationMajors = this.educationMajors
        .filter(c => request.educationMajorIds.indexOf(c.id) !== -1);
    }
    this.searchForm.controls['educationMajors'].patchValue(educationMajors);

    /* SURVEY TYPES */
    let surveyTypes = [];
    if (request.surveyTypeIds && this.surveyTypes) {
      surveyTypes = this.surveyTypes
        .filter(c => request.surveyTypeIds.indexOf(c.id) !== -1);
    }
    this.searchForm.controls['surveyTypes'].patchValue(surveyTypes);

    /* OTHER LANGUAGE */
    this.otherLanguagePicker.patchModel({
      languageId: request.otherLanguageId,
      writtenLevel: request.otherMinWrittenLevel,
      spokenLevel: request.otherMinSpokenLevel
    });

    /* COUNTRIES */
    let countries = [];
    if (request.countryIds && this.countries) {
      countries = this.countries.filter(c => request.countryIds.indexOf(c.id) !== -1);
    }
    this.searchForm.controls['countries'].patchValue(countries);

    /* NATIONALITIES */
    let nationalities = [];
    if (request.nationalityIds && this.nationalities) {
      nationalities = this.nationalities.filter(c => request.nationalityIds.indexOf(c.id) !== -1);
    }
    this.searchForm.controls['nationalities'].patchValue(nationalities);

    /* UNHCR STATUSES */
    let unhcrStatuses: EnumOption[] = [];
    if (request.unhcrStatuses) {
      unhcrStatuses = enumKeysToEnumOptions(request.unhcrStatuses, UnhcrStatus);
    }
    this.searchForm.controls['unhcrStatusesDisplay'].patchValue(unhcrStatuses);

    /* JOINED SEARCHES */
    while (this.searchJoinArray.length) {
      this.searchJoinArray.removeAt(0); // Clear the form array
    }
    if (request['searchJoinRequests'].length) {
      request['searchJoinRequests'].forEach((join) => {
        this.searchJoinArray.push(this.fb.group(join)); // If present, repopulate the array from the request
      });
      this.savedSearchService.get(request['searchJoinRequests'][0].savedSearchId).subscribe(result => {
        this.selectedBaseJoin = result;
      })
    }

    /* Perform a mouse event to force the multi-select components to update */
    this.formWrapper.nativeElement.click();

    /* Mark as pristine after search loaded, some fields may have been marked as dirty when
    populating the form fields but as this is the initial load of the form we don't want it to be dirty.
     */
    this.searchForm.markAsPristine();
    let isDirty = false;
    this.onFormChange.emit(isDirty);
  }

  get exclusionListId(): number {
    return this.exclusionListIdControl?.value;
  }

  get exclusionListIdControl(): AbstractControl {
    return this.searchForm.get('exclusionListId');
  }

  get listAllIds(): number[] {
    return this.listAllIdsControl?.value;
  }

  get listAllIdsControl(): AbstractControl {
    return this.searchForm.get('listAllIds');
  }

  get listAnyIds(): number[] {
    return this.listAnyIdsControl?.value;
  }

  get listAnyIdsControl(): AbstractControl {
    return this.searchForm.get('listAnyIds');
  }

  get searchJoinArray() {
    return this.searchForm.get('searchJoinRequests') as UntypedFormArray;
  }

  addSavedSearchJoin() {
    const joinSavedSearchComponent = this.modalService.open(JoinSavedSearchComponent, {
      centered: true,
      backdrop: 'static'
    });

    if (this.savedSearch != null){
      joinSavedSearchComponent.componentInstance.currentSavedSearchId = this.savedSearch.id;
    }

    joinSavedSearchComponent.result
      .then((join) => {
        this.searchJoinArray.push(this.fb.group(join));
      })
      .catch(() => { /* Isn't possible */
      });
  }

  deleteSavedSearchJoin(joinToRemove) {
    this.searchJoinArray.removeAt(this.searchJoinArray.value.findIndex(join => join.name === joinToRemove.name && join.searchType === joinToRemove.searchType))
  }

  handleDateSelected(e: { fromDate: NgbDateStruct, toDate: NgbDateStruct }, control: string) {
    this.searchForm.markAsDirty();
    if (e.fromDate) {
      this.searchForm.controls[control + 'From'].patchValue(e.fromDate.year + '-' + ('0' + e.fromDate.month).slice(-2) + '-' + ('0' + e.fromDate.day).slice(-2));
    } else {
      this.searchForm.controls[control + 'From'].patchValue(null);
    }
    if (e.toDate) {
      this.searchForm.controls[control + 'To'].patchValue(e.toDate.year + '-' + ('0' + e.toDate.month).slice(-2) + '-' + ('0' + e.toDate.day).slice(-2));
    } else {
      this.searchForm.controls[control + 'To'].patchValue(null);
    }
  }

  handleLanguageLevelChange(model: LanguageLevelFormControlModel, languageKey: string) {
    this.searchForm.markAsDirty();
    if (languageKey === 'english') {
      this.searchForm.controls['englishMinWrittenLevel'].patchValue(model.writtenLevel);
      this.searchForm.controls['englishMinSpokenLevel'].patchValue(model.spokenLevel);
    } else {
      // Update other language form values
      this.searchForm.controls['otherLanguageId'].patchValue(model.languageId);
      this.searchForm.controls['otherMinWrittenLevel'].patchValue(model.writtenLevel);
      this.searchForm.controls['otherMinSpokenLevel'].patchValue(model.spokenLevel);
    }
  }

  handleSearchTypeChange(control: string, value: 'and' | 'or' | 'not') {
    this.searchForm.controls[control].patchValue(value);
  }

  addBaseSearchJoin(baseSearch: SavedSearch) {
    this.selectedBaseJoin = {
      savedSearchId: baseSearch.id,
      name: baseSearch.name,
      searchType: "and"
    };
    // Clear the array before adding new base search
    if (this.searchJoinArray.length) {
      this.searchJoinArray.removeAt(0);
    }
    this.searchJoinArray.push(this.fb.group(this.selectedBaseJoin));
    this.searchForm.controls['searchJoinRequests'].markAsDirty();
    this.onFormChange.emit(this.searchForm.dirty);
  }

  deleteBaseSearchJoin() {
    if (this.searchJoinArray.length) {
      this.searchJoinArray.removeAt(0);
    }
    this.storedBaseJoin = null;
    this.selectedBaseJoin = null;
    this.searchForm.controls['searchJoinRequests'].markAsDirty();
    this.onFormChange.emit(this.searchForm.dirty);
  }

  canChangeSearchRequest(): boolean {
    return this.authorizationService.canEditCandidateSource(this.savedSearch);
  }

  public onSelectAll(options: any, formControl: any) {
    this.searchForm.controls[formControl].markAsDirty();
    this.searchForm.controls[formControl].patchValue(options);
  }

  public onClearAll(formControl: string) {
    this.searchForm.controls[formControl].markAsDirty();
    this.searchForm.controls[formControl].patchValue(null);
  }

  public getTooltip(formControlName: string) {
    let tooltip: string = '';
    const control = this.searchForm.controls[formControlName];
    const item = control.value[0];

    //If enum get the string value from each item, else get name.
    if (isEnumOption(item)) {
      control.value.forEach(i => tooltip += i.stringValue + ', ');
    } else {
      control.value.forEach(i => tooltip += i.name + ', ');
    }
    return tooltip.slice(0, -2);
  }

  public getPartnerDefaultMessage(): string {

    let s: string;

    //Source partners default to seeing only their candidates (unless they are the default partner)
    if (this.authorizationService.isSourcePartner() && !this.authorizationService.isDefaultPartner()) {
      s = "If nothing is specified here, the default is just to show candidates belonging to your partner.";
    } else {
      s = "If nothing is specified here, the default is to show candidates managed by any partner.";
    }

    return s;
  }

  public canViewCandidateName() {
    return this.authorizationService.canViewCandidateName();
  }

  public isEmployerPartner() {
    return this.authorizationService.isEmployerPartner();
  }

  public readonly CandidateSourceType = CandidateSourceType;
}
