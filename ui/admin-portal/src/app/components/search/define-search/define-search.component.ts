import {
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
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
import {FormArray, FormBuilder, FormGroup, ValidationErrors, ValidatorFn} from "@angular/forms";
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
import {ActivatedRoute} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {LocalStorageService} from "angular-2-local-storage";
import {UpdateSearchComponent} from "../update/update-search.component";
import {
  getSavedSearchBreadcrumb,
  SavedSearch
} from "../../../model/saved-search";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {User} from "../../../model/user";
import {AuthService} from "../../../services/auth.service";


@Component({
  selector: 'app-define-search',
  templateUrl: './define-search.component.html',
  styleUrls: ['./define-search.component.scss']
})
export class DefineSearchComponent implements OnInit, OnDestroy {

  @ViewChild('modifiedDate', {static: true}) modifiedDatePicker: DateRangePickerComponent;
  @ViewChild('englishLanguage', {static: true}) englishLanguagePicker: LanguageLevelFormControlComponent;
  @ViewChild('otherLanguage', {static: true}) otherLanguagePicker: LanguageLevelFormControlComponent;
  @ViewChild('formWrapper', {static: true}) formWrapper: ElementRef;
  @ViewChild('downloadCsvErrorModal', {static: true}) downloadCsvErrorModal;

  error: any;
  loading: boolean;
  searching: boolean;
  exporting: boolean;

  searchKey = 'searchRequest';

  searchForm: FormGroup;
  moreFilters: boolean;
  results: SearchResults<Candidate>;
  savedSearch: SavedSearch;
  savedSearchId;
  subscription: Subscription;
  pageNumber: number;
  pageSize: number;
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
              private authService: AuthService) {
    /* SET UP FORM */
    this.searchForm = this.fb.group({
      savedSearchId: [null],
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
      includeUploadedFiles: [false]}, {validator: this.checkDuplicateSearches('savedSearchId')});
  }

  ngOnInit() {
    this.moreFilters = true;
    this.selectedCandidate = null;
    this.pageNumber = 1;
    this.pageSize = 20;
    this.loggedInUser = this.authService.getLoggedInUser();

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

      // start listening to route params after everything is loaded
      this.route.params.subscribe(params => {
        this.savedSearchId = params['savedSearchId'];
        if (this.savedSearchId) {
          this.savedSearchService.get(this.savedSearchId).subscribe(result => {
            this.savedSearch = result;
            this.loadSavedSearch(this.savedSearchId);
          }, err => {
            this.error = err;
          });
        } else {
          let localStorageSearchRequest = JSON.parse(localStorage.getItem(this.searchKey));
          if (localStorageSearchRequest){
            setTimeout(() => {
              this.populateFormWithSavedSearch(localStorageSearchRequest);
              this.search();
            }, 200);
          } else {
            this.search();
          }
        }
      });


    }, error => {
      this.loading = false;
      this.error = error;
    });
  }


  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  checkDuplicateSearches(id: string) {
    return (group: FormGroup): { [key: string]: any } => {
      let savedSearchId = group.controls[id].value;
      if(this.selectedBaseJoin){
        let baseJoinId = this.selectedBaseJoin.savedSearchId;
        if (savedSearchId && baseJoinId && savedSearchId === baseJoinId) {
          return {
            error: "Can't have the same saved search loaded as a selected base search."
          };
        }
        return {};
      }
    }
  }

  search() {
    this.pageNumber = 1;
    this.doSearch();
  }

  /* SEARCH FORM */
  doSearch() {
    this.searching = true;
    this.results = null;
    this.error = null;

    let request = this.searchForm.value;

    request = this.getIdsMultiSelect(request);

    request.shortlistStatus = null;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;
    request.sortFields = [this.sortField];
    request.sortDirection = this.sortDirection;
    localStorage.setItem(this.searchKey, JSON.stringify(request));

    this.subscription = this.candidateService.search(request).subscribe(
      results => {
        this.results = results;
        this.searching = false;
      },
      error => {
        this.error = error;
        this.searching = false;
      });
  }

  getIdsMultiSelect(request): any {
    if (request.countries != null) {
      request.countryIds = request.countries.map(c => c.id);
      request.countries = null;
    }

    if (request.nationalities != null) {
      request.nationalityIds = request.nationalities.map(n => n.id);
      request.nationalities = null;
    }

    if (request.occupations != null) {
      request.occupationIds = request.occupations.map(o => o.id);
      request.occupations = null;
    }

    if (request.educationMajors != null) {
      request.educationMajorIds = request.educationMajors.map(o => o.id);
      request.educationMajors = null;
    }

    if (request.statusesDisplay != null) {
      request.statuses = request.statusesDisplay.map(s => s.id);
      request.statusesDisplay = null;
    }

    return request;
  }

  clearForm() {
    localStorage.setItem(this.searchKey, null);
    this.searchForm.reset();
    while (this.searchJoinArray.length) {
      this.searchJoinArray.removeAt(0); // Clear the form array
    }
    this.selectedBaseJoin = null;

    this.modifiedDatePicker.clearDates();
    this.englishLanguagePicker.clearProficiencies();
    this.otherLanguagePicker.form.reset();
    this.savedSearch = null;
  }

  getBreadcrumb() {
    const infos = this.savedSearchService.getSavedSearchTypeInfos();
    return getSavedSearchBreadcrumb(this.savedSearch, infos);
  }

  loadSavedSearch(id) {
    // this._loading.savedSearch = true;
    this.searchForm.controls['savedSearchId'].patchValue(id);

    // Clear the search join array and remove base search
    if(this.searchJoinArray.length) {
      this.searchJoinArray.removeAt(0);
    }
    this.selectedBaseJoin = null;

    this.savedSearchService.load(id).subscribe(
      request => {
        this.populateFormWithSavedSearch(request);
      },
      error => {
        this.error = error;
        // this._loading.savedSearch = false;
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
        this.savedSearch = savedSearch;
        this.searchForm.controls['savedSearchId'].patchValue(savedSearch.id);
      })
      .catch(() => { /* Isn't possible */
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
            (savedSearch) => {
              this.savedSearch = null;
              this.loading = false;
            },
            (error) => {
              this.error = error;
              this.loading = false;
            });
        }
      })
      .catch(() => { /* Isn't possible */ });
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
      verifiedOccupations = this.verifiedOccupations.filter(c => request.verifiedOccupationIds.indexOf(c.id) != -1);
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
      occupations = this.candidateOccupations.filter(c => request.occupationIds.indexOf(c.id) != -1);
    }
    this.searchForm.controls['occupations'].patchValue(occupations);

    /* MODIFIED DATES */
    if (request.lastModifiedFrom) {
      let [y, m, d] = request.lastModifiedFrom.split('-');
      let date: NgbDate = new NgbDate(Number(y), Number(m), Number(d));
      this.modifiedDatePicker.selectDate(date);
    }
    if (request.lastModifiedTo) {
      let [y, m, d] = request.lastModifiedTo.split('-');
      let date: NgbDate = new NgbDate(Number(y), Number(m), Number(d));
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
      educationMajors = this.educationMajors.filter(c => request.educationMajorIds.indexOf(c.id) != -1);
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
    if (request['searchJoinRequests']) {
      request['searchJoinRequests'].forEach((join) => {
        this.searchJoinArray.push(this.fb.group(join)); // If present, repopulate the array from the request
      });
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

    if(this.savedSearch != null){
      joinSavedSearchComponent.componentInstance.currentSavedSearchId = this.savedSearch.id;
    };

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

  viewCandidate(candidate: Candidate) {
    this.selectedCandidate = candidate;
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

  toggleSort(column) {
    if (this.sortField === column) {
      this.sortDirection = this.sortDirection === 'ASC' ? 'DESC' : 'ASC';
    } else {
      this.sortField = column;
      this.sortDirection = 'ASC';
    }
    this.search();
  }

  exportCandidates() {
    this.exporting = true;
    let request = this.searchForm.value;
    request.size = 10000;
    this.candidateService.export(request).subscribe(
      result => {
        let options = {type: 'text/csv;charset=utf-8;'};
        let filename = 'candidates.csv';
        this.createAndDownloadBlobFile(result, options, filename);
        this.exporting = false;
      },
      err => {
        const reader = new FileReader();
        let _this = this;
        reader.addEventListener('loadend', function () {
          if (typeof reader.result === 'string') {
            _this.error = JSON.parse(reader.result);
            const modalRef = _this.modalService.open(_this.downloadCsvErrorModal);
            modalRef.result
              .then(() => {
              })
              .catch(() => {
              });
          }
        });
        reader.readAsText(err.error);
        this.exporting = false;
      }
    );
  }

  createAndDownloadBlobFile(body, options, filename) {
    let blob = new Blob([body], options);
    if (navigator.msSaveBlob) {
      // IE 10+
      navigator.msSaveBlob(blob, filename);
    } else {
      let link = document.createElement('a');
      // Browsers that support HTML5 download attribute
      if (link.download !== undefined) {
        let url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', filename);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      }
    }
  }

  downloadCv(candidate){
    let tab = window.open();
    this.candidateService.downloadCv(candidate.id).subscribe(
      result => {
        const fileUrl = URL.createObjectURL(result);
        tab.location.href = fileUrl;
      },
      error => {
        this.error = error;
      }
    )
  }

  addBaseSearchJoin(baseSearch: SavedSearch) {
    this.selectedBaseJoin = {
      savedSearchId: baseSearch.id,
      name: baseSearch.name,
      searchType: "and"
    };
    // Clear the array before adding new base search
    if(this.searchJoinArray.length) {
      this.searchJoinArray.removeAt(0);
    }
    this.searchJoinArray.push(this.fb.group(this.selectedBaseJoin));
  }

}
