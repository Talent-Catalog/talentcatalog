import {Component, OnDestroy, OnInit} from '@angular/core';

import {Candidate} from '../../../model/candidate';
import {CandidateService} from '../../../services/candidate.service';
import {Country} from '../../../model/country';
import {CountryService} from "../../../services/country.service";
import {Nationality} from "../../../model/nationality";
import {NationalityService} from "../../../services/nationality.service";
import {Language} from "../../../model/language";
import {LanguageService} from "../../../services/language.service";
import {SearchResults} from '../../../model/search-results';

import {NgbDateStruct, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {FormArray, FormBuilder, FormGroup} from "@angular/forms";
import {SearchSavedSearchesComponent} from "./saved-search/search-saved-searches.component";
import {SaveSearchComponent} from "./save/save-search.component";
import {SavedSearchService} from "../../../services/saved-search.service";
import {IDropdownSettings} from 'ng-multiselect-dropdown';
import {Subscription} from "rxjs";
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


@Component({
  selector: 'app-search-candidates',
  templateUrl: './search-candidates.component.html',
  styleUrls: ['./search-candidates.component.scss']
})
export class SearchCandidatesComponent implements OnInit, OnDestroy {

  searchForm: FormGroup;
  loading: boolean;
  error: any;
  moreFilters: boolean;
  results: SearchResults<Candidate>;
  savedSearch;
  subscription: Subscription;
  pageNumber: number;
  pageSize: number;

  /* MULTI SELECT */
  dropdownSettings: IDropdownSettings = {
    idField: 'id',
    textField: 'name',
    singleSelection: false,
    selectAllText: 'Select All',
    unSelectAllText: 'Deselect All',
    itemsShowLimit: 3,
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

  statuses: { id: string, name: string }[] = [
    {id: 'pending', name: 'pending'},
    {id: 'incomplete', name: 'incomplete'},
    {id: 'rejected', name: 'rejected'},
    {id: 'approved', name: 'approved'},
    {id: 'employed', name: 'employed'},
    {id: 'deleted', name: 'deleted'},
    {id: 'active', name: 'active'},
    {id: 'inactive', name: 'inactive'},
  ];

  selectedCandidate: Candidate;
  englishLanguageModel: LanguageLevelFormControlModel;
  otherLanguageModel: LanguageLevelFormControlModel;

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService,
              private nationalityService: NationalityService,
              private countryService: CountryService,
              private languageService: LanguageService,
              private savedSearchService: SavedSearchService,
              private educationLevelService: EducationLevelService,
              private educationMajorService: EducationMajorService,
              private candidateOccupationService: CandidateOccupationService,
              private modalService: NgbModal) {
  }

  ngOnInit() {
    this.moreFilters = false;
    this.selectedCandidate = null;
    this.pageNumber = 1;
    this.pageSize = 50;

    /* SET UP FORM */
    this.searchForm = this.fb.group({
      savedSearchId: [null],
      keyword: [null],
      statuses: [[]],
      gender: [null],
      occupationIds: [[]],
      orProfileKeyword: [null],
      verifiedOccupationIds: [[]],
      verifiedOccupationSearchType: [null],
      nationalityIds: [[]],
      nationalitySearchType: [null],
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
      shortlistStatus: null,
      //for display purposes
      occupations: [[]],
      verifiedOccupations: [[]],
      countries: [[]],
      educationMajors: [[]],
      nationalities: [[]],
      statusesDisplay: [[]],

    });

    /* LOAD NATIONALITIES */
    this.nationalityService.listNationalities().subscribe(
      (response) => {
        this.nationalities = response;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    /* LOAD COUNTRIES */
    this.countryService.listCountries().subscribe(
      (response) => {
        this.countries = response;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    /* LOAD LANGUAGES */
    this.languageService.listLanguages().subscribe(
      (response) => {
        this.languages = response;
        const englishLanguageObj = this.languages.find(l => l.name.toLowerCase() === 'english');
        this.englishLanguageModel = Object.assign(emptyLanguageLevelFormControlModel, {languageId: englishLanguageObj.id || null});
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    /* LOAD EDUCATIONAL LEVELS */
    this.educationLevelService.listEducationLevels().subscribe(
      (response) => {
        this.educationLevels = response;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });

    /* LOAD EDUCATIONAL MAJORS */
    this.educationMajorService.listMajors().subscribe(
      (response) => {
        this.educationMajors = response;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });

    /* LOAD VERIFIED OCCUPATIONS */
    this.candidateOccupationService.listVerifiedOccupations().subscribe(
      (response) => {
        this.verifiedOccupations = response;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });

    /* LOAD CANDIDATE OCCUPATIONS (includes unverified) */
    this.candidateOccupationService.listOccupations().subscribe(
      (response) => {
        this.candidateOccupations = response;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });

    /* SEARCH ON CHANGE*/
    this.searchForm.get('shortlistStatus').valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged()
      )
      .subscribe(res => {
        this.search();
      });
    this.search();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  /* MULTI SELECT METHODS */
  onItemSelect(item: any, formControlName: string) {
    const values = this.searchForm.controls[formControlName].value || [];
    const addValue = item.id || item;
    values.push(addValue);
    this.searchForm.controls[formControlName].patchValue(values);
  }

  onSelectAll(items: any[], formControlName: string) {
    const values = this.searchForm.controls[formControlName].value || [];
    items = items.map(i => i.id || i);
    values.push(...items);
    this.searchForm.controls[formControlName].patchValue(values);
  }

  onItemDeSelect(item: any, formControlName: string) {
    const values = this.searchForm.controls[formControlName].value || [];
    const removeValue = item.id || item;
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
    this.loading = true;
    this.error = null;
    const request = this.searchForm.value;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;
    this.subscription = this.candidateService.search(request).subscribe(
      results => {
        this.results = results;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      });
  }

  clear() {
    this.searchForm.reset();
    this.savedSearch = null;
   }

  loadSavedSearch(id) {
    this.loading = true;
    this.savedSearchService.load(id).subscribe(
      request => {
        this.populateFormFromRequest(request);
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
        this.loadSavedSearch(savedSearch.id)
      })
      .catch(() => { /* Isn't possible */
      });
  }

  showSave() {
    const showSaveModal = this.modalService.open(SaveSearchComponent, {
      centered: true,
      backdrop: 'static'
    });

    showSaveModal.componentInstance.savedSearchId = this.savedSearch ? this.savedSearch.id : null;
    showSaveModal.componentInstance.searchCandidateRequest = this.searchForm.value;

    showSaveModal.result
      .then((savedSearch) => {
        this.savedSearch = savedSearch;
        console.log(savedSearch);
      })
      .catch(() => { /* Isn't possible */
      });
  }


  populateFormFromRequest(request) {

    Object.keys(this.searchForm.controls).forEach(name => {
      this.searchForm.controls[name].patchValue(request[name]);

      //Form arrays need to be handled
      if (name === 'searchJoinRequests' && request[name]) {
        while (this.searchJoinArray.length) {
          this.searchJoinArray.removeAt(0);
        }
        request[name].forEach((join) => {
          this.searchJoinArray.push(this.fb.group(join))
        });
      }

      //todo why not re-rendering until click on screen
      let occupations = [];
      if (this.searchForm.value.occupationIds){
        occupations = this.candidateOccupations.filter(c => this.searchForm.value.occupationIds.indexOf(c.id) != -1);
      }
      this.searchForm.controls['occupations'].patchValue(occupations);

      let verifiedOccupations = [];
      if (this.searchForm.value.verifiedOccupationIds){
        verifiedOccupations = this.verifiedOccupations.filter(c => this.searchForm.value.verifiedOccupationIds.indexOf(c.id) != -1);
      }
      this.searchForm.controls['verifiedOccupations'].patchValue(verifiedOccupations);

      let educationMajors = [];
      if (this.searchForm.value.educationMajorIds){
        educationMajors = this.educationMajors.filter(c => this.searchForm.value.educationMajorIds.indexOf(c.id) != -1);
      }
      this.searchForm.controls['educationMajors'].patchValue(educationMajors);

      let nationalities = [];
      if (this.searchForm.value.nationalityIds){
        nationalities = this.nationalities.filter(c => this.searchForm.value.nationalityIds.indexOf(c.id) != -1);
      }
      this.searchForm.controls['nationalities'].patchValue(nationalities);

      let countries = [];
      if (this.searchForm.value.countryIds){
        countries = this.countries.filter(c => this.searchForm.value.countryIds.indexOf(c.id) != -1);
      }
      this.searchForm.controls['countries'].patchValue(countries);

      let statusesDisplay = [];
      if (this.searchForm.value.statuses){
        statusesDisplay = this.statuses.filter(c => this.searchForm.value.statuses.indexOf(c) != -1);
      }
      this.searchForm.controls['statusesDisplay'].patchValue(statusesDisplay);


    });


    this.search();
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
        this.searchJoinArray.push(this.fb.group(join))
      })
      .catch(() => { /* Isn't possible */
      });
  }

  viewCandidate(candidate: Candidate) {
    this.selectedCandidate = candidate;
  }

  handleDateSelected(e: {fromDate: NgbDateStruct, toDate: NgbDateStruct}, control: string) {
    if (e.fromDate) {
      console.log(e);
      this.searchForm.controls[control + 'From'].patchValue(e.fromDate.year + '-' + ('0' + e.fromDate.month).slice(-2)  + '-' + ('0' + e.fromDate.day).slice(-2));
    } else {
      this.searchForm.controls[control + 'From'].patchValue(null);
    }
    if (e.toDate) {
      this.searchForm.controls[control + 'To'].patchValue(e.toDate.year + '-' + ('0' + e.toDate.month).slice(-2) + '-' +('0' + e.toDate.day).slice(-2));
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
    //todo partial update
  }
}
