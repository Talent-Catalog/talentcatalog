import {Component, OnInit} from '@angular/core';


import {SearchResults} from '../../../model/search-results';

import {FormBuilder, FormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {Industry} from "../../../model/industry";
import {IndustryService} from "../../../services/industry.service";
import {CreateIndustryComponent} from "./create/create-industry.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EditIndustryComponent} from "./edit/edit-industry.component";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";

@Component({
  selector: 'app-search-industries',
  templateUrl: './search-industries.component.html',
  styleUrls: ['./search-industries.component.scss']
})
export class SearchIndustriesComponent implements OnInit {

  searchForm: FormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<Industry>;


  constructor(private fb: FormBuilder,
              private industryService: IndustryService,
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
    this.industryService.search(request).subscribe(results => {
      this.results = results;
      this.loading = false;
    });
  }

  addIndustry() {
    const addIndustryModal = this.modalService.open(CreateIndustryComponent, {
      centered: true,
      backdrop: 'static'
    });

    addIndustryModal.result
      .then((industry) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  editIndustry(industry) {
    const editIndustryModal = this.modalService.open(EditIndustryComponent, {
      centered: true,
      backdrop: 'static'
    });

    editIndustryModal.componentInstance.industryId = industry.id;

    editIndustryModal.result
      .then((industry) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  deleteIndustry(industry) {
    const deleteIndustryModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteIndustryModal.componentInstance.message = 'Are you sure you want to delete '+ industry.name;

    deleteIndustryModal.result
      .then((result) => {
        console.log(result);
        if (result === true) {
          this.industryService.delete(industry.id).subscribe(
            (industry) => {
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
