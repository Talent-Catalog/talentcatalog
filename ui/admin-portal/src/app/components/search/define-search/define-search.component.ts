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

import {Candidate} from '../../../model/candidate';
import {CandidateService} from '../../../services/candidate.service';
import {Country} from '../../../model/country';
import {CountryService} from "../../../services/country.service";
import {Nationality} from "../../../model/nationality";
import {NationalityService} from "../../../services/nationality.service";
import {Language} from "../../../model/language";
import {LanguageService} from "../../../services/language.service";
import {SearchResults} from '../../../model/search-results';

import {NgbDate, NgbDateStruct, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {FormArray, FormBuilder, FormGroup} from "@angular/forms";
import {SearchSavedSearchesComponent} from "../load-search/search-saved-searches.component";
import {CreateSearchComponent} from "../create/create-search.component";
import {SavedSearchService} from "../../../services/saved-search.service";
import {IDropdownSettings} from 'ng-multiselect-dropdown';
import {forkJoin, Subscription} from "rxjs";
import {JoinSavedSearchComponent} from "../join-search/join-saved-search.component";
import {EducationLevel} from "../../../model/education-level";
import {EducationLevelService} from "../../../services/education-level.service";
import {EducationMajor} from "../../../model/education-major";
import {EducationMajorService} from "../../../services/education-major.service";
import {Occupation} from "../../../model/occupation";
import {CandidateOccupationService} from "../../../services/candidate-occupation.service";
import {
  emptyLanguageLevelFormControlModel,
  LanguageLevelFormControlModel
} from "../../util/form/language-proficiency/language-level-form-control-model";
import * as moment from 'moment-timezone';
import {LanguageLevel} from "../../../model/language-level";
import {LanguageLevelService} from '../../../services/language-level.service';
import {DateRangePickerComponent} from "../../util/form/date-range-picker/date-range-picker.component";
import {LanguageLevelFormControlComponent} from "../../util/form/language-proficiency/language-level-form-control.component";
import {ActivatedRoute, Router} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {LocalStorageService} from "angular-2-local-storage";
import {UpdateSearchComponent} from "../update/update-search.component";
import {
  getCandidateSourceNavigation,
  getSavedSearchBreadcrumb,
  SavedSearch,
  SearchCandidateRequestPaged
} from "../../../model/saved-search";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {User} from "../../../model/user";
import {AuthService} from "../../../services/auth.service";


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

  /* MULTI SELECT */
  dropdownSettings: IDropdownSettings = {
    idField: 'id',
    textField: 'name',
    singleSelection: false,
    selectAllText: 'Select All',
    unSelectAllText: 'Deselect All',
    itemsShowLimit: 2,
    allowSearchFilter: true
  };

  /* DATA */
  nationalities: Nationality[];
  countries: Country[];
  languages: Language[];
  educationLevels: EducationLevel[];
  educationMajors: EducationMajor[];
  verifiedOccupations: Occupation[];
  candidateOccupations: Occupation[];
  languageLevels: LanguageLevel[];

  statuses: { id: string, name: string }[] = [
    {id: 'pending', name: 'pending'},
    {id: 'incomplete', name: 'incomplete'},
    {id: 'employed', name: 'employed'},
    {id: 'active', name: 'active'},
    {id: 'inactive', name: 'inactive'},
  ];

  selectedCandidate: Candidate;
  englishLanguageModel: LanguageLevelFormControlModel;
  otherLanguageModel: LanguageLevelFormControlModel;
  loggedInUser: User;

  selectedBaseJoin;
  storedBaseJoin;

  constructor(private http: HttpClient, private fb: FormBuilder,
              private candidateService: CandidateService,
              private nationalityService: NationalityService,
              private countryService: CountryService,
              private languageService: LanguageService,
              private savedSearchService: SavedSearchService,
              private educationLevelService: EducationLevelService,
              private educationMajorService: EducationMajorService,
              private candidateOccupationService: CandidateOccupationService,
              private languageLevelService: LanguageLevelService,
              private modalService: NgbModal,
              private localStorageService: LocalStorageService,
              private route: ActivatedRoute,
              private router: Router,
              private authService: AuthService) {
    /* SET UP FORM */
    this.searchForm = this.fb.group({
      savedSearchId: [null],
      simpleQueryString: [null],
      keyword: [null],
      statuses: [[]],
      gender: [null],
      occupationIds: [[]],
      orProfileKeyword: [null],
      verifiedOccupationIds: [[]],
      verifiedOccupationSearchType: ['or'],
      nationalityIds: [[]],
      nationalitySearchType: ['or'],
      countryIds: [[]],
      englishMinWrittenLevel: [null],
      englishMinSpokenLevel: [null],
      otherLanguageId: [null],
      otherMinWrittenLevel: [null],
      otherMinSpokenLevel: [null],
      unRegistered: [null],
      lastModifiedFrom: [null],
      lastModifiedTo: [null],
      createdFrom: [null],
      createdTo: [null],
      timezone: moment.tz.guess(),
      minAge: [null],
      maxAge: [null],
      minEducationLevel: [null],
      educationMajorIds: [[]],
      searchJoinRequests: this.fb.array([]),
      //for display purposes
      occupations: [[]],
      verifiedOccupations: [[]],
      countries: [[]],
      educationMajors: [[]],
      nationalities: [[]],
      statusesDisplay: [[]],
      includeDraftAndDeleted: [false],
      includeUploadedFiles: [false]}, {validator: this.validateDuplicateSearches('savedSearchId')});
  }

  ngOnInit() {
    this.selectedCandidate = null;
    this.loggedInUser = this.authService.getLoggedInUser();
    this.storedBaseJoin = null;

    forkJoin({
      'nationalities': this.nationalityService.listNationalities(),
      'countries': this.countryService.listCountries(),
      'languages': this.languageService.listLanguages(),
      'languageLevels': this.languageLevelService.listLanguageLevels(),
      'educationLevels': this.educationLevelService.listEducationLevels(),
      'majors': this.educationMajorService.listMajors(),
      'verifiedOccupation': this.candidateOccupationService.listVerifiedOccupations(),
      'occupations': this.candidateOccupationService.listOccupations()
    }).subscribe(results => {
      this.loading = false;
      this.nationalities = results['nationalities'];
      this.countries = results['countries'];
      this.languages = results['languages'];
      this.languageLevels = results['languageLevels'];
      this.educationLevels = results['educationLevels'];
      this.educationMajors = results['majors'];
      this.verifiedOccupations = results['verifiedOccupation'];
      this.candidateOccupations = results['occupations'];

      const englishLanguageObj = this.languages.find(l => l.name.toLowerCase() === 'english');
      this.englishLanguageModel = Object.assign(emptyLanguageLevelFormControlModel, {languageId: englishLanguageObj.id || null});

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

    request.reviewStatusFilter = null;

    //A new search request has to clear page number. Old page number no longer
    //relevant with new search.
    request.pageNumber = 0;
    request.pageSize = this.pageSize;
    request.sortFields = [this.sortField];
    request.sortDirection = this.sortDirection;

    this.searchRequest = request;
  }

  getIdsMultiSelect(request): SearchCandidateRequestPaged {
    if (request.countries != null) {
      request.countryIds = request.countries.map(c => c.id);
      delete request.countries;
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
      request.statuses = request.statusesDisplay.map(s => s.id);
      delete request.statusesDisplay;
    }

    return request;
  }

  clearForm() {
    this.searchForm.reset();
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
      request => {
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
    const showSaveModal = this.modalService.open(CreateSearchComponent);

    showSaveModal.componentInstance.savedSearch = this.savedSearch;

    // Convert ids as we do for searches
    const request = this.searchForm.value;
    showSaveModal.componentInstance.searchCandidateRequest =
      this.getIdsMultiSelect(request);


    showSaveModal.result
      .then((savedSearch) => {
        const urlCommands = getCandidateSourceNavigation(savedSearch);
        this.router.navigate(urlCommands);
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

  updateSavedSearchModal() {
    const showSaveModal = this.modalService.open(UpdateSearchComponent);

    showSaveModal.componentInstance.savedSearch = this.savedSearch;
    showSaveModal.componentInstance.searchCandidateRequest = this.searchForm.value;

    showSaveModal.result
      .then(() => {
      })
      .catch(() => { /* Isn't possible */
      });
  }

  populateFormWithSavedSearch(request) {
    /* Do a blanket patch of all form fields */
    Object.keys(this.searchForm.controls).forEach(name => {
      this.searchForm.controls[name].patchValue(request[name]);
    });

    // For the multiselects we have to set the corresponding id/name object

    /* STATUSES */
    let statuses = [];
    if (request.statuses && this.statuses) {
      statuses = this.statuses.filter(s => request.statuses.indexOf(s.id) !== -1);
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

    /* COUNTRIES */
    let countries = [];
    if (request.countryIds && this.countries) {
      countries = this.countries.filter(c => request.countryIds.indexOf(c.id) !== -1);
    }
    this.searchForm.controls['countries'].patchValue(countries);

    /* EDUCATION MAJORS */
    let educationMajors = [];
    if (request.educationMajorIds && this.educationMajors) {
      educationMajors = this.educationMajors
        .filter(c => request.educationMajorIds.indexOf(c.id) !== -1);
    }
    this.searchForm.controls['educationMajors'].patchValue(educationMajors);

    /* OTHER LANGUAGE */
    this.otherLanguagePicker.patchModel({
      languageId: request.otherLanguageId,
      writtenLevel: request.otherMinWrittenLevel,
      spokenLevel: request.otherMinSpokenLevel
    });

    /* NATIONALITIES */
    let nationalities = [];
    if (request.nationalityIds && this.nationalities) {
      nationalities = this.nationalities.filter(c => request.nationalityIds.indexOf(c.id) !== -1);
    }
    this.searchForm.controls['nationalities'].patchValue(nationalities);

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
    //We can change the search request if we own the savedSearch or if it
    //not fixed.
    let changeable: boolean = false;
    if (this.savedSearch) {
      if (!this.savedSearch.fixed) {
        changeable = true;
      } else {
        //Only can change the request associated with a saved search if we
        //own that search.
        if (this.loggedInUser && this.savedSearch.createdBy) {
          changeable = this.savedSearch.createdBy.id === this.loggedInUser.id;
        }
      }
    }
    return changeable;
  }
}
