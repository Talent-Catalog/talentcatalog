import {Component, OnInit} from '@angular/core';


import {SearchResults} from '../../../model/search-results';

import {FormBuilder, FormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {LanguageLevel} from "../../../model/language-level";
import {LanguageLevelService} from "../../../services/language-level.service";
import {CreateLanguageLevelComponent} from "./create/create-language-level.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EditLanguageLevelComponent} from "./edit/edit-language-level.component";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";

@Component({
  selector: 'app-search-language-levels',
  templateUrl: './search-language-levels.component.html',
  styleUrls: ['./search-language-levels.component.scss']
})
export class SearchLanguageLevelsComponent implements OnInit {

  searchForm: FormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<LanguageLevel>;


  constructor(private fb: FormBuilder,
              private languageLevelService: LanguageLevelService,
              private modalService: NgbModal) {
  }

  ngOnInit() {

    /* SET UP FORM */
    this.searchForm = this.fb.group({
      keyword: [''],
      status: ['active'],
    });
    this.pageNumber = 1;
    this.pageSize = 50;

    this.onChanges();
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
    this.languageLevelService.search(request).subscribe(results => {
      this.results = results;
      this.loading = false;
    });
  }

  addLanguageLevel() {
    const addLanguageLevelModal = this.modalService.open(CreateLanguageLevelComponent, {
      centered: true,
      backdrop: 'static'
    });

    addLanguageLevelModal.result
      .then((languageLevel) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  editLanguageLevel(languageLevel) {
    const editLanguageLevelModal = this.modalService.open(EditLanguageLevelComponent, {
      centered: true,
      backdrop: 'static'
    });

    editLanguageLevelModal.componentInstance.languageLevelId = languageLevel.id;

    editLanguageLevelModal.result
      .then((languageLevel) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  deleteLanguageLevel(languageLevel) {
    const deleteLanguageLevelModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteLanguageLevelModal.componentInstance.message = 'Are you sure you want to delete '+languageLevel.level;

    deleteLanguageLevelModal.result
      .then((result) => {
        // console.log(result);
        if (result === true) {
          this.languageLevelService.delete(languageLevel.id).subscribe(
            (languageLevel) => {
              this.loading = false;
              this.search();
            },
            (error) => {
              this.error = error;
              this.loading = false;
            });
          this.search()
        }
      })
      .catch(() => { /* Isn't possible */ });

  }
}
