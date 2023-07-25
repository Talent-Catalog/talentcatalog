/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
  ViewChild
} from '@angular/core';

import {Candidate, CandidateFilterByOpps, CandidateStatus, Gender} from '../../../model/candidate';
import {CandidateService} from '../../../services/candidate.service';
import {Country} from '../../../model/country';
import {CountryService} from '../../../services/country.service';
import {Language} from '../../../model/language';
import {LanguageService} from '../../../services/language.service';
import {SearchResults} from '../../../model/search-results';

import {NgbDate, NgbDateStruct, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {FormArray, FormBuilder, FormGroup} from '@angular/forms';
import {SearchSavedSearchesComponent} from '../load-search/search-saved-searches.component';
import {CreateUpdateSearchComponent} from '../create-update/create-update-search.component';
import {SavedSearchService} from '../../../services/saved-search.service';
import {forkJoin, Subscription} from 'rxjs';
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
import {ActivatedRoute, Router} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {LocalStorageService} from 'angular-2-local-storage';
import {
  getCandidateSourceNavigation,
  getSavedSearchBreadcrumb,
  SavedSearch,
  SearchCandidateRequestPaged
} from '../../../model/saved-search';
import {canEditSource, SearchPartnerRequest} from '../../../model/base';
import {ConfirmationComponent} from '../../util/confirm/confirmation.component';
import {User} from '../../../model/user';
import {AuthService} from '../../../services/auth.service';
import {enumKeysToEnumOptions, EnumOption, enumOptions, isEnumOption} from "../../../util/enum";
import {SearchCandidateRequest} from "../../../model/search-candidate-request";
import {SurveyTypeService} from "../../../services/survey-type.service";
import {SurveyType} from "../../../model/survey-type";
import {SavedList, SearchSavedListRequest} from "../../../model/saved-list";
import {SavedListService} from "../../../services/saved-list.service";
import {Partner} from "../../../model/partner";
import {PartnerService} from "../../../services/partner.service";

@Component({
  selector: 'app-define-search',
  templateUrl: './define-search.component.html',
  styleUrls: ['./define-search.component.scss']
})
export class DefineSearchComponent implements OnInit, OnChanges, OnDestroy {

  @ViewChild('modifiedDate', {static: true}) modifiedDatePicker: DateRangePickerComponent;
  @ViewChild('englishLanguage', {static: true}) englishLanguagePicker: LanguageLevelFormControlComponent;
  @ViewChild('otherLanguage', {static: true}) otherLanguagePicker: LanguageLevelFormControlComponent;
  @ViewChild('formWrapper', {static: true}) formWrapper: ElementRef;
  @ViewChild('downloadCsvErrorModal', {static: true}) downloadCsvErrorModal;

  @Input() savedSearch: SavedSearch;
  @Input() pageNumber: number;
  @Input() pageSize: number;

  error: any;
  loading: boolean;
  searchForm: FormGroup;
  showSearchRequest: boolean = false;
  results: SearchResults<Candidate>;
  savedSearchId;

  searchRequest: SearchCandidateRequestPaged;

  subscription: Subscription;
  sortField = 'id';
  sortDirection = 'DESC';

  /* DATA - these are all drop down options for each select field*/
  nationalities: Country[];
  countries: Country[];
  partners: Partner[];
  languages: Language[];
  lists: SavedList[] = [];
  educationLevels: EducationLevel[];
  educationMajors: EducationMajor[];
  verifiedOccupations: Occupation[];
  candidateOccupations: Occupation[];
  languageLevels: LanguageLevel[];
  surveyTypes: SurveyType[];

  notElastic;

  candidateStatusOptions: EnumOption[] = enumOptions(CandidateStatus);
  genderOptions: EnumOption[] = enumOptions(Gender);
  candidateFilterByOppsOptions: EnumOption[] = enumOptions(CandidateFilterByOpps);
  selectedCandidate: Candidate;
  englishLanguageModel: LanguageLevelFormControlModel;
  otherLanguageModel: LanguageLevelFormControlModel;
  loggedInUser: User;

  selectedBaseJoin;
  storedBaseJoin;

  constructor(private http: HttpClient, private fb: FormBuilder,
              private candidateService: CandidateService,
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
              private localStorageService: LocalStorageService,
              private route: ActivatedRoute,
              private router: Router,
              private savedListService: SavedListService,
              private authService: AuthService) {
    /* SET UP FORM */
    this.searchForm = this.fb.group({
      savedSearchId: [null],
      simpleQueryString: [null],
      keyword: [null],
      statuses: [[]],
      gender: [null],
      occupationIds: [[]],
      minYrs: [null],
      maxYrs: [null],
      verifiedOccupationIds: [[]],
      verifiedOccupationSearchType: ['or'],
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
      searchJoinRequests: this.fb.array([]),
      //for display purposes
      occupations: [[]],
      verifiedOccupations: [[]],
      countries: [[]],
      partners: [[]],
      educationMajors: [[]],
      nationalities: [[]],
      regoReferrerParam: [null],
      statusesDisplay: [[]],
      surveyTypes: [[]],
      candidateFilterByOpps: [null],
      exclusionListId: [null],
      includeUploadedFiles: [false]}, {validator: this.validateDuplicateSearches('savedSearchId')});
  }

  ngOnInit() {
    this.selectedCandidate = null;
    this.loggedInUser = this.authService.getLoggedInUser();
    this.storedBaseJoin = null;
    this.notElastic = {
      readonly: this.elastic()
    }
    this.loading = true;
    this.error = null;

    const partnerRequest: SearchPartnerRequest = {sourcePartner: true};
    const request: SearchSavedListRequest = {owned: true, shared: true, global: true};
    forkJoin({
      'lists': this.savedListService.search(request),
      'nationalities': this.countryService.listCountries(),
      'countriesRestricted': this.countryService.listCountriesRestricted(),
      'languages': this.languageService.listLanguages(),
      'languageLevels': this.languageLevelService.listLanguageLevels(),
      'educationLevels': this.educationLevelService.listEducationLevels(),
      'majors': this.educationMajorService.listMajors(),
      'partners': this.partnerService.listSourcePartners(),
      'verifiedOccupation': this.candidateOccupationService.listVerifiedOccupations(),
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
      this.verifiedOccupations = results['verifiedOccupation'];
      this.candidateOccupations = results['occupations'];
      this.surveyTypes = results['surveyTypes'];
      this.lists = results['lists'];

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

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  validateDuplicateSearches(id: string) {
    return (group: FormGroup): { [key: string]: any } => {
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

    //Note that just changing searchRequest triggers the display of the results
    //See the html of this component, for which <app-show-candidates takes
    //searchRequest as an input.
    this.searchRequest = request;
  }

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

    return request;
  }

  clearForm() {
    this.searchForm.reset();
    this.searchForm.controls['countrySearchType'].patchValue('or');
    this.searchForm.controls['nationalitySearchType'].patchValue('or');

    while (this.searchJoinArray.length) {
      this.searchJoinArray.removeAt(0); // Clear the form array
    }
    this.selectedBaseJoin = null;
    this.storedBaseJoin = null;

    this.modifiedDatePicker.clearDates();
    this.englishLanguagePicker.clearProficiencies();
    this.otherLanguagePicker.form.reset();
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
            (found) => {
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

    // For the multiselects we have to set the corresponding id/name object by searching for the
    // values in the given search request in the complete set of drop down options for that field.

    /* STATUSES */

    let statuses: EnumOption[] = [];
    if (request.statuses) {
      statuses = enumKeysToEnumOptions(request.statuses, CandidateStatus);
    }
    this.searchForm.controls['statusesDisplay'].patchValue(statuses);

    /* VERIFIED OCCUPATIONS */
    let verifiedOccupations = [];
    if (request.verifiedOccupationIds && this.verifiedOccupations) {
      verifiedOccupations = this.verifiedOccupations
        .filter(c => request.verifiedOccupationIds.indexOf(c.id) !== -1);
    }
    this.searchForm.controls['verifiedOccupations'].patchValue(verifiedOccupations);

    /* ENGLISH PROFICIENCY */
    this.englishLanguagePicker.patchModel({
      writtenLevel: request.englishMinWrittenLevel,
      spokenLevel: request.englishMinSpokenLevel
    });

    /* UNVERIFIED OCCUPATIONS */
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
    let countrySearchType = request.countrySearchType;
    if (countrySearchType == null) {
      countrySearchType = 'or';
    }
    this.searchForm.controls['countrySearchType'].patchValue(countrySearchType);

    /* NATIONALITIES */
    let nationalities = [];
    if (request.nationalityIds && this.nationalities) {
      nationalities = this.nationalities.filter(c => request.nationalityIds.indexOf(c.id) !== -1);
    }
    this.searchForm.controls['nationalities'].patchValue(nationalities);
    let searchType = request.nationalitySearchType;
    if (searchType == null) {
      searchType = 'or';
    }
    this.searchForm.controls['nationalitySearchType'].patchValue(searchType);

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
  }

  get searchJoinArray() {
    return this.searchForm.get('searchJoinRequests') as FormArray;
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
    if (e.fromDate) {
      // console.log(e);
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

  handleSearchTypeChange(control: string, value: 'or' | 'not') {
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
  }

  deleteBaseSearchJoin() {
    if (this.searchJoinArray.length) {
      this.searchJoinArray.removeAt(0);
    }
    this.storedBaseJoin = null;
    this.selectedBaseJoin = null;
  }

  canChangeSearchRequest(): boolean {
    return canEditSource(this.savedSearch, this.authService)
  }

  public onSelectAll(options: any, formControl: any) {
    this.searchForm.controls[formControl].patchValue(options);
  }

  public onClearAll(formControl: string) {
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
    if (this.authService.isSourcePartner() && !this.authService.isDefaultPartner()) {
      s = "If nothing is specified, the default is to just show candidates belonging to your partner";
    } else {
      s = "If nothing is specified, the default is to show candidates managed by any partner";
    }

    return s;
  }
}
