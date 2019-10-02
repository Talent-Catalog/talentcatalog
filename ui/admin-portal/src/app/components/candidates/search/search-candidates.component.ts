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

import {FormArray, FormBuilder, FormControl, FormGroup} from "@angular/forms";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {SearchSavedSearchesComponent} from "./saved/search-saved-searches.component";
import {SaveSearchComponent} from "./save/save-search.component";
import {SavedSearchService} from "../../../services/saved-search.service";
import {SavedSearch, SavedSearchJoin} from "../../../model/saved-search";
import {IDropdownSettings} from 'ng-multiselect-dropdown';
import {Subscription} from "rxjs";

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
  educationLevels: { id: string, label: string }[] = [
    {id: 'lessHighSchool', label: 'Less than High School'},
    {id: 'highSchool', label: 'Completed High School'},
    {id: 'bachelorsDegree', label: "Have a Bachelor's Degree"},
    {id: 'mastersDegree', label: "Have a Master's Degree"},
    {id: 'doctorateDegree', label: 'Have a Doctorate Degree'}
  ];
  occupations: { id: string, name: string }[] = [
    {id: 'tester', name: 'Tester'}
  ];

  statuses: {id: string, name: string}[] = [
    {id: 'pending', name: 'pending'},
    {id: 'incomplete', name: 'incomplete'},
    {id: 'rejected', name: 'rejected'},
    {id: 'approved', name: 'approved'},
    {id: 'employed', name: 'employed'},
    {id: 'deleted', name: 'deleted'},
    {id: 'active', name: 'active'},
    {id: 'inactive', name: 'inactive'},
  ];
  savedSearches: SavedSearch[];
  searchJoin: SavedSearchJoin;
  selectedCandidate: Candidate;

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService,
              private nationalityService: NationalityService,
              private countryService: CountryService,
              private languageService: LanguageService,
              private savedSearchService: SavedSearchService,
              private modalService: NgbModal) { }

  ngOnInit() {
    this.moreFilters = false;
    this.selectedCandidate = null;
    this.resetSavedSearchJoin();
    // TODO saved search service call
    this.savedSearches = [];

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
      englishMinWrittenLevelId: [null],
      englishMinSpokenLevelId: [null],
      otherLanguageId: [null],
      otherMinWrittenLevelId: [null],
      otherMinSpokenLevelId: [null],
      unRegistered: [null],
      lastModifiedFrom: [null],
      lastModifiedTo: [null],
      createdFrom: [null],
      createdTo: [null],
      minAge: [null],
      maxAge: [null],
      minEducationLevelId: [null],
      educationMajorIds: [[]],
      searchJoinRequests: this.fb.array([]),
      page: 1,
      size: 50
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
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    // TODO Change to explicit button click
    /* SEARCH ON CHANGE */
    // this.searchForm.get('keyword').valueChanges
    //   .pipe(
    //     debounceTime(400),
    //     distinctUntilChanged()
    //   )
    //   .subscribe(res => {
    //     this.search();
    //   });
    // this.search();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  /* MULTI SELECT METHODS */
  onItemSelect(item: any, formControlName: string) {
    /* DEBUG */
    console.log('item', item);
    const values = this.searchForm.controls[formControlName].value || [];
    const addValue = item.id || item;
    /* DEBUG */
    console.log('addvalud', addValue);
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
    const request = this.searchForm.value;
    request.page = request.page - 1;
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

  loadSavedSearch(id){
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
      .catch(() => { /* Isn't possible */ });
  }

  showSave() {
    const showSaveModal = this.modalService.open(SaveSearchComponent, {
      centered: true,
      backdrop: 'static'
    });

    showSaveModal.componentInstance.savedSearchId = this.savedSearch? this.savedSearch.id:null;
    showSaveModal.componentInstance.searchCandidateRequest = this.searchForm.value;

    showSaveModal.result
      .then((savedSearch) => {
        this.savedSearch = savedSearch;
        console.log(savedSearch);
      })
      .catch(() => { /* Isn't possible */ });
  }

  populateFormFromRequest(request){

    Object.keys(this.searchForm.controls).forEach(name => {
        this.searchForm.controls[name].patchValue(request[name]);
    });
    this.search();
  }

  resetSavedSearchJoin() {
    this.searchJoin = {savedSearchId: null, searchType: null};
  }

  addSavedSearchJoin() {
    (this.searchForm.controls.searchJoinRequests as FormArray).push(new FormControl(this.searchJoin));
    this.resetSavedSearchJoin();
  }

  viewCandidate(candidate: Candidate) {
    this.selectedCandidate = candidate;
  }
}
