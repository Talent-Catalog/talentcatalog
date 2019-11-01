import {Component, OnInit} from '@angular/core';


import {SearchResults} from '../../../model/search-results';

import {FormBuilder, FormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {Nationality} from "../../../model/nationality";
import {NationalityService} from "../../../services/nationality.service";
import {CreateNationalityComponent} from "./create/create-nationality.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EditNationalityComponent} from "./edit/edit-nationality.component";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";

@Component({
  selector: 'app-search-nationalities',
  templateUrl: './search-nationalities.component.html',
  styleUrls: ['./search-nationalities.component.scss']
})
export class SearchNationalitiesComponent implements OnInit {

  searchForm: FormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<Nationality>;


  constructor(private fb: FormBuilder,
              private nationalityService: NationalityService,
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
    this.nationalityService.search(request).subscribe(results => {
      this.results = results;
      this.loading = false;
    });
  }

  addNationality() {
    const addNationalityModal = this.modalService.open(CreateNationalityComponent, {
      centered: true,
      backdrop: 'static'
    });

    addNationalityModal.result
      .then((nationality) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  editNationality(nationality) {
    const editNationalityModal = this.modalService.open(EditNationalityComponent, {
      centered: true,
      backdrop: 'static'
    });

    editNationalityModal.componentInstance.nationalityId = nationality.id;

    editNationalityModal.result
      .then((nationality) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  deleteNationality(nationality) {
    const deleteNationalityModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteNationalityModal.componentInstance.message = 'Are you sure you want to delete '+nationality.name;

    deleteNationalityModal.result
      .then((result) => {
        // console.log(result);
        if (result === true) {
          this.nationalityService.delete(nationality.id).subscribe(
            (nationality) => {
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
