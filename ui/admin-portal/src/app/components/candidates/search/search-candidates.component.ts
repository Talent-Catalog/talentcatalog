import { Component, OnInit } from '@angular/core';

import { Candidate } from '../../../model/candidate';
import { CandidateService } from '../../../services/candidate.service';
import { Country } from '../../../model/country';
import {CountryService} from "../../../services/country.service";
import {Nationality} from "../../../model/nationality";
import {NationalityService} from "../../../services/nationality.service";
import {Language} from "../../../model/language";
import {LanguageService} from "../../../services/language.service";
import { SearchResults } from '../../../model/search-results';

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
  pageNumber: number;
  pageSize: number;
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
  educationLevels: {id: string, label: string}[] = [
      {id: 'lessHighSchool', label: 'Less than High School'},
      {id: 'highSchool', label: 'Completed High School'},
      {id: 'bachelorsDegree', label: "Have a Bachelor's Degree"},
      {id: 'mastersDegree', label: "Have a Master's Degree"},
      {id: 'doctorateDegree', label: 'Have a Doctorate Degree'}
    ];
  ages: {id: string, label: string}[] = [
      {id: 'lessThan18', label: 'Less than 18'},
      {id: '18To24', label: '18 to 24'},
      {id: '24To30', label: '25 to 30'},
      {id: '30To40', label: '30 to 40'},
      {id: '40To50', label: '40 to 50'},
      {id: '50To60', label: '50 to 60'},
      {id: '60Plus', label: '60+ '},
  ];

  status: String[] = ['pending','incomplete','rejected','approved','employed','deleted', 'active', 'inactive'];

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService,
              private nationalityService: NationalityService,
              private countryService: CountryService,
              private languageService: LanguageService,
              private modalService: NgbModal) { }

  ngOnInit() {
    this.moreFilters = false;

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

  /* SET UP FORM */
    this.searchForm = this.fb.group({
      keyword: [''],
      status: [''],
      registeredWithUN: [''],
      nationalityId: [''],
      countryId: [''],
      gender: [''],
      educationLevel: [''],
      candidateLanguageId: [''],
      age: [''],
      selectedNationalities: [[]],
      selectedStatus: [[]]
    });
    this.pageNumber = 1;
    this.pageSize = 50;

  /* SEARCH ON CHANGE*/
    this.searchForm.valueChanges
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

/* METHODS */

 /* MULTI SELECT METHODS */
  onItemSelect(item: any) {
    this.selectedStatus.push(item.id);
  }
  onSelectAll(items: any) {}
  onItemDeSelect(item: any) {}
  onDeSelectAll(items: any) {}

  search(){
    this.loading = true;
    let request = this.searchForm.value;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize =  this.pageSize;
    this.candidateService.search(request).subscribe(results => {
      this.results = results;
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
