import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {getJobExternalHref, isJob, Job} from "../../../../model/job";
import {NgbModal, NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {MainSidePanelBase} from "../../../util/split/MainSidePanelBase";
import {User} from "../../../../model/user";
import {AuthService} from "../../../../services/auth.service";
import {LocalStorageService} from "angular-2-local-storage";
import {SalesforceService} from "../../../../services/salesforce.service";
import {JobService} from "../../../../services/job.service";
import {SlackService} from "../../../../services/slack.service";
import {Location} from "@angular/common";
import {Router} from "@angular/router";
import {isStarredByMe} from "../../../../model/base";
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
import {
  CandidateSourceCandidateService
} from "../../../../services/candidate-source-candidate.service";
import {Opportunity} from "../../../../model/opportunity";

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
  @Output() jobUpdated = new EventEmitter<Job>();

  activeTabId: string;
  currentPrepItem: JobPrepItem;
  error: any;
  loading: boolean;
  loggedInUser: User;
  publishing: boolean;
  slacklink: string;

  private jobPrepJobSummary = new JobPrepJobSummary();
  private jobPrepJD = new JobPrepJD();
  private jobPrepSuggestedCandidates = new JobPrepSuggestedCandidates();

  jobPrepItems: JobPrepItem[] = [
    this.jobPrepJobSummary,
    this.jobPrepJD,
    new JobPrepJOI(),
    new JobPrepSuggestedSearches(),
    this.jobPrepSuggestedCandidates,
    new JobPrepDueDate(),
  ];

  private lastTabKey: string = 'JobLastTab';

  constructor(
    private authService: AuthService,
    private candidateSourceService: CandidateSourceCandidateService,
    private localStorageService: LocalStorageService,
    private jobService: JobService,
    private modalService: NgbModal,
    private salesforceService: SalesforceService,
    private slackService: SlackService,
    private location: Location,
    private router: Router
  ) {
    super(0,0, false)
  }

  ngOnInit(): void {
    this.loggedInUser = this.authService.getLoggedInUser();
    this.selectDefaultTab();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.job) {
      this.checkSubmissionListContents();
      this.jobPrepItems.forEach(j => j.job = this.job)
    }
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

  onTabChanged(event: NgbNavChangeEvent) {
    this.setActiveTabId(event.nextId);
  }

  private setActiveTabId(id: string) {
    this.activeTabId = id;
    this.localStorageService.set(this.lastTabKey, id);
  }

  publishJob() {
    //Reject if not enough info has been supplied about the job.
    if (!this.jobPrepJD.isCompleted() || !this.jobPrepJobSummary.isCompleted()) {
      const showReport = this.modalService.open(ConfirmationComponent, {
        centered: true, backdrop: 'static'});
      showReport.componentInstance.title = "More information needed about job";
      showReport.componentInstance.showCancel = false;
      let mess = "At the minimum you need to supply a job summary and job description document " +
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
    let mess = "Job has been updated to 'Candidate Search' if it wasn't already at that stage or " +
      "later. Also posted to Slack.";

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
    return isStarredByMe(this.job?.starringUsers, this.authService);
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
    return this.authService.canAccessSalesforce();
  }
}
