/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, OnInit} from '@angular/core';
import {SavedSearch} from "../../../model/saved-search";
import {ActivatedRoute} from "@angular/router";
import {SavedSearchService} from "../../../services/saved-search.service";
import {BlockUnsavedChanges} from "../../../services/unsaved-changes.guard";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {combineLatest} from "rxjs";

@Component({
  selector: 'app-candidates-search',
  templateUrl: './candidates-search.component.html',
  styleUrls: ['./candidates-search.component.scss']
})
export class CandidatesSearchComponent implements OnInit, BlockUnsavedChanges {
  error: string;
  jobAssignmentError: string;
  loading: boolean;
  pageNumber: number;
  pageSize: number;
  savedSearch: SavedSearch;
  private id: number;
  private previousId: number;
  jobId: number;
  listId: number;
  formDirty: boolean;

  constructor(private route: ActivatedRoute,
              private savedSearchService: SavedSearchService,
              private modalService: NgbModal) { }

  ngOnInit() {
    this.loading = true;

    // Both paramMap (which saved search to load) and queryParamMap (job/list/paging hints) are
    // needed together to decide whether a job query param should be explicitly assigned to the
    // user's default search - see assignJobToDefaultSearchIfNeeded. Using combineLatest (rather
    // than nested or separate subscriptions) lets us process the latest values of both together.
    combineLatest([this.route.paramMap, this.route.queryParamMap]).subscribe(
      ([params, queryParams]) => {
        this.jobId = +queryParams.get('job');
        this.listId = +queryParams.get('list');

        this.pageNumber = +queryParams.get('pageNumber');
        if (!this.pageNumber) {
          this.pageNumber = 1;
        }
        this.pageSize = +queryParams.get('pageSize');
        if (!this.pageSize) {
          this.pageSize = 20;
        }

        const id = +params.get('id');
        const idChanged = id !== this.previousId;
        this.id = id;

        // Only (re)load the saved search when the :id route param has actually changed - not
        // merely because a query param changed (eg. Clear Search removing 'job' from the URL,
        // see DefineSearchComponent.clearJobAssociation).
        if (idChanged) {
          this.previousId = id;
          this.loading = true;

          if (id) {
            //Load saved search to get name and type to display
            this.savedSearchService.get(id).subscribe(result => {
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
              this.assignJobToDefaultSearchIfNeeded(result);
            }, err => {
              this.error = err;
              this.loading = false;
            });
          }
        }
      }
    );
  }

  /**
   * If the URL carries a job id, and the saved search just loaded is the user's default search,
   * explicitly (and immediately) persists that job association server side - so that it is
   * retained even if the user navigates away without running a search. Does nothing if the
   * default search is already associated with that same job (see TC-1535).
   * <p/>
   * Deliberately mutates sfJobOpp on the existing savedSearch object in place, rather than
   * reassigning this.savedSearch to a new object/reference. DefineSearchComponent's @Input()
   * savedSearch is bound to this same object; reassigning the reference would re-trigger its
   * ngOnChanges -> loadSavedSearch(), which races with (and can clobber the result of) the
   * ordinary job-driven search already in progress via its own jobId input.
   */
  private assignJobToDefaultSearchIfNeeded(savedSearch: SavedSearch) {
    if (this.jobId && savedSearch?.defaultSearch && savedSearch.sfJobOpp?.id !== this.jobId) {
      this.jobAssignmentError = null;
      this.savedSearchService.updateJob(savedSearch.id, this.jobId).subscribe({
        next: updated => {
          savedSearch.sfJobOpp = updated.sfJobOpp;
        },
        error: err => {
          this.jobAssignmentError = err;
        }
      });
    }
  }

  canExit() {
    return this.formDirty ? this.unsavedChangesCheck() : true;
  }

  private isAutoUpdateSearch(): boolean {
    return this.savedSearch?.autoUpdateOnSearch ?? true;
  }

  unsavedChangesCheck() {
    const unsavedChangesModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    const autoUpdateSearch = this.isAutoUpdateSearch();

    unsavedChangesModal.componentInstance.title =
      autoUpdateSearch
        ? "Unapplied search filter changes"
        : "Unsaved search filter changes";

    unsavedChangesModal.componentInstance.message =
      autoUpdateSearch
        ? 'You have filter changes that have not been applied. ' +
        'To keep them, please cancel and click "Search". ' +
        '<br><br>Or to proceed without keeping them - click OK.'
        : 'You have unsaved changes to the search filters. ' +
        'To keep them, please cancel and click "Update Search". ' +
        '<br><br>Or to proceed without saving - click OK.';
    
    return unsavedChangesModal.result.then(
      () => {
        return true;
      },() => {
        return false;
      }
    );
  }

}
