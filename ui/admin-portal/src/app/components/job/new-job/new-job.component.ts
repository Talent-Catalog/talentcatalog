import {Component, OnInit} from '@angular/core';
import {JoblinkValidationEvent} from "../../util/joblink/joblink.component";
import {SavedList, UpdateSavedListInfoRequest} from "../../../model/saved-list";
import {SavedListService} from "../../../services/saved-list.service";
import {Progress, UpdateEmployerOpportunityRequest} from "../../../model/base";
import {getCandidateSourceExternalHref} from "../../../model/saved-search";
import {Location} from "@angular/common";
import {Router} from "@angular/router";
import {SalesforceService} from "../../../services/salesforce.service";

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
  findingJob: boolean;
  errorFindingJob: string = null;
  errorCreatingFolders: string = null;
  errorCreatingList: string = null;
  errorCreatingSFLinks: string = null;

  constructor(
    private salesforceService: SalesforceService,
    private savedListService: SavedListService,
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

  onJoblinkValidation(jobOpportunity: JoblinkValidationEvent) {
    if (jobOpportunity.valid) {
      this.sfJoblink = jobOpportunity.sfJoblink;
      this.jobName = jobOpportunity.jobname;
    } else {
      this.sfJoblink = null;
      this.jobName = null;
    }
  }

  create() {
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
        this.processSavedList();
      },
      (error) => {
        this.errorCreatingList = error;
        this.creatingList = Progress.NotStarted;
      });
  }

  private processSavedList() {
    //todo Check if list already exists for this opportunity - if so, use that, and update other things
    //     as needed.
    this.errorCreatingFolders = null;
    this.creatingFolders = Progress.Started;
    this.savedListService.createFolder(this.savedList.id).subscribe(
      savedList => {
        this.savedList = savedList;
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
      },
      error => {
        this.errorCreatingSFLinks = error;
        this.creatingSFLinks = Progress.NotStarted;
      });


    //todo Slack post (can run in parallel with creating SF backlinks)

  }
}
