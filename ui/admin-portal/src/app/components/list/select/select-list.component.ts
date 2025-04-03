/*
 * Copyright (c) 2024 Talent Catalog.
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

import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {UntypedFormBuilder, UntypedFormGroup} from '@angular/forms';
import {isSubmissionList, SavedList, SearchSavedListRequest} from '../../../model/saved-list';
import {SavedListService} from '../../../services/saved-list.service';
import {CandidateStatus, UpdateCandidateStatusInfo} from "../../../model/candidate";
import {JobNameAndId} from "../../../model/job";


export interface TargetListSelection {
  //List id - 0 if new list requested
  savedListId: number;

  //Name of new list to be created (if any - only used if savedListId = 0)
  newListName?: string;

  //If true any existing contents of target list should be replaced, otherwise
  //contents are added (merged).
  replace: boolean;

  jobId?: number;

  /**
   * If present, the statuses of all candidates in list are set according to this.
   */
  statusUpdateInfo?: UpdateCandidateStatusInfo;
}


@Component({
  selector: 'app-select-list',
  templateUrl: './select-list.component.html',
  styleUrls: ['./select-list.component.scss']
})
export class SelectListComponent implements OnInit {

  error: string = null;
  excludeList: SavedList;
  form: UntypedFormGroup;
  jobName: string;
  jobId: number;
  loading: boolean;
  canChangeStatuses: boolean = true;
  readOnly: boolean = false;
  employerPartner: boolean = false;
  saving: boolean;
  action: string = "Save";
  title: string = "Select List";

  lists: SavedList[] = [];

  private statusUpdateInfo: UpdateCandidateStatusInfo;

  constructor(
    private savedListService: SavedListService,
    private activeModal: NgbActiveModal,
    private fb: UntypedFormBuilder) { }

  ngOnInit() {
    this.form = this.fb.group({
      newListName: [null],
      newList: [false],
      savedList: [null],
      replace: [false],
      changeStatuses: [false],
    },
      {validator: this.nonBlankListName()}
    );
    this.loadLists();
  }

  get changeStatuses(): boolean { return this.form.value.changeStatuses; }
  get newListNameControl() { return this.form.get('newListName'); }
  get newListName(): string { return this.form.value.newListName; }
  get newList(): boolean { return this.form.value.newList; }
  get replace(): boolean { return this.form.value.replace; }
  get savedList(): SavedList { return this.form.value.savedList; }
  get CandidateStatus() {
    return CandidateStatus;
  }

  private loadLists() {
    /*load all our non fixed lists */
    this.loading = true;
    const request: SearchSavedListRequest = {
      owned: true,
      shared: !this.readOnly,
      global: !this.employerPartner && !this.readOnly,
      fixed: false,
      ownedByMyPartner: this.employerPartner,
      // Don't allow selection of a list if it is a closed submission list
      sfOppClosed: false
    };

    this.savedListService.search(request).subscribe(
      (results) => {
        this.lists = results.filter(list => list.id !== this.excludeList?.id) ;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  select() {
    const selection: TargetListSelection = {
      savedListId: this.savedList === null ? 0 : this.savedList.id,
      newListName: this.newList ? this.newListName : null,
      replace: this.replace,
      jobId: this.jobId
    }

    if (this.changeStatuses) {
      selection.statusUpdateInfo = this.statusUpdateInfo;
    }
    // for submission lists we need to set replace to false
    if (this.selectedSubmissionList()) {
      selection.replace = false;
    }
    this.activeModal.close(selection);
  }

  disableNew() {
    this.form.controls['newList'].disable();
  }

  enableNew() {
    this.form.controls['newList'].enable();
    this.form.controls['savedList'].patchValue(null);
  }


  onJobSelection(job: JobNameAndId) {
    this.jobName = job?.name;
    this.jobId = job?.id;

    //If existing name is empty, auto copy into them
    if (!this.newListNameControl.value) {
      this.newListNameControl.patchValue(this.jobName);
    }
  }

  onStatusInfoUpdate(info: UpdateCandidateStatusInfo) {
    this.statusUpdateInfo = info;
  }

  private nonBlankListName() {
    return (group: UntypedFormGroup): { [key: string]: any } => {
      const newList: boolean = group.controls['newList'].value;
      if (newList) {
        const newListName: string = group.controls['newListName'].value;
        if (!newListName) {
          return { invalidName: "Name can't be blank" }
        }
      } else {
        const existingList: string = group.controls['savedList'].value;
        if (!existingList){
          return { invalidName: "List can't be blank"}
        }
      }
      return {}
    }
  }

  selectedSubmissionList(): boolean {
    return isSubmissionList(this.savedList);
  }
}
