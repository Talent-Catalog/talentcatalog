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

import {Component} from '@angular/core';
import {JobIntakeComponentTabBase} from "../../../../util/intake/JobIntakeComponentTabBase";
import {JobService} from "../../../../../services/job.service";
import {AuthenticationService} from "../../../../../services/authentication.service";
import {JobOppIntake} from "../../../../../model/job-opp-intake";
import {AuthorizationService} from "../../../../../services/authorization.service";

@Component({
  selector: 'app-job-intake-tab',
  templateUrl: './job-intake-tab.component.html',
  styleUrls: ['./job-intake-tab.component.scss']
})
export class JobIntakeTabComponent extends JobIntakeComponentTabBase {

  constructor(
    authenticationService: AuthenticationService,
    authorizationService: AuthorizationService,
    jobService: JobService
  ) {
    super(authenticationService, authorizationService, jobService);
  }

  onIntakeChanged(joi: JobOppIntake) {
    this.intakeChanged.emit(joi);
  }
}
