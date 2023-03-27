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

import {Component, OnInit} from '@angular/core';
import {CandidateService} from '../../../services/candidate.service';
import {
  Candidate,
  UpdateCandidateStatusInfo,
  UpdateCandidateStatusRequest
} from '../../../model/candidate';
import {ActivatedRoute, Router} from '@angular/router';
import {NgbModal, NgbNavChangeEvent} from '@ng-bootstrap/ng-bootstrap';
import {DeleteCandidateComponent} from './delete/delete-candidate.component';
import {EditCandidateStatusComponent} from './status/edit-candidate-status.component';
import {Title} from '@angular/platform-browser';
import {AuthService} from '../../../services/auth.service';
import {User} from '../../../model/user';
import {IHasSetOfCandidates, SavedList, SearchSavedListRequest} from '../../../model/saved-list';
import {SavedListService} from '../../../services/saved-list.service';
import {CandidateSavedListService} from '../../../services/candidate-saved-list.service';
import {SavedListCandidateService} from '../../../services/saved-list-candidate.service';
import {forkJoin} from 'rxjs';
import {CandidateAttachmentService} from '../../../services/candidate-attachment.service';
import {CandidateAttachment} from '../../../model/candidate-attachment';
import {FormBuilder, FormGroup} from '@angular/forms';
import {environment} from '../../../../environments/environment';
import {LocalStorageService} from 'angular-2-local-storage';
import {CreateUpdateListComponent} from '../../list/create-update/create-update-list.component';
import {CandidateFieldService} from "../../../services/candidate-field.service";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {DownloadCvComponent} from "../../util/download-cv/download-cv.component";
import {MainSidePanelBase} from "../../util/split/MainSidePanelBase";

@Component({
  selector: 'app-view-candidate',
  templateUrl: './view-candidate.component.html',
  styleUrls: ['./view-candidate.component.scss']
})
export class ViewCandidateComponent extends MainSidePanelBase implements OnInit {

  private lastTabKey: string = 'CandidateLastTab';

  activeTabId: string;
  loading: boolean;
  savingList: boolean;
  loadingError: boolean;
  selectDropdownText: boolean = true;
  error;
  candidate: Candidate;
  loggedInUser: User;

  selectedLists: SavedList[] = [];
  lists: SavedList[] = [];
  attachmentForm: FormGroup;
  attachments: CandidateAttachment[];
  cvs: CandidateAttachment[];
  s3BucketUrl = environment.s3BucketUrl;
  token: string;

  constructor(private candidateService: CandidateService,
              private savedListService: SavedListService,
              private candidateSavedListService: CandidateSavedListService,
              private localStorageService: LocalStorageService,
              private savedListCandidateService: SavedListCandidateService,
              private candidateAttachmentService: CandidateAttachmentService,
              private route: ActivatedRoute,
              private router: Router,
              private modalService: NgbModal,
              private titleService: Title,
              private authService: AuthService,
              private candidateFieldService: CandidateFieldService,
              private fb: FormBuilder) {
    super(2, 4);
  }

  ngOnInit() {
    this.refreshCandidateInfo();
    this.loggedInUser = this.authService.getLoggedInUser();
    this.selectDefaultTab();
  }

  refreshCandidateInfo() {
    this.loadingError = false;
    this.route.paramMap.subscribe(params => {
      const candidateNumber = params.get('candidateNumber');
      this.loading = true;
      this.error = null;
      this.loadingError = false;
      this.candidateService.getByNumber(candidateNumber).subscribe(candidate => {
        if (candidate == null) {
          this.loadingError = true;
          this.error = 'There is no candidate with number: ' + params.get('candidateNumber');
          this.loading = false;
        } else {
          this.setCandidate(candidate);
          this.loadLists();
          this.generateToken();
        }
      }, error => {
        this.loadingError = true;
        this.error = error;
        this.loading = false;
      });
    });
  }

  private loadLists() {
    /*load all our non fixed lists */
    this.loading = true;
    const request: SearchSavedListRequest = {
      owned: true,
      shared: true,
      global: true,
      fixed: false
    };

    forkJoin( {
      'lists': this.savedListService.search(request),
      'selectedLists': this.candidateSavedListService.search(this.candidate.id, request)
    }).subscribe(
      results => {
        this.loading = false;
        this.lists = results['lists'];
        this.selectedLists = results['selectedLists'];
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  deleteCandidate() {
    const modal = this.modalService.open(DeleteCandidateComponent);
    modal.componentInstance.candidate = this.candidate;
    modal.result.then(result => {
      this.router.navigate(['/']);
    });
  }

  editCandidate() {
    const modal = this.modalService.open(EditCandidateStatusComponent);

    //Initialize status with candidate's current status
    modal.componentInstance.candidateStatus = this.candidate.status;

    modal.result
      .then((info: UpdateCandidateStatusInfo) => {
        this.updateCandidateStatus(info);
      } )
      .catch(() => { /* Isn't possible */ });
  }

  private updateCandidateStatus(info: UpdateCandidateStatusInfo) {
    this.error = null;
    this.loading = true;
    const request: UpdateCandidateStatusRequest = {
      candidateIds: [this.candidate.id],
      info: info
    };
    this.candidateService.updateStatus(request).subscribe(
      () => {
        this.loading = false;
        //Update candidate with new status
        this.candidate.status = info.status;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

  setCandidate(value: Candidate) {
    this.candidate = value;
    if (this.candidate.user.firstName && this.candidate.user.lastName) {
      this.titleService.setTitle(this.candidate.user.firstName + ' '
        + this.candidate.user.lastName + ' ' + this.candidate.candidateNumber);
    } else {
      this.titleService.setTitle(this.candidate.candidateNumber);
    }
  }

  downloadCV() {
    // Modal
    const downloadCVModal = this.modalService.open(DownloadCvComponent, {
      centered: true,
      backdrop: 'static'
    });

    downloadCVModal.componentInstance.candidateId = this.candidate.id;

    downloadCVModal.result
      .then((result) => {
      })
      .catch(() => { /* Isn't possible */ });
  }

  private selectDefaultTab() {
    const defaultActiveTabID: string = this.localStorageService.get(this.lastTabKey);
    this.activeTabId = defaultActiveTabID;
  }

  onTabChanged(event: NgbNavChangeEvent) {
    this.setActiveTabId(event.nextId);
  }

  publicCvUrl() {
    //todo use document.location.origin - see url.ts
    return 'https://tctalent.org/public-portal/cv/' + this.token;
  }

  private setActiveTabId(id: string) {
    this.activeTabId = id;
    this.localStorageService.set(this.lastTabKey, id);
  }

  onCandidateChanged() {
    this.refreshCandidateInfo();
  }

  isCVViewable(): boolean {
    return this.authService.canViewCandidateCV();
  }

  isAnAdmin(): boolean {
    return this.authService.isAnAdmin();
  }

  /*
    Methods for ng-select list selection
   */
  onItemSelect($event) {
    const savedListId: number = +$event.id;
    this.addCandidateToList(savedListId, false);
  }

  onItemDeSelect($event: SavedList) {
    const savedListId: number = +$event.id;
    const deleteCandidateListModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteCandidateListModal.componentInstance.message =
      'Are you sure you want to remove ' + this.candidate.user.firstName + ' ' + this.candidate.user.lastName +
      ' from the list ' + $event.name + ' ?';

    deleteCandidateListModal.result
      .then((result) => {
        this.removeCandidateFromList(savedListId);
      })
      .catch(() => { /* Isn't possible */ });

  }

  compareLists = (item, selected) => {
    return item.id === selected.id;
  };

  onNewList() {
    const modal = this.modalService.open(CreateUpdateListComponent);
    modal.result
      .then((savedList: SavedList) => {
        this.addCandidateToList(savedList.id, true);
      })
      .catch(() => { /* Isn't possible */
      });
  }

  private addCandidateToList(savedListId: number, reload: boolean) {
    this.savingList = true;
    const request: IHasSetOfCandidates = {
      candidateIds: [this.candidate.id]
    };
    this.savedListCandidateService.merge(savedListId, request).subscribe(
      () => {
        this.savingList = false;
        if (reload) {
          this.loadLists();
        }
      },
      (error) => {
        this.savingList = false;
        this.error = error;
      }
    );
  }

  private setCandidateLists(lists: SavedList[]) {
    this.savingList = true;
    const ids: number[] = [];
    if (lists !== null && lists.length > 0) {
      for (const savedList of lists) {
        ids.push(savedList.id);
      }
    }
    this.candidateSavedListService.replace(this.candidate.id,
      {savedListIds: ids})
      .subscribe(
        () => {
          this.savingList = false;
        },
        (error) => {
          this.error = error;
          this.savingList = false;
        }
      );
  }

  private removeCandidateFromList(savedListId: number) {
    this.savingList = true;
    const request: IHasSetOfCandidates = {
      candidateIds: [this.candidate.id]
    };
    this.savedListCandidateService.remove(savedListId, request).subscribe(
      () => {
        this.selectedLists = this.selectedLists.filter(list => list.id !== savedListId)
        this.savingList = false;
      },
      (error) => {
        this.savingList = false;
        this.error = error;
      })
  }

  generateToken() {
    this.candidateService.generateToken(this.candidate?.candidateNumber).subscribe(
      (result) => {
        this.token = result;
      },
      (error) => {
        this.error = error;
      }
    )
  }

  isEditable(): boolean {
    return this.authService.isEditableCandidate(this.candidate);
  }

  canViewPrivateInfo() {
    return this.authService.canViewPrivateCandidateInfo(this.candidate);
  }
}
