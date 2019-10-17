import {Component, OnInit} from '@angular/core';
import {SearchResults} from '../../../model/search-results';
import {FormBuilder, FormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {LanguageService} from "../../../services/language.service";
import {Language} from '../../../model/language';

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
//  results: SearchResults<Country>;
  languages: Language[];

  constructor(private fb: FormBuilder,
              private modalService: NgbModal,
              private languageService: LanguageService ) {
  }

  ngOnInit() {

    /* SET UP FORM */
    this.searchForm = this.fb.group({
      keyword: [''],
      type: [''],
      languageId: [''],
    });
    this.pageNumber = 1;
    this.pageSize = 50;

    this.onChanges();
    this.getLanguages();
  }

  getLanguages() {
    this.languageService.listLanguages().subscribe(
      (response) => {
        this.languages = response;
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
    /*
    this.countryService.search(request).subscribe(results => {
      this.results = results;
      this.loading = false;
    });
    */
  }
}
