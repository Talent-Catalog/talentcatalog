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
import {getJobExternalHref, isJob, Job} from "../../../../model/job";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {MainSidePanelBase} from "../../../util/split/MainSidePanelBase";
import {User} from "../../../../model/user";
import {AuthorizationService} from "../../../../services/authorization.service";
import {SalesforceService} from "../../../../services/salesforce.service";
import {JobService} from "../../../../services/job.service";
import {SlackService} from "../../../../services/slack.service";
import {Location} from "@angular/common";
import {Router} from "@angular/router";
import {
  JobPrepDueDate,
  JobPrepItem,
  JobPrepJD,
  JobPrepJobSummary,
  JobPrepJOI,
  JobPrepSuggestedCandidates,
  JobPrepSuggestedSearches
} from "../../../../model/job-prep-item";
import {ConfirmationComponent} from "../../../util/confirm/confirmation.component";
import {CandidateSourceCandidateService} from "../../../../services/candidate-source-candidate.service";
import {Opportunity} from "../../../../model/opportunity";
import {AuthenticationService} from "../../../../services/authentication.service";
import {forkJoin, Observable} from "rxjs";
import {CreateChatRequest, JobChat, JobChatType} from "../../../../model/chat";
import {ChatService} from "../../../../services/chat.service";
import {PartnerService} from "../../../../services/partner.service";
import {Partner} from "../../../../model/partner";
import {JobOppIntake} from "../../../../model/job-opp-intake";
import {LocalStorageService} from "../../../../services/local-storage.service";

/**
 * Display details of a job object passed in as an @Input.
 */
@Component({
  selector: 'app-view-job',
  templateUrl: './view-job.component.html',
  styleUrls: ['./view-job.component.scss']
})
export class ViewJobComponent extends MainSidePanelBase implements OnInit, OnChanges {
  @Input() job: Job;

  /**
   * When showBreadcrumb is false, we display abbreviated information, suitable for showing in the
   * detail display of a selected job
   */
  @Input() showBreadcrumb: boolean = true;

  /**
   * True if the view job comes from the viewJobFromUrl component, false if the job comes from the jobsWithDetail component.
   * Depending where it comes from will depending how the chat view appears (as chat is much smaller on side panel).
   */
  @Input() fromUrl: boolean;

  @Output() jobUpdated = new EventEmitter<Job>();

  activeTabId: string;
  chatReadStatus$: Observable<boolean>;
  currentPrepItem: JobPrepItem;
  error: any;
  loading: boolean;
  groupChats: JobChat[];
  partnerChats: JobChat[];
  loggedInUser: User;
  publishing: boolean;
  slacklink: string;

  private jobPrepJobSummary = new JobPrepJobSummary();
  private jobPrepJD = new JobPrepJD();
  private jobPrepJOI = new JobPrepJOI();
  private jobPrepSuggestedCandidates = new JobPrepSuggestedCandidates();

  jobPrepItems: JobPrepItem[] = [
    this.jobPrepJobSummary,
    this.jobPrepJD,
    this.jobPrepJOI,
    new JobPrepSuggestedSearches(),
    this.jobPrepSuggestedCandidates,
    new JobPrepDueDate(),
  ];

  private lastTabKey: string = 'JobLastTab';

  constructor(
    private authorizationService: AuthorizationService,
    private authenticationService: AuthenticationService,
    private candidateSourceService: CandidateSourceCandidateService,
    private chatService: ChatService,
    private localStorageService: LocalStorageService,
    private jobService: JobService,
    private modalService: NgbModal,
    private partnerService: PartnerService,
    private salesforceService: SalesforceService,
    private slackService: SlackService,
    private location: Location,
    private router: Router
  ) {
    super(0,0, false)
  }

  ngOnInit(): void {
    this.selectDefaultTab();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.job) {
      this.loggedInUser = this.authenticationService.getLoggedInUser();
      this.checkSubmissionListContents();
      this.jobPrepItems.forEach(j => j.job = this.job);
      this.fetchGroupChats();
      if (this.authorizationService.isSourcePartner() &&
        !this.authorizationService.isDefaultSourcePartner()) {
        //There is only one partner chat to be fetched - my one.
        let partner = this.loggedInUser.partner;
        if (partner) {
          this.fetchChats([partner]);
        }
      } else {
        this.fetchPartnerChats();
      }
    }
  }

  /**
   * Job is editable only by the user who created it or the contact user.
   * Also by a non read only user working for the default job creator.
   */
  isEditable(): boolean {
    return this.authorizationService.isJobMine(this.job) ||
      (this.authorizationService.isDefaultJobCreator() && !this.authorizationService.isReadOnly());
  }

  private fetchGroupChats() {
    const allCandidatesChatRequest: CreateChatRequest = {
      type: JobChatType.AllJobCandidates,
      jobId: this.job?.id
    }
    const allSourcePartnersChatRequest: CreateChatRequest = {
      type: JobChatType.JobCreatorAllSourcePartners,
      jobId: this.job?.id
    }

    this.loading = true;
    this.error = null;
    forkJoin( {
      'allJobCandidatesChat': this.chatService.getOrCreate(allCandidatesChatRequest),
      'allSourcePartnersChat': this.chatService.getOrCreate(allSourcePartnersChatRequest),
    }).subscribe(
      results => {
        this.loading = false;
        const allJobCandidatesChat = results['allJobCandidatesChat'];
        const allSourcePartnersChat = results['allSourcePartnersChat'];
        this.groupChats = [allJobCandidatesChat, allSourcePartnersChat];
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  private fetchPartnerChats() {
    this.partnerService.listSourcePartners(this.job).subscribe(
    (sourcePartners) => {this.fetchChats(sourcePartners); this.loading = false},
    (error) => {this.error = error; this.loading = false}
     )
  }

  private fetchChats(sourcePartners: Partner[]) {

    //Map sourcePartners array to array of their corresponding chats for this job
    let jobChatObservables: Observable<JobChat>[] =
      sourcePartners.map((partner) => {
           const request: CreateChatRequest = {
              type: JobChatType.JobCreatorSourcePartner,
              jobId: this.job?.id,
              sourcePartnerId: partner.id
            }
            return this.chatService.getOrCreate(request);
      } )

    //Now fetch all those source partner chats
    this.loading = true;
    this.error = null;
    forkJoin(jobChatObservables).subscribe(
      (jobChats) => {this.partnerChats = jobChats; this.loading = false;},
      (error) => {
        this.error = error;
        this.loading = false;
      }
    )
  }

  get visible(): boolean {
    const loggedInUser = this.authenticationService.getLoggedInUser();
    let visible = false;
    if (this.authorizationService.isJobCreatorPartner()) {
      //Only the actual job creator (and the default job creator) see this chat
      visible = this.authorizationService.isDefaultJobCreator() ? true :
                this.job.jobCreator.id == loggedInUser.partner.id;
    } else if (this.authorizationService.isSourcePartner()) {
      //All source partners see the chat
      visible = true;
    }
    return visible;
  }

  private checkSubmissionListContents() {
    const submissionList = this.job?.submissionList;
    if (submissionList == null) {
      this.jobPrepSuggestedCandidates.empty = true;
    } else {
      this.candidateSourceService.isEmpty(submissionList).subscribe(
        (empty) => this.jobPrepSuggestedCandidates.empty = empty,
        (error) => this.error = error
      )
    }
  }

  private selectDefaultTab() {
    const defaultActiveTabID: string = this.localStorageService.get(this.lastTabKey);
    this.activeTabId = defaultActiveTabID;
  }

  onTabChanged(activeTabId: string) {
    this.setActiveTabId(activeTabId);
  }

  private setActiveTabId(id: string) {
    this.activeTabId = id;
    this.localStorageService.set(this.lastTabKey, id);
  }

  publishJob() {
    //Reject if not enough info has been supplied about the job.
    if (!this.jobPrepJOI.isCompleted() || !this.jobPrepJD.isCompleted() || !this.jobPrepJobSummary.isCompleted()) {
      const showReport = this.modalService.open(ConfirmationComponent, {
        centered: true, backdrop: 'static'});
      showReport.componentInstance.title = "More information needed about job";
      showReport.componentInstance.showCancel = false;
      let mess = "At the minimum you need to supply a job summary, job description document " +
        "and job opportunity intake " +
        "before publishing a job. Otherwise our source colleagues won't have enough information to " +
        "work with. Please consider providing more information than the absolute minimum " +
        "if you have it, as suggested in the preparation items. " +
        "The more information source has about the job, the better the results for employer and " +
        "candidates, and the less work for our source staff.";

      showReport.componentInstance.message = mess;

    } else {
      this.error = null;
      this.publishing = true;
      this.jobService.publishJob(this.job.id).subscribe(
        (job) => {this.fireJobEventAndPostOnSlack(job)},
        (error) => {this.error = error; this.publishing = false}
      )
    }
  }

  onJobUpdated(job: Job) {
    this.jobUpdated.emit(job);
  }

  onOppProgressUpdated(opp: Opportunity) {
    if (isJob(opp)) {
      this.onJobUpdated(opp)
    }
  }

  getSalesforceJobLink(sfId: string): string {
    return this.salesforceService.sfOppToLink(sfId);
  }

  private fireJobEventAndPostOnSlack(job: Job) {

    //Fire the job update event
    this.onJobUpdated(job);

    //Post on Slack
    this.slackService.postJobFromId(
      job.id, getJobExternalHref(this.router, this.location, job)).subscribe(
      (response) => {
        this.slacklink = response.slackChannelUrl;
        this.publishing = false;
        this.displayPublicationReport();
      },
      (error) => {this.error = error; this.publishing = false});
  }

  private displayPublicationReport() {
    const showReport = this.modalService.open(ConfirmationComponent, {
      centered: true, backdrop: 'static'});
    showReport.componentInstance.title = "Published job: " + this.job.name;
    showReport.componentInstance.showCancel = false;
    let mess =
      "Job has been updated to 'Candidate Search' if it wasn't already at that stage or later.";

    if (this.authorizationService.isDefaultJobCreator()) {
      mess += " Also posted to Slack."
    }

    showReport.componentInstance.message = mess;
  }

  doToggleStarred() {
    this.loading = true;
    this.error = null
    this.jobService.updateStarred(this.job.id, !this.isStarred()).subscribe(
      (job: Job) => {this.job = job; this.loading = false},
      (error) => {this.error = error; this.loading = false}
    )
  }

  isStarred(): boolean {
    return this.authorizationService.isStarredByMe(this.job?.starringUsers);
  }

  onPrepItemSelected(item: JobPrepItem) {
    if (item.tabId) {
      this.setActiveTabId(item.tabId);
    }
    this.currentPrepItem = item;
  }

  currentPrepItemIsSummary(): boolean {
    return this.currentPrepItem instanceof JobPrepJobSummary;
  }

  canAccessSalesforce() {
    return this.authorizationService.canAccessSalesforce();
  }

  onChatReadStatusCreated(chatReadStatus$: Observable<boolean>) {
    this.chatReadStatus$ = chatReadStatus$;
  }

  /**
   * If intake has changed, update the job with the updated intake.
   * This will trigger the logic to run which checks whether the intake is complete (JobPrepJOI).
   * @param joi Updated intake
   */
  onIntakeChanged(joi: JobOppIntake) {
    this.job.jobOppIntake = joi;
  }

  public canSeeJobDetails() {
    return this.authorizationService.canSeeJobDetails()
  }

}
