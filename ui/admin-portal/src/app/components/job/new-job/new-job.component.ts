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
import {SavedList} from "../../../model/saved-list";
import {SavedListService} from "../../../services/saved-list.service";
import {
  PostJobToSlackRequest,
  Progress,
  UpdateEmployerOpportunityRequest
} from "../../../model/base";
import {getCandidateSourceExternalHref} from "../../../model/saved-search";
import {Location} from "@angular/common";
import {Router} from "@angular/router";
import {SalesforceService} from "../../../services/salesforce.service";
import {SlackService} from "../../../services/slack.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {Job, UpdateJobRequest} from "../../../model/job";
import {JobService} from "../../../services/job.service";
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {AuthenticationService} from "../../../services/authentication.service";
import {Employer} from "../../../model/partner";
import {SfJoblinkValidationEvent} from "../../util/sf-joblink/sf-joblink.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {SelectJobCopyComponent} from "../../util/select-job-copy/select-job-copy.component";

@Component({
  selector: 'app-new-job',
  templateUrl: './new-job.component.html',
  styleUrls: ['./new-job.component.scss']
})
export class NewJobComponent implements OnInit {
  employer: Employer;
  jobName: string;
  roleName: string;
  job: Job;
  savedList: SavedList;
  sfJoblink: string;
  jobToCopyId: number;
  jobsToCopy: Job[];
  slacklink: string;
  creatingJob: Progress = Progress.NotStarted;
  creatingFolders: Progress = Progress.NotStarted;
  creatingSFLinks: Progress = Progress.NotStarted;
  postingToSlack: Progress = Progress.NotStarted;
  errorFindingJob: string = null;
  errorCreatingFolders: string = null;
  errorCreatingJob: string = null;
  errorCreatingSFLinks: string = null;
  errorPostingToSlack: string = null;
  errorGettingJobsToCopySlack: string = null;
  jobForm: UntypedFormGroup;
  remainingChars: number = 50;

  constructor(
    private authorizationService: AuthorizationService,
    private authenticationService: AuthenticationService,
    private fb: UntypedFormBuilder,
    private jobService: JobService,
    public salesforceService: SalesforceService,
    private savedListService: SavedListService,
    private slackService: SlackService,
    private location: Location,
    private router: Router,
    private modalService: NgbModal) { }

  ngOnInit(): void {
    if (this.isEmployerPartner()) {
      this.employer = this.authenticationService.getLoggedInUser()?.partner?.employer;

      this.jobForm = this.fb.group({
        role: []
      });
      this.subscribeToJobFormChanges();
      this.jobForm.get('role').valueChanges.subscribe((value: string) => {
        this.remainingChars = 50 - (value?.length || 0);
      });
    }
  }

  get listLink(): string {
    return getCandidateSourceExternalHref(this.router, this.location, this.savedList);
  }
  get Progress() {
    return Progress;
  }

  get progressPercent(): number {
    let pct = 0;
    if (this.creatingJob === Progress.Finished) {
      pct += 25;
    }
    if (this.creatingFolders === Progress.Finished) {
      pct += 25;
    }
    if (this.creatingSFLinks === Progress.Finished) {
      pct += 25;
    }
    if (this.postingToSlack === Progress.Finished) {
      pct += 25;
    }
    return pct;
  }


  private subscribeToJobFormChanges() {
    this.jobForm.valueChanges
    .pipe(
      debounceTime(1000),
      distinctUntilChanged()
    )
    .subscribe(() => {
      this.roleName = this.jobForm.value.role;
      this.jobName = this.generateJobName();
    });
  }

  private generateJobName(): string {
    let name = "";
    if (this.roleName && this.employer) {

      name = this.employer.name + "-" + (new Date()).getFullYear() + "-" + this.roleName;
    }
    return name;
  }

  onSfJoblinkValidation(jobOpportunity: SfJoblinkValidationEvent) {
    this.creatingJob = Progress.NotStarted;
    this.creatingFolders = Progress.NotStarted;
    this.creatingSFLinks = Progress.NotStarted;
    this.postingToSlack = Progress.NotStarted;

    if (jobOpportunity.valid) {
      this.sfJoblink = jobOpportunity.sfJoblink;
      this.jobName = jobOpportunity.jobname;
    } else {
      this.jobName = null;
    }
  }

  onJoblinkError(error) {
    this.errorFindingJob = error;
    if (error) {
      this.jobName = null;
    }
  }


  private createRegisteredJob() {
    this.errorCreatingJob = null;

    this.creatingJob = Progress.Started;
    const request: UpdateJobRequest = {
      roleName: this.roleName ? this.roleName : null,
      sfJoblink: this.sfJoblink ? this.sfJoblink : null,
      jobToCopyId: this.jobToCopyId ? this.jobToCopyId : null
    };
    this.jobService.create(request).subscribe(
      (job) => {
        this.creatingJob = Progress.Finished;
        this.job = job;
        this.savedList = job.submissionList;
        this.createFolders();
      },
      (error) => {
        this.errorCreatingJob = error;
        this.creatingJob = Progress.NotStarted;
      });
  }

  private createFolders() {
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
      sfJoblink: this.salesforceService.joblink(this.savedList),
      fileJdLink: this.savedList.fileJdLink,
      fileJdName: this.savedList.fileJdName,
      fileJoiLink: this.savedList.fileJoiLink,
      fileJoiName: this.savedList.fileJoiName,
      folderlink: this.savedList.folderlink,
      folderjdlink: this.savedList.folderjdlink,
      listlink: this.listLink,
      jobId: this.job.id
    };
    this.salesforceService.updateEmployerOpportunity(request).subscribe(
      () => {
        this.creatingSFLinks = Progress.Finished;
      },
      error => {
        this.errorCreatingSFLinks = error;
        this.creatingSFLinks = Progress.NotStarted;
      });

      this.postingToSlack = Progress.Finished;
  }

  private postJobToSlack() {
    this.errorPostingToSlack = null;
    this.postingToSlack = Progress.Started;

    const request: PostJobToSlackRequest = {
      sfJoblink: this.salesforceService.joblink(this.savedList),
      jobName: this.jobName,
      fileJdLink: this.savedList.fileJdLink,
      fileJdName: this.savedList.fileJdName,
      fileJoiLink: this.savedList.fileJoiLink,
      fileJoiName: this.savedList.fileJoiName,
      folderlink: this.savedList.folderlink,
      folderjdlink: this.savedList.folderjdlink,
      listlink: this.listLink
    };
    this.slackService.postJob(request).subscribe(
      (response) => {
        this.slacklink = response.slackChannelUrl;
        this.postingToSlack = Progress.Finished;
      },
      error => {
        this.errorPostingToSlack = error;
        this.postingToSlack = Progress.NotStarted;
      });

  }

  doRegistration() {
    this.createRegisteredJob()
  }

  getBreadCrumb() {
    return "New Job";
  }

  doPreparation() {
    this.createRegisteredJob()
  }

  selectJobCopy() {
    let jobs: Job[] = []
    const copyJobModal = this.modalService.open(SelectJobCopyComponent, {
      centered: true,
      backdrop: 'static'
    });

    copyJobModal.result.then(
      (jobId: number) => {
        this.jobToCopyId = jobId;
        this.doPreparation()
      })
      .catch(() => {})
  }

  doShowJob() {
    this.router.navigate(['job', this.job.id]);
  }

  isDefaultJobCreator() {
    return this.authorizationService.isDefaultJobCreator()
  }

  isEmployerPartner() {
    return this.authorizationService.isEmployerPartner();
  }
}
