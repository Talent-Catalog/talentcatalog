import {Component, OnInit} from '@angular/core';

import {Candidate} from '../../../model/candidate';
import {CandidateService} from '../../../services/candidate.service';
import {Country} from '../../../model/country';
import {CountryService} from "../../../services/country.service";
import {Nationality} from "../../../model/nationality";
import {NationalityService} from "../../../services/nationality.service";
import {Language} from "../../../model/language";
import {LanguageService} from "../../../services/language.service";
import {SearchResults} from '../../../model/search-results';

import {FormBuilder, FormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {EditCountryComponent} from "../../settings/countries/edit/edit-country.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {SearchSavedSearchesComponent} from "./saved/search-saved-searches.component";
import {SaveSearchComponent} from "./save/save-search.component";

@Component({
  selector: 'app-search-candidates',
  templateUrl: './search-candidates.component.html',
  styleUrls: ['./search-candidates.component.scss']
})
export class SearchCandidatesComponent implements OnInit {

  searchForm: FormGroup;
  loading: boolean;
  error: any;
  moreFilters: boolean;
  results: SearchResults<Candidate>;

  savedSearchId;
  /* MULTI SELECT */
  selectedNationalities = [];
  selectedStatus = [];
  dropdownSettings = {};

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

  statuses: String[] = ['pending', 'incomplete', 'rejected', 'approved', 'employed', 'deleted', 'active', 'inactive'];

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService,
              private nationalityService: NationalityService,
              private countryService: CountryService,
              private languageService: LanguageService,
              private modalService: NgbModal) { }

  ngOnInit() {
    this.moreFilters = false;

    /* SET UP FORM */
    this.searchForm = this.fb.group({
      selectedNationalities: [[]],
      selectedStatus: [[]],

      savedSearchId: [''],
      keyword: [''],
      statuses: [''],
      gender: [''],
      occupationIds: [''],
      orProfileKeyword: [''],
      verifiedOccupationIds: [''],
      verifiedOccupationSearchType: [''],
      nationalityIds: [''],
      nationalitySearchType: [''],
      countryIds: [''],
      englishMinWrittenLevelId: [''],
      englishMinSpokenLevelId: [''],
      otherLanguageId: [''],
      otherMinWrittenLevelId: [''],
      otherMinSpokenLevelId: [''],
      unRegistered: [''],
      lastModifiedFrom: [''],
      lastModifiedTo: [''],
      createdFrom: [''],
      createdTo: [''],
      minAge: [''],
      maxAge: [''],
      minEducationLevelId: [''],
      educationMajorIds: [''],
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
    this.searchForm.get('keyword').valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged()
      )
      .subscribe(res => {
        this.search();
      });
    this.search();

    /* MULTI SELECT DROPDOWN SETTINGS */
    this.dropdownSettings = {
      singleSelection: false,
      textField: 'name',
      selectAllText: 'Select All',
      unSelectAllText: 'UnSelect All',
      itemsShowLimit: 3,
      allowSearchFilter: true
    };

  }

  /* MULTI SELECT METHODS */
  onItemSelect(item: any) {
    this.selectedStatus.push(item.id);
  }

  onSelectAll(items: any) {
  }

  onItemDeSelect(item: any) {
  }

  onDeSelectAll(items: any) {
  }

  /* SEARCH FORM */
  search() {
    this.loading = true;
    const request = this.searchForm.value;
    request.page = request.page - 1;
    this.candidateService.search(request).subscribe(
      results => {
        this.results = results;
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
      .then((request) => this.populateFormFromRequest(request))
      .catch(() => { /* Isn't possible */ });
  }

  showSave() {
    const showSaveModal = this.modalService.open(SaveSearchComponent, {
      centered: true,
      backdrop: 'static'
    });

    showSaveModal.componentInstance.savedSearchId = this.savedSearchId;
    showSaveModal.componentInstance.searchCandidateRequest = this.searchForm.value;

    showSaveModal.result
      .then((savedSearch) => this.savedSearchId = savedSearch.id)
      .catch(() => { /* Isn't possible */ });
  }

  populateFormFromRequest(request){

  }

}
