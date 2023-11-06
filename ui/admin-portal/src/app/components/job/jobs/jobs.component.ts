import {Component, Inject, LOCALE_ID} from '@angular/core';
import {AuthorizationService} from "../../../services/authorization.service";
import {LocalStorageService} from "angular-2-local-storage";
import {FormBuilder} from "@angular/forms";
import {Job, JobOpportunityStage, SearchJobRequest} from "../../../model/job";
import {JobService} from "../../../services/job.service";
import {EnumOption, enumOptions} from "../../../util/enum";
import {SearchOppsBy} from "../../../model/base";
import {FilteredOppsComponentBase} from "../../util/opportunity/FilteredOppsComponentBase";
import {SalesforceService} from "../../../services/salesforce.service";
import {SearchOpportunityRequest} from "../../../model/candidate-opportunity";

@Component({
  selector: 'app-jobs',
  templateUrl: './jobs.component.html',
  styleUrls: ['./jobs.component.scss']
})
export class JobsComponent extends FilteredOppsComponentBase<Job> {

  //Override text to replace "opps" text with "jobs"
  myOppsOnlyLabel = "My jobs only";
  myOppsOnlyTip = "Only show jobs that I manage";
  showClosedOppsLabel = "Show closed jobs";
  showClosedOppsTip = "Show jobs that have been closed";
  showInactiveOppsLabel = "Show inactive jobs";
  showInactiveOppsTip = "Show jobs that are not currently accepting new candidates";

  constructor(
    fb: FormBuilder,
    authService: AuthorizationService,
    localStorageService: LocalStorageService,
    oppService: JobService,
    salesforceService: SalesforceService,
    @Inject(LOCALE_ID) locale: string
  ) {
    super(fb, authService, localStorageService, oppService, salesforceService, locale,
          "Jobs")
  }

  protected createSearchRequest(): SearchOpportunityRequest {
    let req =  new SearchJobRequest();

    switch (this.searchBy) {
      case SearchOppsBy.live:

        //Don't want to see closed jobs
        req.sfOppClosed = false;

        //Jobs must have been published
        req.published = true;

        //Only want jobs which are accepting candidates. This is equivalent to checking that the
        //job's stage is between candidate search and prior to job offer/acceptance.
        //This request is ignored if certain stages have been requested (because that will clash
        //with checking the above range of stages)
        req.activeStages = true;
        break;

      case SearchOppsBy.starredByMe:
        req.starred = true;
        break;
    }

    return req;
  }

  protected loadStages(): EnumOption[] {
    return enumOptions(JobOpportunityStage);
  }

}
