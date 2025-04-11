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

import {
  Directive,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {forkJoin} from 'rxjs';
import {User} from '../../../model/user';
import {Job} from "../../../model/job";
import {JobOppIntake} from "../../../model/job-opp-intake";
import {JobService} from "../../../services/job.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {AuthorizationService} from "../../../services/authorization.service";

/**
 * Base class for all job intake tab components.
 * <p/>
 * Provides following standard functionality
 * <ul>
 *   <li>Loads standard data required by all intake tabs
 *   <li>Declares standard job @Input</li>
 *   <li>Provides standard "error" and "loading" attributes for display to user</li>
 * </ul>
 * @author John Cameron
 */
@Directive()
export abstract class JobIntakeComponentTabBase implements OnInit, OnChanges {
  /**
   * This is the job whose intake data we are entering
   */
  @Input() job: Job;

  @Output() intakeChanged = new EventEmitter<JobOppIntake>();


  /**
   * This is the existing job intake data (if any) which is used to
   * initialize the form data.
   */
  jobIntakeData: JobOppIntake;

  /**
   * Error which should be displayed to user if not null.
   * Typically, an error connecting to the Spring server.
   */
  error: string;

  /**
   * True when loading is underway. Should be used to show the user when a load
   * is happening.
   */
  loading: boolean;

  /**
   * True when saving is underway. Should be used to show the user when a save
   * is happening.
   */
  saving: boolean;

  /**
   * Logged in User for audit
   */
  loggedInUser: User;

  public constructor(
    protected authenticationService: AuthenticationService,
    protected authorizationService: AuthorizationService,
    protected jobService: JobService,
  ) {
    this.loggedInUser = this.authenticationService.getLoggedInUser();
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.refreshIntakeDataInternal(true);
  }

  canViewEmployerDetails() {
    //Employer partners do not need to see details about themselves.
    return !this.authorizationService.isEmployerPartner();
  }

  /**
   * Loads all the job's intake data from the server, as well as
   * standard data used for intake data entry, such as lists of all countries
   * and nationalities.
   */
  refreshIntakeData(): void {
    this.refreshIntakeDataInternal(false)
  }

  private refreshIntakeDataInternal(init: boolean): void {
    //Load existing intakeData and other data needed by intake
    this.error = null;
    this.loading = true;
    forkJoin({
      'job':  this.jobService.get(this.job.id),
    }).subscribe(results => {
      this.loading = false;
      this.jobIntakeData = results['job'].jobOppIntake;
      if (!this.jobIntakeData) {
        //If there is no JobIntakeData - create an empty one.
        this.jobIntakeData = {};
      }
      this.onDataLoaded(init);
    }, error => {
      this.loading = false;
      this.error = error;
    });
  }

  /**
   * Called when all intake data has been loaded (by refreshIntakeData).
   * <p/>
   * Default implementation does nothing but can be overridden in subclasses
   * to perform extra processing once data has been loaded.
   * @param init True if data was loaded as part of ngOnInit. If false
   * the data loading was triggered by directly calling refreshIntakeData
   * externally to do a data refresh.
   */
  protected onDataLoaded(init: boolean) {}
}
