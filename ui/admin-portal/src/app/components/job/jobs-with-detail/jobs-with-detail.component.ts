import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../model/job";
import {MainSidePanelBase} from "../../util/split/MainSidePanelBase";
import {Router} from "@angular/router";
import {isStarredByMe, SearchOppsBy} from "../../../model/base";
import {JobService} from "../../../services/job.service";
import {AuthService} from "../../../services/auth.service";
import {AuthenticationService} from "../../../services/authentication.service";

/**
 * Displays the jobs returned by the given type of search, together with extra details
 * related to the selected job.
 * <p/>
 * The actual display of the jobs is delegated to the JobsComponent.
 */
@Component({
  selector: 'app-jobs-with-detail',
  templateUrl: './jobs-with-detail.component.html',
  styleUrls: ['./jobs-with-detail.component.scss']
})
export class JobsWithDetailComponent extends MainSidePanelBase implements OnInit {
  selectedJob: Job;
  error: any;
  loading: boolean;

  @Input() searchBy: SearchOppsBy;

  constructor(
    private router: Router,
    private authenticationService: AuthenticationService,
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
    return isStarredByMe(this.selectedJob?.starringUsers, this.authenticationService);
  }
}
