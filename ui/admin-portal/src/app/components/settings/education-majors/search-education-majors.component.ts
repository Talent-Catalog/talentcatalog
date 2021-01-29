/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, Input, OnInit} from '@angular/core';


import {SearchResults} from '../../../model/search-results';

import {FormBuilder, FormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {EducationMajor} from "../../../model/education-major";
import {EducationMajorService} from "../../../services/education-major.service";
import {CreateEducationMajorComponent} from "./create/create-education-major.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EditEducationMajorComponent} from "./edit/edit-education-major.component";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {User} from "../../../model/user";

@Component({
  selector: 'app-search-education-majors',
  templateUrl: './search-education-majors.component.html',
  styleUrls: ['./search-education-majors.component.scss']
})
export class SearchEducationMajorsComponent implements OnInit {

  @Input() loggedInUser: User;

  searchForm: FormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<EducationMajor>;


  constructor(private fb: FormBuilder,
              private educationMajorService: EducationMajorService,
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
    this.educationMajorService.search(request).subscribe(results => {
      this.results = results;
      this.loading = false;
    });
  }

  addEducationMajor() {
    const addEducationMajorModal = this.modalService.open(CreateEducationMajorComponent, {
      centered: true,
      backdrop: 'static'
    });

    addEducationMajorModal.result
      .then((educationMajor) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  editEducationMajor(educationMajor) {
    const editEducationMajorModal = this.modalService.open(EditEducationMajorComponent, {
      centered: true,
      backdrop: 'static'
    });

    editEducationMajorModal.componentInstance.educationMajorId = educationMajor.id;

    editEducationMajorModal.result
      .then((educationMajor) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  deleteEducationMajor(educationMajor) {
    const deleteEducationMajorModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteEducationMajorModal.componentInstance.message = 'Are you sure you want to delete '+educationMajor.name;

    deleteEducationMajorModal.result
      .then((result) => {
        // console.log(result);
        if (result === true) {
          this.educationMajorService.delete(educationMajor.id).subscribe(
            (educationMajor) => {
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
