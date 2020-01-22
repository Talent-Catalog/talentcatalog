import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';

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
import {SearchSavedSearchesComponent} from "./saved-search/search-saved-searches.component";
import {CreateSearchComponent} from "./create/create-search.component";
import {SavedSearchService} from "../../../services/saved-search.service";
import {IDropdownSettings} from 'ng-multiselect-dropdown';
import {forkJoin, Subscription} from "rxjs";
import {JoinSavedSearchComponent} from "./join-search/join-saved-search.component";
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
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import * as moment from 'moment-timezone';
import {CandidateShortlistItem} from "../../../model/candidate-shortlist-item";
import {LanguageLevel} from "../../../model/language-level";
import {LanguageLevelService} from '../../../services/language-level.service';
import {DateRangePickerComponent} from "../../util/form/date-range-picker/date-range-picker.component";
import {LanguageLevelFormControlComponent} from "../../util/form/language-proficiency/language-level-form-control.component";
import {ActivatedRoute} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {LocalStorageService} from "angular-2-local-storage";
import {UpdateSearchComponent} from "./update/update-search.component";


@Component({
  selector: 'app-search-candidates',
  templateUrl: './search-candidates.component.html',
  styleUrls: ['./search-candidates.component.scss']
})
export class SearchCandidatesComponent implements OnInit, OnDestroy {

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
  savedSearch;
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
    {id: 'deleted', name: 'deleted'},
    {id: 'active', name: 'active'},
    {id: 'inactive', name: 'inactive'},
  ];

  selectedCandidate: Candidate;
  englishLanguageModel: LanguageLevelFormControlModel;
  otherLanguageModel: LanguageLevelFormControlModel;

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
              private localStorage: LocalStorageService,
              private route: ActivatedRoute) {
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
      shortListStatusField: [null],
      shortlistStatus: [],
      //for display purposes
      occupations: [[]],
      verifiedOccupations: [[]],
      countries: [[]],
      educationMajors: [[]],
      nationalities: [[]],
      statusesDisplay: [[]],

    });
  }

  ngOnInit() {
    this.moreFilters = false;
    this.selectedCandidate = null;
    this.pageNumber = 1;
    this.pageSize = 20;

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
          })
        } else {
          let localStorageSearchRequest = JSON.parse(localStorage.getItem(this.searchKey));
          console.log(localStorageSearchRequest);
          console.log(this.searchForm);
          if (localStorageSearchRequest){
            setTimeout(() => {
              console.log('Populate from local storage');
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

    /* SEARCH ON CHANGE*/
    this.searchForm.get('shortlistStatus').valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged()
      )
      .subscribe(res => {
        console.log('searching as shortlist changes');
        if (!this.searching) {
          this.search();
        }
      });
  }


  ngOnDestroy(): void {
    if (this.subscription){
      this.subscription.unsubscribe();
    }
  }

  /* MULTI SELECT METHODS */
  onItemSelect(item: any, formControlName: string) {
    const values = this.searchForm.controls[formControlName].value || [];
    const addValue = item.id != null ? item.id : item;
    values.push(addValue);
    this.searchForm.controls[formControlName].patchValue(values);
  }

  onSelectAll(items: any[], formControlName: string) {
    const values = this.searchForm.controls[formControlName].value || [];
    items = items.map(i =>  i.id != null ? i.id : i);
    values.push(...items);
    this.searchForm.controls[formControlName].patchValue(values);
  }

  onItemDeSelect(item: any, formControlName: string) {
    const values = this.searchForm.controls[formControlName].value || [];
    const removeValue = item.id != null ? item.id : item;
    const indexToRemove = values.findIndex(val => val === removeValue);
    if (indexToRemove >= 0) {
      values.splice(indexToRemove, 1);
      this.searchForm.controls[formControlName].patchValue(values);
    }
  }

  onDeSelectAll(formControlName: string) {
    this.searchForm.controls[formControlName].patchValue([]);
  }

  /* SEARCH FORM */
  search() {
    this.searching = true;
    this.results = null;
    this.error = null;
    const request = this.searchForm.value;
    if (request.shortlistStatus == '') {
      request.shortlistStatus = null;
    }
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

  clearForm() {
    localStorage.setItem(this.searchKey, null);
    this.searchForm.reset();
    while (this.searchJoinArray.length) {
      this.searchJoinArray.removeAt(0); // Clear the form array
    }

    this.modifiedDatePicker.clearDates();
    this.englishLanguagePicker.clearProficiencies();
    this.otherLanguagePicker.form.reset();
    this.savedSearch = null;
  }

  loadSavedSearch(id) {
    // this._loading.savedSearch = true;
    this.searchForm.controls['savedSearchId'].patchValue(id);
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
        this.loadSavedSearch(savedSearch.id)
      })
      .catch(() => { /* Isn't possible */
      });
  }

  createNewSavedSearchModal() {
    const showSaveModal = this.modalService.open(CreateSearchComponent);

    showSaveModal.componentInstance.savedSearch = this.savedSearch;
    showSaveModal.componentInstance.searchCandidateRequest = this.searchForm.value;

    showSaveModal.result
      .then((savedSearch) => {
        this.savedSearch = savedSearch;
      })
      .catch(() => { /* Isn't possible */
      });
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

    /* STATUSES */
    this.searchForm.controls['statusesDisplay'].patchValue(request.statuses || []);

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
      countries = this.countries.filter(c => request.countryIds.indexOf(c.id) != -1);
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
      nationalities = this.nationalities.filter(c => request.nationalityIds.indexOf(c.id) != -1);
    }
    this.searchForm.controls['nationalities'].patchValue(nationalities);

    /* JOINED SEARCHES */
    while (this.searchJoinArray.length) {
      this.searchJoinArray.removeAt(0); // Clear the form array
    }
    if (request['searchJoinRequests']) {
      request['searchJoinRequests'].forEach((join) => {
        this.searchJoinArray.push(this.fb.group(join)) // If present, repopulate the array from the request
      });
    }

    /* Perform a mouse event to force the multi-select components to update */
    this.formWrapper.nativeElement.click();

    this.searchForm.controls['shortListStatusField'].patchValue('valid');
    this.setSortListStatus('valid');
  }

  get searchJoinArray() {
    return this.searchForm.get('searchJoinRequests') as FormArray;
  }

  addSavedSearchJoin() {
    const joinSavedSearchComponent = this.modalService.open(JoinSavedSearchComponent, {
      centered: true,
      backdrop: 'static'
    });

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

  handleCandidateShortlistSaved(candidateShortlistItem: CandidateShortlistItem) {
    this.search();
  }

  handleSearchTypeChange(control: string, value: 'or' | 'not') {
    this.searchForm.controls[control].patchValue(value);
  }

  toggleSort(column) {
    if (this.sortField == column) {
      this.sortDirection = this.sortDirection == 'ASC' ? 'DESC' : 'ASC';
    } else {
      this.sortField = column;
      this.sortDirection = 'ASC';
    }
    this.search();
  }

  setSortListStatus(event: any) {
    let statuses: string[] = [];

    const value: string = typeof event === "string" ? event : event.target.value;
    if (value == 'valid'){
      statuses.push('pending', 'verified')
    } else {
      statuses.push(value);
    }

    this.searchForm.controls['shortlistStatus'].patchValue(statuses);

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


}
