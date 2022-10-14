import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {JobService} from "../../../../services/job.service";
import {Job} from "../../../../model/job";

@Component({
  selector: 'app-view-job',
  templateUrl: './view-job.component.html',
  styleUrls: ['./view-job.component.scss']
})
export class ViewJobComponent implements OnInit {
  job: Job;

  loading: boolean;
  error: string;

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
      } else {
        //This is the job url with no id specified.
        this.createNewJob();
      }
    });
  }

  private loadJob(id: number) {

    this.loading = true;
    this.error = null;
    this.jobService.get(id).subscribe(
      job => {
        this.setJob(job);
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      });

  }

  private createNewJob() {
    //todo create new job
  }

  private setJob(job: Job) {
    this.job = job;
  }
}
