import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Job} from "../../../../model/job";
import {NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {MainSidePanelBase} from "../../../util/split/MainSidePanelBase";
import {User} from "../../../../model/user";
import {AuthService} from "../../../../services/auth.service";
import {LocalStorageService} from "angular-2-local-storage";
import {SalesforceService} from "../../../../services/salesforce.service";
import {JobService} from "../../../../services/job.service";
import {SlackService} from "../../../../services/slack.service";
import {Location} from "@angular/common";
import {Router} from "@angular/router";

@Component({
  selector: 'app-view-job',
  templateUrl: './view-job.component.html',
  styleUrls: ['./view-job.component.scss']
})
export class ViewJobComponent extends MainSidePanelBase implements OnInit {
  @Input() job: Job;
  @Output() jobUpdated = new EventEmitter<Job>();

  activeTabId: string;
  error: any;
  loggedInUser: User;
  publishing: boolean;
  slacklink: string;

  private lastTabKey: string = 'JobLastTab';

  constructor(
    private authService: AuthService,
    private localStorageService: LocalStorageService,
    private jobService: JobService,
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
    //todo checks - are you sure?
    this.error = null;
    this.publishing = true;
    this.jobService.publishJob(this.job.id).subscribe(
      (job) => {this.updateJobAndPostOnSlack(job)},
      (error) => {this.error = error; this.publishing = false}
    )
  }

  onJobUpdated(job: Job) {
    this.jobUpdated.emit(job);
  }

  getSalesforceJobLink(sfId: string): string {
    return this.salesforceService.sfOppToLink(sfId);
  }

  private updateJobAndPostOnSlack(job: Job) {

    this.onJobUpdated(job);

    this.slackService.postJobFromId(job.id).subscribe(
      (response) => {
        this.slacklink = response.slackChannelUrl;
        this.publishing = false;
      },
      (error) => {this.error = error; this.publishing = false});
  }
}
