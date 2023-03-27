import {Component, OnInit} from '@angular/core';
import {JobService} from "../../../../services/job.service";
import {ActivatedRoute} from "@angular/router";
import {Job} from "../../../../model/job";

@Component({
  selector: 'app-view-job-from-url',
  templateUrl: './view-job-from-url.component.html',
  styleUrls: ['./view-job-from-url.component.scss']
})
export class ViewJobFromUrlComponent implements OnInit {

  loading: boolean;
  error: string;
  job: Job;

  constructor(
    private jobService: JobService,
    private route: ActivatedRoute,
  ) { }

  ngOnInit(): void {
    this.refreshJobInfo();
  }

  private refreshJobInfo() {
    this.route.paramMap.subscribe(params => {
      const id = +params.get('id');
      if (id) {
        this.loadJob(id);
      }
    });
  }

  private loadJob(id: number) {

    this.loading = true;
    this.error = null;
    this.jobService.get(id).subscribe(
      job => {
        // todo ADDED hardcoded data to job object for JOI testing
        job.website = "www.iress.com.au"
        job.employerDescription = "A global team of 2,250+ people building software that helps the financial services industry perform at its best."
        job.employerHiringCommitment = 20
        job.employerPreviousHire = "Yes";
        this.setJob(job);
        this.loading = false;
      },
      error => {
        this.error = error;
        this.setJob(null);
        this.loading = false;
      });
  }

  private setJob(job: Job) {
    this.job = job;
  }

  onJobUpdated(job: Job) {
    this.setJob(job)
  }
}
