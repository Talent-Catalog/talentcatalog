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

import {Component, OnDestroy, OnInit} from '@angular/core';
import {
  Candidate,
  UpdateCandidateNotificationPreferenceRequest,
  UpdateCandidateStatusInfo,
  UpdateCandidateStatusRequest
} from '../../../model/candidate';
import {CandidateService, DownloadCVRequest} from '../../../services/candidate.service';
import {ActivatedRoute, Router} from '@angular/router';
import {NgbModal, NgbNavChangeEvent} from '@ng-bootstrap/ng-bootstrap';
import {DeleteCandidateComponent} from './delete/delete-candidate.component';
import {EditCandidateStatusComponent} from './status/edit-candidate-status.component';
import {Title} from '@angular/platform-browser';
import {AuthorizationService} from '../../../services/authorization.service';
import {User} from '../../../model/user';
import {IHasSetOfCandidates, SavedList, SearchSavedListRequest} from '../../../model/saved-list';
import {SavedListService} from '../../../services/saved-list.service';
import {CandidateSavedListService} from '../../../services/candidate-saved-list.service';
import {SavedListCandidateService} from '../../../services/saved-list-candidate.service';
import {forkJoin, Subject} from 'rxjs';
import {CreateUpdateListComponent} from '../../list/create-update/create-update-list.component';
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {DownloadCvComponent} from "../../util/download-cv/download-cv.component";
import {MainSidePanelBase} from "../../util/split/MainSidePanelBase";
import {TailoredCvComponent} from 'src/app/components/candidates/view/tailored-cv.component';
import {AuthenticationService} from "../../../services/authentication.service";
import {CreateChatRequest, JobChat, JobChatType} from "../../../model/chat";
import {ChatService} from "../../../services/chat.service";
import {DtoType} from "../../../model/base";
import {LocalStorageService} from "../../../services/local-storage.service";
import {concatMap, takeUntil} from "rxjs/operators";


@Component({
  selector: 'app-view-candidate',
  templateUrl: './view-candidate.component.html',
  styleUrls: ['./view-candidate.component.scss']
})
export class ViewCandidateComponent extends MainSidePanelBase implements OnInit, OnDestroy {

  private lastTabKey: string = 'CandidateLastTab';

  activeTabId: string;
  loading: boolean;
  loadingButton: boolean;
  savingList: boolean;
  loadingError: boolean;
  error;
  candidate: Candidate;
  candidateChat: JobChat;
  candidateProspectTabVisible: boolean;
  loggedInUser: User;

  selectedLists: SavedList[] = [];
  lists: SavedList[] = [];
  token: string;

  private destroy$ = new Subject<void>();

  constructor(private candidateService: CandidateService,
              private chatService: ChatService,
              private savedListService: SavedListService,
              private candidateSavedListService: CandidateSavedListService,
              private localStorageService: LocalStorageService,
              private savedListCandidateService: SavedListCandidateService,
              private route: ActivatedRoute,
              private router: Router,
              private modalService: NgbModal,
              private titleService: Title,
              private authorizationService: AuthorizationService,
              private authenticationService: AuthenticationService) {
    super(2, 4);
  }

  ngOnInit() {
    this.refreshCandidateProfile();
    this.loggedInUser = this.authenticationService.getLoggedInUser();
    this.selectDefaultTab();
    this.candidateService.candidateUpdated().pipe(
      takeUntil(this.destroy$),
      concatMap(() => {
        return this.candidateService.getByNumber(this.candidate.candidateNumber)
      })
      ).subscribe(candidate => {
      // We aren't able to merge two candidate objects (e.g. using Spread operator like in the candidate-search-card)
      // if a value is changed to null. Null values aren't returned via the updated object DTO and therefore there is nothing
      // to replace the old value with the new null value. So in this case, it is best to fetch the new object.
      this.candidate = candidate;
    })
  }

  private setChatAccess() {
    const candidatePartner = this.candidate.user?.partner;
    const loggedInPartner = this.authenticationService.getLoggedInUser().partner;

    //User is source partner responsible for candidate or default source partner
    const userIsCandidatePartner =
      loggedInPartner.defaultSourcePartner || loggedInPartner.id == candidatePartner?.id;

    this.candidateProspectTabVisible = userIsCandidatePartner;

    // May return null, in which case 'Create Chat' button displayed instead of chat
    if (this.candidateProspectTabVisible) {
      this.chatService.getCandidateProspectChat(this.candidate.id).subscribe(result => {
        this.candidateChat = result;
      })
    }
  }

  refreshCandidateProfile() {
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
          this.setChatAccess();
        }
      }, error => {
        this.loadingError = true;
        this.error = error;
        this.loading = false;
      });
    });
  }

  get JobChatType() {
    return JobChatType;
  }

  private loadLists() {
    /*load all our non-fixed lists */
    this.loading = true;
    const request: SearchSavedListRequest = {
      dtoType: DtoType.MINIMAL, //We just need the names and ids of the lists
      owned: true,
      shared: true,
      global: this.canSeeGlobalLists(),
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

  createChat() {
    this.loadingButton = true;
    this.error = null;

    const candidateProspectChatRequest: CreateChatRequest = {
      type: JobChatType.CandidateProspect,
      candidateId: this.candidate.id,
    }
    this.chatService.create(candidateProspectChatRequest).subscribe({
        next: (chat) => {this.candidateChat = chat; this.loadingButton = false},
        error: (error) => {this.error = error; this.loadingButton = false}
      }
    )
  }

  /**
   * Very similar to ShowCandidatesComponent.downloadGeneratedCV.
   * Opens {@link DownloadCvComponent} modal that returns CV generated from candidate profile.
   */
  downloadGeneratedCV() {
    if (this.canViewCandidateName()) {
      // Modal
      const downloadCVModal = this.modalService.open(DownloadCvComponent, {
        centered: true,
        backdrop: 'static'
      });

      downloadCVModal.componentInstance.candidateId = this.candidate.id;

      downloadCVModal.result
      .then((result) => {
      })
      .catch(() => { /* Isn't possible */
      });
    } else {
      // No modal giving option to view name and contact details - straight to anonymised DL
      const request: DownloadCVRequest = {
        candidateId: this.candidate.id,
        showName: false,
        showContact: false
      }
      const tab = window.open();
      this.candidateService.downloadCv(request).subscribe(
        result => {
          tab.location.href = URL.createObjectURL(result);
        },
        error => {
          this.error = error;
        }
      );
    }
  }

  private selectDefaultTab() {
    const defaultActiveTabID: string = this.localStorageService.get(this.lastTabKey);
    this.activeTabId = defaultActiveTabID;
  }

  onTabChanged(event: NgbNavChangeEvent) {
    this.setActiveTabId(event.nextId);
  }

  publicCvUrl() {
    const isDevSetup = document.location.port == '4201';
    let origin = document.location.hostname;
    let path = '/public-portal/cv/';
    let protocol = document.location.protocol;
    if (isDevSetup) {
      origin = `${document.location.hostname}:4202`;
      path = '/cv/'
    }
    return `${protocol}//${origin}${path}${this.token}`;
  }

  private setActiveTabId(id: string) {
    this.activeTabId = id;
    this.localStorageService.set(this.lastTabKey, id);
  }

  isCVViewable(): boolean {
    return this.authorizationService.canViewCandidateCV();
  }

  isAnAdmin(): boolean {
    return this.authorizationService.isAnAdmin();
  }

  onMarkCandidateChatAsRead() {
    if (this.candidateChat) {
      this.chatService.markChatAsRead(this.candidateChat);
    }
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
    this.candidateService.generateToken(this.candidate?.candidateNumber, false, []).subscribe(
      (result) => {
        this.token = result;
      },
      (error) => {
        this.error = error;
      }
    )
  }

  isEditable(): boolean {
    return this.authorizationService.isEditableCandidate(this.candidate);
  }

  canViewPrivateInfo() {
    return this.authorizationService.canViewPrivateCandidateInfo(this.candidate);
  }

  canAccessSalesforce(): boolean {
    return this.authorizationService.canAccessSalesforce();
  }

  canAccessGoogleDrive(): boolean {
    return this.authorizationService.canAccessGoogleDrive();
  }

  createTailoredCv() {
    const createTailoredCvModal = this.modalService.open(TailoredCvComponent, {
      centered: true,
      backdrop: 'static'
    });

    createTailoredCvModal.componentInstance.candidateId = this.candidate?.id;
    createTailoredCvModal.componentInstance.candidateNumber = this.candidate?.candidateNumber;

  }

  isReadOnlyUser() {
    return this.authorizationService.isReadOnly();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private canSeeGlobalLists() {
    return this.authorizationService.canSeeGlobalLists();
  }

  public canSeeJobDetails() {
    return this.authorizationService.canSeeJobDetails()
  }

  public canViewCandidateName() {
    return this.authorizationService.canViewCandidateName();
  }

  public computeNotificationButtonLabel() {
    return "Notification " + (this.candidate?.allNotifications ? "Opt Out": "Opt In");
  }

  public toggleNotificationPreferences() {
    //Ask user if they are sure. Normally this should be changed by candidate only.
    const areYouSure = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    })

    areYouSure.componentInstance.title = "Override candidate's chat notification preferences?";
    areYouSure.componentInstance.message =
      "The candidate can opt into receiving detailed chat notifications. " +
      " The default is that they don't - but normally we should respect" +
      " whatever decision they have made.";

    areYouSure.result
    .then((result) => {
      if (result == true) {
        this.doToggleNotificationPreferences()
      }
    })
    .catch(() => {});
  }

  private doToggleNotificationPreferences() {
    this.error = null;
    const request: UpdateCandidateNotificationPreferenceRequest = {
      allNotifications: !this.candidate.allNotifications
    };
    this.candidateService.updateNotificationPreference(this.candidate.id, request).subscribe(
      () => {
        //Update candidate with new preference
        this.candidate.allNotifications = request.allNotifications;

        //Refresh to get new candidate notes.
        this.refreshCandidateProfile();
      },
      (error) => {
        this.error = error;
      });
  }
}
