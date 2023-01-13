import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../model/job";
import {MainSidePanelBase} from "../../util/split/MainSidePanelBase";
import {Router} from "@angular/router";
import {SearchJobsBy} from "../../../model/base";
import {JobService} from "../../../services/job.service";
import {User} from "../../../model/user";
import {AuthService} from "../../../services/auth.service";


@Component({
  selector: 'app-jobs-with-detail',
  templateUrl: './jobs-with-detail.component.html',
  styleUrls: ['./jobs-with-detail.component.scss']
})
export class JobsWithDetailComponent extends MainSidePanelBase implements OnInit {
  selectedJob: Job;
  error: any;
  loading: boolean;

  @Input() searchBy: SearchJobsBy;

  constructor(
    private router: Router,
    private authService: AuthService,
    private jobService: JobService
  ) {
    super(6);
  }

  ngOnInit(): void {

  }

  onJobSelected(job: Job) {
    this.selectedJob = job;
  }

  doOpenJob() {
    this.router.navigate(['job', this.selectedJob.id]);
  }

  doToggleStarred() {
    this.loading = true;
    this.error = null
    this.jobService.updateStarred(this.selectedJob.id, !this.isStarred()).subscribe(
      (job: Job) => {this.selectedJob = job; this.loading = false},
      (error) => {this.error = error; this.loading = false}
    )
  }

  isStarred(): boolean {
    let starredByMe: boolean = false;
    const me: User = this.authService.getLoggedInUser();
    const starringUsers = this.selectedJob?.starringUsers;
    if (starringUsers && me) {
      starredByMe = starringUsers.find(u => u.id === me.id ) !== undefined;
    }
    return starredByMe;
  }
}
