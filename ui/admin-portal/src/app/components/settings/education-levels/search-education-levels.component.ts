import {Component, OnInit} from '@angular/core';


import {SearchResults} from '../../../model/search-results';

import {FormBuilder, FormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {EducationLevel} from "../../../model/education-level";
import {EducationLevelService} from "../../../services/education-level.service";
import {CreateEducationLevelComponent} from "./create/create-education-level.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EditEducationLevelComponent} from "./edit/edit-education-level.component";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";

@Component({
  selector: 'app-search-education-levels',
  templateUrl: './search-education-levels.component.html',
  styleUrls: ['./search-education-levels.component.scss']
})
export class SearchEducationLevelsComponent implements OnInit {

  searchForm: FormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<EducationLevel>;


  constructor(private fb: FormBuilder,
              private educationLevelService: EducationLevelService,
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
    this.educationLevelService.search(request).subscribe(results => {
      this.results = results;
      this.loading = false;
    });
  }

  addEducationLevel() {
    const addEducationLevelModal = this.modalService.open(CreateEducationLevelComponent, {
      centered: true,
      backdrop: 'static'
    });

    addEducationLevelModal.result
      .then((educationLevel) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  editEducationLevel(educationLevel) {
    const editEducationLevelModal = this.modalService.open(EditEducationLevelComponent, {
      centered: true,
      backdrop: 'static'
    });

    editEducationLevelModal.componentInstance.educationLevelId = educationLevel.id;

    editEducationLevelModal.result
      .then((educationLevel) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  deleteEducationLevel(educationLevel) {
    const deleteEducationLevelModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteEducationLevelModal.componentInstance.message = 'Are you sure you want to delete '+educationLevel.name;

    deleteEducationLevelModal.result
      .then((result) => {
        // console.log(result);
        if (result === true) {
          this.educationLevelService.delete(educationLevel.id).subscribe(
            (educationLevel) => {
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
