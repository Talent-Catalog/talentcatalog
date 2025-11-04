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
import {SavedSearch} from "../../../model/saved-search";
import {ActivatedRoute} from "@angular/router";
import {SavedSearchService} from "../../../services/saved-search.service";
import {BlockUnsavedChanges} from "../../../services/unsaved-changes.guard";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-candidates-search',
  templateUrl: './candidates-search.component.html',
  styleUrls: ['./candidates-search.component.scss']
})
export class CandidatesSearchComponent implements OnInit, BlockUnsavedChanges {
  error: string;
  loading: boolean;
  pageNumber: number;
  pageSize: number;
  savedSearch: SavedSearch;
  private id: number;
  jobId: number;
  formDirty: boolean;

  constructor(private route: ActivatedRoute,
              private savedSearchService: SavedSearchService,
              private modalService: NgbModal) { }

  ngOnInit() {
    this.loading = true;

    // start listening to route params after everything is loaded
    this.route.queryParamMap.subscribe(
      params => {
        this.jobId = +params.get('job');

        this.pageNumber = +params.get('pageNumber');
        if (!this.pageNumber) {
          this.pageNumber = 1;
        }
        this.pageSize = +params.get('pageSize');
        if (!this.pageSize) {
          this.pageSize = 20;
        }
      }
    );

    this.route.paramMap.subscribe(params => {
      this.id = +params.get('id');
      if (this.id) {

        //Load saved search to get name and type to display
        this.savedSearchService.get(this.id).subscribe(result => {
          this.savedSearch = result;
          this.loading = false;
        }, err => {
          this.error = err;
          this.loading = false;
        });
      } else {
        this.savedSearchService.getDefault().subscribe(result => {
          this.savedSearch = result;
          this.loading = false;
        }, err => {
          this.error = err;
          this.loading = false;
        });
      }
    });
  }

  canExit() {
    return this.formDirty ? this.unsavedChangesCheck() : true;
  }

  unsavedChangesCheck() {
    const unsavedChangesModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    unsavedChangesModal.componentInstance.title = "Unsaved search filter changes"
    unsavedChangesModal.componentInstance.message =
      'You have unsaved changes to the search filters - to keep please cancel and click the "Update Search" button to save them. Or to proceed without saving - click OK.'
    return unsavedChangesModal.result.then(
      () => {
        return true;
      },() => {
        return false;
      }
    );
  }

}
