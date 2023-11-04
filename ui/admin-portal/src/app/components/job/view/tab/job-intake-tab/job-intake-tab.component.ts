import {Component} from '@angular/core';
import {JobIntakeComponentTabBase} from "../../../../util/intake/JobIntakeComponentTabBase";
import {JobService} from "../../../../../services/job.service";
import {AuthService} from "../../../../../services/auth.service";
import {AuthenticationService} from "../../../../../services/authentication.service";

@Component({
  selector: 'app-job-intake-tab',
  templateUrl: './job-intake-tab.component.html',
  styleUrls: ['./job-intake-tab.component.scss']
})
export class JobIntakeTabComponent extends JobIntakeComponentTabBase {

  constructor(
    authenticationService: AuthenticationService,
    jobService: JobService
  ) {
    super(authenticationService, jobService);
  }


}
