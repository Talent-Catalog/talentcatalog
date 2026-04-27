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
import {JobService} from "../../../../services/job.service";
import {ActivatedRoute} from "@angular/router";
import {Job} from "../../../../model/job";

/*
   MODEL: Landing component for viewing an object from its url, fetching the object, then
   delegating to another component for the actual display of the object.
 */
/**
 * This is the landing page for urls referencing a job.
 * </p>
 * It is referred to in AppRoutingModule (app-routing.module.ts)
 * <p/>
 * It checks the url, extracting the job id and attempts to load the job from the server,
 * displaying an error if none found, otherwise it uses the ViewJobComponent to display the job
 * details.
 */
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
