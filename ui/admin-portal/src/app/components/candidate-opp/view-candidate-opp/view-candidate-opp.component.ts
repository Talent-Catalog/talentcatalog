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

import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {
  CandidateOpportunity,
  isCandidateOpportunity,
  isOppStageGreaterThanOrEqualTo
} from "../../../model/candidate-opportunity";
import {EditCandidateOppComponent} from "../edit-candidate-opp/edit-candidate-opp.component";
import {CandidateOpportunityParams} from "../../../model/candidate";
import {NgbModal, NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {CandidateOpportunityService} from "../../../services/candidate-opportunity.service";
import {SalesforceService} from "../../../services/salesforce.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {getOpportunityStageName, Opportunity} from "../../../model/opportunity";
import {ShortSavedList} from "../../../model/saved-list";
import {CreateChatRequest, JobChat, JobChatType} from "../../../model/chat";
import {AuthenticationService} from "../../../services/authentication.service";
import {FileSelectorComponent} from "../../util/file-selector/file-selector.component";
import {ChatService} from "../../../services/chat.service";
import {forkJoin} from "rxjs";
import {LocalStorageService} from "../../../services/local-storage.service";

@Component({
  selector: 'app-view-candidate-opp',
  templateUrl: './view-candidate-opp.component.html',
  styleUrls: ['./view-candidate-opp.component.scss']
})
export class ViewCandidateOppComponent implements OnInit, OnChanges {
  @Input() opp: CandidateOpportunity;
  @Input() showBreadcrumb: boolean = true;
  @Input() fromUrl: boolean;
  @Output() candidateOppUpdated = new EventEmitter<CandidateOpportunity>();

  activeTabId: string;
  error: string;
  private lastTabKey: string = 'CaseLastTab';
  loading: boolean;
  updating: boolean;
  saving: boolean;
  candidateChats: JobChat[];
  nonCandidateChats: JobChat[];

  private candidateChat: JobChat;
  private candidateRecruitingChat: JobChat;
  private jobCreatorSourcePartnerChat: JobChat;
  private jobCreatorAllSourcePartnersChat: JobChat;
  private allJobCandidatesChat: JobChat;

  constructor(
    private authorizationService: AuthorizationService,
    private authenticationService: AuthenticationService,
    private candidateOpportunityService: CandidateOpportunityService,
    private chatService: ChatService,
    private localStorageService: LocalStorageService,
    private modalService: NgbModal,
    private salesforceService: SalesforceService,

  ) { }

  ngOnInit(): void {
    this.selectDefaultTab();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.opp) {
      this.fetchChats();
    }
  }

  private fetchChats() {
    const candidateProspectChatRequest: CreateChatRequest = {
      type: JobChatType.CandidateProspect,
      candidateId: this.opp?.candidate?.id,
    }
    const candidateRecruitingChatRequest: CreateChatRequest = {
      type: JobChatType.CandidateRecruiting,
      candidateId: this.opp?.candidate?.id,
      jobId: this.opp?.jobOpp?.id
    }
    const jobCreatorSourcePartnerChatRequest: CreateChatRequest = {
      type: JobChatType.JobCreatorSourcePartner,
      sourcePartnerId: this.opp?.candidate?.user?.partner?.id,
      jobId: this.opp?.jobOpp?.id
    }
    const jobCreatorAllSourcePartnersChatRequest: CreateChatRequest = {
      type: JobChatType.JobCreatorAllSourcePartners,
      jobId: this.opp?.jobOpp?.id
    }
    const allJobCandidatesChatRequest: CreateChatRequest = {
      type: JobChatType.AllJobCandidates,
      jobId: this.opp?.jobOpp?.id
    }

    this.loading = true;
    this.error = null;
    forkJoin( {
      'candidateChat': this.chatService.getOrCreate(candidateProspectChatRequest),
      'candidateRecruitingChat': this.chatService.getOrCreate(candidateRecruitingChatRequest),
      'jobCreatorSourcePartnerChat': this.chatService.getOrCreate(jobCreatorSourcePartnerChatRequest),
      'jobCreatorAllSourcePartnersChat': this.chatService.getOrCreate(jobCreatorAllSourcePartnersChatRequest),
      'allJobCandidatesChat': this.chatService.getOrCreate(allJobCandidatesChatRequest),
    }).subscribe(
      results => {
        this.loading = false;

        this.candidateChat = results['candidateChat'];
        this.candidateRecruitingChat = results['candidateRecruitingChat'];
        this.jobCreatorSourcePartnerChat = results['jobCreatorSourcePartnerChat'];
        this.jobCreatorAllSourcePartnersChat = results['jobCreatorAllSourcePartnersChat'];
        this.allJobCandidatesChat = results['allJobCandidatesChat'];
        this.checkVisibility();
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  get getCandidateOpportunityStageName() {
    return getOpportunityStageName
  }

  get editable(): boolean {
    return this.authorizationService.canEditCandidateOpp(this.opp);
  }

  get JobChatType() {
    return JobChatType;
  }

  editOppProgress() {
    const editQuery = this.modalService.open(EditCandidateOppComponent, {size: 'lg'});
    editQuery.componentInstance.opp = this.opp;

    //Progress parameters (stage, nextStep) are set separately in this component
    editQuery.componentInstance.showProgressParams = false;

    editQuery.result
    .then((info: CandidateOpportunityParams) => {this.doUpdate(info);})
    .catch(() => { });
  }

  private doUpdate(info: CandidateOpportunityParams) {
    this.updating = true;
    this.candidateOpportunityService.updateCandidateOpportunity(this.opp.id, info)
    .subscribe(opp => {
        //Emit an opp updated which will refresh the display
        this.candidateOppUpdated.emit(opp);
        this.updating = false;
      },
      err => {this.error = err; this.updating = false; }
    );

  }

  getOppSfLink(sfId: string): string {
    return this.salesforceService.sfOppToLink(sfId);
  }

  canAccessSalesforce(): boolean {
    return this.authorizationService.canAccessSalesforce();
  }

  displaySavedList(list: ShortSavedList) {
    return list ? list.name + "(" + list.id + ")" : "";
  }

  onOppProgressUpdated(opp: Opportunity) {
    if (isCandidateOpportunity(opp)) {
      this.checkVisibility();
      this.candidateOppUpdated.emit(opp);
    }
  }

  private selectDefaultTab() {
    const defaultActiveTabID: string = this.localStorageService.get(this.lastTabKey);
    this.activeTabId = defaultActiveTabID;
  }

  onTabChanged(event: NgbNavChangeEvent) {
    this.setActiveTabId(event.nextId);
  }

  private setActiveTabId(id: string) {
    this.activeTabId = id;
    this.localStorageService.set(this.lastTabKey, id);
  }

  private checkVisibility() {
    const candidatePartner = this.opp?.candidate?.user?.partner;
    const jobCreator = this.opp?.jobOpp.jobCreator;
    const loggedInPartner = this.authenticationService.getLoggedInUser().partner;

    //User is recruiter for this opp or default job creator
    const userIsJobCreator =
      loggedInPartner.defaultJobCreator || loggedInPartner.id == jobCreator?.id;

    //User is source partner responsible for candidate or default source partner
    const userIsCandidatePartner =
      loggedInPartner.defaultSourcePartner || loggedInPartner.id == candidatePartner?.id;

    this.candidateChats = [];
    this.nonCandidateChats = [];

    if (userIsCandidatePartner) {
      this.candidateChats = [this.candidateChat, this.candidateRecruitingChat, this.allJobCandidatesChat];
      this.nonCandidateChats = [this.jobCreatorSourcePartnerChat, this.jobCreatorAllSourcePartnersChat];
    } else if (userIsJobCreator) {
      this.nonCandidateChats = [this.jobCreatorSourcePartnerChat, this.jobCreatorAllSourcePartnersChat];
      if (this.cvReviewStageOrMore()) {
        this.candidateChats = [this.candidateRecruitingChat, this.allJobCandidatesChat];
      } else {
        this.candidateChats = [];
      }
    }
  }
  uploadOffer() {
      const fileSelectorModal = this.modalService.open(FileSelectorComponent, {
        centered: true,
        backdrop: 'static'
      })

      fileSelectorModal.componentInstance.maxFiles = 1;
      fileSelectorModal.componentInstance.closeButtonLabel = "Upload";
      fileSelectorModal.componentInstance.title = "Select the candidate's job offer contract";

      fileSelectorModal.result
      .then((selectedFiles: File[]) => {
        if (selectedFiles.length > 0) {
          this.doUpload(selectedFiles[0]);
        }
      })
      .catch(() => {});
  }

  private doUpload(file: File) {
    const formData: FormData = new FormData();
    formData.append('file', file);

    this.error = null;
    this.saving = true;
    this.candidateOpportunityService.uploadOffer(this.opp.id, formData).subscribe(
      opp => {
        //Need event to bubble up and change job
        this.candidateOppUpdated.emit(opp)
        this.saving = false;
      },
      (error) => {
        this.error = error
        this.saving = false;
      }
    );
  }

  /**
   *  Recruiters only see candidates past the CV Review stage.
   */
  cvReviewStageOrMore() {
    return isOppStageGreaterThanOrEqualTo(this.opp?.stage, 'cvReview')
  }

  hasVisibleCandidateChats(): boolean {
    return this.candidateChats && this.candidateChats.length > 0;
  }

  hasVisibleNonCandidateChats() {
    return this.nonCandidateChats && this.nonCandidateChats.length > 0;
  }
}
