import {Component, Inject, Input, LOCALE_ID, SimpleChanges} from '@angular/core';
import {
  CandidateOpportunity,
  CandidateOpportunityStage,
  SearchOpportunityRequest
} from "../../../model/candidate-opportunity";
import {CandidateOpportunityService} from "../../../services/candidate-opportunity.service";
import {LocalStorageService} from "angular-2-local-storage";
import {FormBuilder} from "@angular/forms";
import {SalesforceService} from "../../../services/salesforce.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {EnumOption, enumOptions} from "../../../util/enum";
import {FilteredOppsComponentBase} from "../../util/opportunity/FilteredOppsComponentBase";

@Component({
  selector: 'app-candidate-opps',
  templateUrl: './candidate-opps.component.html',
  styleUrls: ['./candidate-opps.component.scss']
})
export class CandidateOppsComponent extends FilteredOppsComponentBase<CandidateOpportunity> {
  /**
   * Only one of these inputs should be selected - one does a search, the other just takes
   * an array of opps.
   */
  @Input() candidateOpps: CandidateOpportunity[];

  /**
   * Display the name of the job opportunity rather than the name of the candidate opportunity.
   * <p/>
   * This is useful when you are displaying the Jobs that a candidate has gone for.
   */
  @Input() showJobOppName: boolean = false;

  /**
   * This is to determine if the component is being viewed from the candidate preview (from within a list/search) or not.
   * If preview is true we have less space to display the component so can use this boolean to make space efficient changes of the CSS.
   */
  @Input() preview: boolean = false;

  //Override text to replace "opps" text with "cases"
  myOppsOnlyLabel = "My cases only";
  myOppsOnlyTip = "Only show cases that I am the contact for";
  showClosedOppsLabel = "Show closed cases";
  showClosedOppsTip = "Show cases that have been closed";
  showInactiveOppsLabel = "Show inactive cases";
  showInactiveOppsTip = "Show cases that are no longer active - " +
    "for example if the candidate has already relocated";

  constructor(
    fb: FormBuilder,
    authService: AuthorizationService,
    localStorageService: LocalStorageService,
    oppService: CandidateOpportunityService,
    salesforceService: SalesforceService,
    @Inject(LOCALE_ID) locale: string
  ) {
    super(fb, authService, localStorageService, oppService, salesforceService, locale,
      "Opps")

  }

  ngOnChanges(changes: SimpleChanges): void {
    super.ngOnChanges(changes);

    if (changes.candidateOpps) {
      this.opps = this.candidateOpps;
    }
  }

  protected createSearchRequest(): SearchOpportunityRequest {
    return new SearchOpportunityRequest();
  }

  protected loadStages(): EnumOption[] {
    return enumOptions(CandidateOpportunityStage);
  }
}
