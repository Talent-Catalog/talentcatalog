import {Component, OnInit} from '@angular/core';
import {JoblinkValidationEvent} from "../../util/joblink/joblink.component";
import {SavedList, UpdateSavedListInfoRequest} from "../../../model/saved-list";
import {SavedListService} from "../../../services/saved-list.service";
import {PostJobToSlackRequest, Progress, UpdateEmployerOpportunityRequest} from "../../../model/base";
import {getCandidateSourceExternalHref} from "../../../model/saved-search";
import {Location} from "@angular/common";
import {Router} from "@angular/router";
import {SalesforceService} from "../../../services/salesforce.service";
import {SlackService} from "../../../services/slack.service";

@Component({
  selector: 'app-new-job',
  templateUrl: './new-job.component.html',
  styleUrls: ['./new-job.component.scss']
})
export class NewJobComponent implements OnInit {
  jobName: string;
  savedList: SavedList;
  sfJoblink: string;
  creatingList: Progress = Progress.NotStarted;
  creatingFolders: Progress = Progress.NotStarted;
  creatingSFLinks: Progress = Progress.NotStarted;
  postingToSlack: Progress = Progress.NotStarted;
  findingJob: boolean;
  errorFindingJob: string = null;
  errorCreatingFolders: string = null;
  errorCreatingList: string = null;
  errorCreatingSFLinks: string = null;
  errorPostingToSlack: string = null;
  totalProgress: number;

  constructor(
    private salesforceService: SalesforceService,
    private savedListService: SavedListService,
    private slackService: SlackService,
    private location: Location,
    private router: Router) { }

  ngOnInit(): void {
  }

  get listLink(): string {
    return getCandidateSourceExternalHref(this.router, this.location, this.savedList);
  }
  get Progress() {
    return Progress;
  }

  get progressPercent() {
    return this.totalProgress;
  }


  onJoblinkValidation(jobOpportunity: JoblinkValidationEvent) {
    if (jobOpportunity.valid) {
      this.sfJoblink = jobOpportunity.sfJoblink;
      this.jobName = jobOpportunity.jobname;
    } else {
      this.sfJoblink = null;
      this.jobName = null;
    }
  }

  createList() {
    this.errorCreatingList = null;
    this.creatingList = Progress.Started;
    const request: UpdateSavedListInfoRequest = {
      name: this.jobName,
      fixed: true,
      sfJoblink: this.sfJoblink ? this.sfJoblink : null
    };
    this.savedListService.create(request).subscribe(
      (savedList) => {
        this.creatingList = Progress.Finished;
        this.savedList = savedList;
        this.totalProgress = 25;
        this.createFolders();
      },
      (error) => {
        this.errorCreatingList = error;
        this.creatingList = Progress.NotStarted;
      });
  }

  private createFolders() {
    //todo Check if list already exists for this opportunity - if so, use that, and update other things
    //     as needed.
    this.errorCreatingFolders = null;
    this.creatingFolders = Progress.Started;
    this.savedListService.createFolder(this.savedList.id).subscribe(
      savedList => {
        this.savedList = savedList;
        this.totalProgress = 50;
        this.createSFBacklinks();
        this.creatingFolders = Progress.Finished;
      },
      error => {
        this.errorCreatingFolders = error;
        this.creatingFolders = Progress.NotStarted;
      });
  }

  private createSFBacklinks() {
    this.errorCreatingSFLinks = null;
    this.creatingSFLinks = Progress.Started;

    const request: UpdateEmployerOpportunityRequest = {
      sfJoblink: this.savedList.sfJoblink,
      folderlink: this.savedList.folderlink,
      listlink: this.listLink
    };
    this.salesforceService.updateEmployerOpportunity(request).subscribe(
      () => {
        this.creatingSFLinks = Progress.Finished;
        this.totalProgress = 75;
      },
      error => {
        this.errorCreatingSFLinks = error;
        this.creatingSFLinks = Progress.NotStarted;
      });

    //Slack post can run in parallel with creating SF backlinks
    this.postJobToSlack();

  }

  private postJobToSlack() {
    this.errorPostingToSlack = null;
    this.postingToSlack = Progress.Started;

    const request: PostJobToSlackRequest = {
      sfJoblink: this.savedList.sfJoblink,
      jobName: this.jobName,
      folderlink: this.savedList.folderlink,
      listlink: this.listLink
    };
    this.slackService.postJob(request).subscribe(
      () => {
        //todo Could return link to Slack post
        this.postingToSlack = Progress.Finished;
        this.totalProgress = 100;
      },
      error => {
        this.errorPostingToSlack = error;
        this.postingToSlack = Progress.NotStarted;
      });

  }
}
