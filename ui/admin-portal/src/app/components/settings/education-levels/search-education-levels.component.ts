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
import {EducationLevel} from "../../../model/education-level";
import {EducationLevelService} from "../../../services/education-level.service";
import {CreateEducationLevelComponent} from "./create/create-education-level.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EditEducationLevelComponent} from "./edit/edit-education-level.component";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {User} from "../../../model/user";

@Component({
  selector: 'app-search-education-levels',
  templateUrl: './search-education-levels.component.html',
  styleUrls: ['./search-education-levels.component.scss']
})
export class SearchEducationLevelsComponent implements OnInit {

  @Input() loggedInUser: User;

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
