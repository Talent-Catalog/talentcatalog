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
import {AuthService} from "../../../services/auth.service";
import {EnumOption, enumOptions} from "../../../util/enum";
import {FilteredOppsComponent} from "../../opportunity/filtered-opps/filtered-opps.component";

@Component({
  selector: 'app-candidate-opps',
  templateUrl: '../../opportunity/filtered-opps/filtered-opps.component.html',
  styleUrls: ['../../opportunity/filtered-opps/filtered-opps.component.scss']
})
export class CandidateOppsComponent extends FilteredOppsComponent<CandidateOpportunity> {
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

  constructor(
    fb: FormBuilder,
    authService: AuthService,
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
