import { Component, OnInit } from '@angular/core';
import { CandidateService } from '../../../services/candidate.service';
import { Candidate } from '../../../model/candidate';
import { Country } from '../../../model/country';
import {Nationality} from "../../../model/nationality";
import { SearchResults } from '../../../model/search-results';
import {FormBuilder, FormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {NationalityService} from "../../../services/nationality.service";
import {CountryService} from "../../../services/country.service";
import {Language} from "../../../model/language";
import {LanguageService} from "../../../services/language.service";

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

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService,
              private nationalityService: NationalityService,
              private countryService: CountryService,
              private languageService: LanguageService) { }

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
      age: ['']
    });
    this.pageNumber = 1;
    this.pageSize = 50;

    /* SEARCH ON CHANGE*/
    this.searchForm.get('keyword').valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged()
      )
      .subscribe(res => {
        this.search();
      });
    this.search();
  }

  search() {
    this.loading = true;
    let request = this.searchForm.value;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize =  this.pageSize;
    this.candidateService.search(request).subscribe(results => {
      console.log(request);
      console.log(results);
      this.results = results;
      this.loading = false;
    });
  }

}
