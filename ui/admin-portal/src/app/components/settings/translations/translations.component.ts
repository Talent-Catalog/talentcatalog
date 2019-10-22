import {Component, OnInit} from '@angular/core';
import {SearchResults} from '../../../model/search-results';
import {FormBuilder, FormGroup} from "@angular/forms";
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
  translations: SearchResults<Translation>;
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
      type: [''],
      systemLanguage: [''],
    });
    this.pageNumber = 1;
    this.pageSize = 50;

    this.getSystemLanguages();
    this.onChanges();

  }

  getSystemLanguages() {
    this.languageService.listSystemLanguages().subscribe(
      (response) => {
        this.systemLanguages = response;
        console.log(this.systemLanguages);
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
    this.loading = true;
    let request = this.searchForm.value;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;

    if(request.type == "countries"){
      /* LOAD COUNTRIES */
      this.countryService.search(request).subscribe(results => {
        this.types = results;
        console.log(this.types);
        this.loading = false;
      });
      this.translate(request.type);
    }else if(request.type == "nationalities"){
      console.log("nationalities loading")
      /* LOAD NATIONALITIES
      this.nationalityService.search(request).subscribe(results => {
        console.log(results);
        this.results = results;
        this.loading = false;
      });
      this.translate();
      */
    }else {
      console.log("not country or nationality")
    }

  }

  translate(type) {
    this.loading = true
    this.translationService.search(type, this.searchForm.value.systemLanguage).subscribe(results => {
      for(var value of this.types.content) {
        let y = results.find(x => x.id == value.id);
        value.translation = y;
      }
      console.log(this.types.content);
      this.loading = false;
    });
  }

}
