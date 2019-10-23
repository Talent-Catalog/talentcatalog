import {Component, OnInit} from '@angular/core';
import {SearchResults} from '../../../model/search-results';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {LanguageService} from "../../../services/language.service";
import {CountryService} from "../../../services/country.service";
import {TranslationService} from "../../../services/translation.service";
import {Language} from '../../../model/language';
import {Country} from '../../../model/country';
import {Translation} from '../../../model/translation';
import {SystemLanguage} from '../../../model/language';
import {Occupation} from "../../../model/occupation";

@Component({
  selector: 'app-translations',
  templateUrl: './translations.component.html',
  styleUrls: ['./translations.component.scss']
})
export class TranslationsComponent implements OnInit {

  searchForm: FormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<Translation>;
  systemLanguages: SystemLanguage[];
  types: SearchResults<any>;

  constructor(private fb: FormBuilder,
              private modalService: NgbModal,
              private languageService: LanguageService,
              private countryService: CountryService,
              private translationService: TranslationService ) {
  }

  ngOnInit() {

    /* SET UP FORM */
    this.searchForm = this.fb.group({
      keyword: [''],
      type: ['', Validators.required],
      systemLanguage: ['', Validators.required],
    });
    this.pageNumber = 1;
    this.pageSize = 50;

    this.getSystemLanguages();
    this.onChanges();

  }

  getSystemLanguages() {
    this.loading = true;
    this.languageService.listSystemLanguages().subscribe(
      (response) => {
        this.systemLanguages = response;
        this.loading = false;
      },
      (error) => {
        console.error(error);
    });
  }

  onChanges(): void {
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
  }

  /* SEARCH FORM */
  search() {
    let request = this.searchForm.value;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;
  console.log(request);
    if(this.searchForm.valid){
      this.loading = true;
      this.translationService.search(request.type, request).subscribe(results => {
        this.results = results;
        this.loading = false;
      });
    }


  }



}
