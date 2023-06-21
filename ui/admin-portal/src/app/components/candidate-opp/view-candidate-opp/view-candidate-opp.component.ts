import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CandidateOpportunity, isCandidateOpportunity} from "../../../model/candidate-opportunity";
import {EditCandidateOppComponent} from "../edit-candidate-opp/edit-candidate-opp.component";
import {CandidateOpportunityParams} from "../../../model/candidate";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateOpportunityService} from "../../../services/candidate-opportunity.service";
import {SalesforceService} from "../../../services/salesforce.service";
import {AuthService} from "../../../services/auth.service";
import {getOpportunityStageName, Opportunity} from "../../../model/opportunity";
import {ShortSavedList} from "../../../model/saved-list";

@Component({
  selector: 'app-view-candidate-opp',
  templateUrl: './view-candidate-opp.component.html',
  styleUrls: ['./view-candidate-opp.component.scss']
})
export class ViewCandidateOppComponent implements OnInit {
  @Input() opp: CandidateOpportunity;
  @Input() showBreadcrumb: boolean = true;
  @Output() candidateOppUpdated = new EventEmitter<CandidateOpportunity>();

  error: string;
  updating: boolean;

  constructor(
    private authService: AuthService,
    private candidateOpportunityService: CandidateOpportunityService,
    private modalService: NgbModal,
    private salesforceService: SalesforceService,

  ) { }

  ngOnInit(): void {
  }

  get getCandidateOpportunityStageName() {
    return getOpportunityStageName
  }

  get editable(): boolean {
    return this.authService.canEditCandidateOpp(this.opp);
  }

  editOppProgress() {
    const editQuery = this.modalService.open(EditCandidateOppComponent, {size: 'lg'});
    editQuery.componentInstance.opp = this.opp;

    //Progress parameters (stage, nextStep) are set separately in this component
    editQuery.componentInstance.showProgressParams = false;

    editQuery.result
    .then((info: CandidateOpportunityParams) => {this.doUpdate(info);})
    .catch(() => { });
  }

  private doUpdate(info: CandidateOpportunityParams) {
    this.updating = true;
    this.candidateOpportunityService.updateCandidateOpportunity(this.opp.id, info)
    .subscribe(opp => {
        //Emit an opp updated which will refresh the display
        this.candidateOppUpdated.emit(opp);
        this.updating = false;
      },
      err => {this.error = err; this.updating = false; }
    );

  }

  getOppSfLink(sfId: string): string {
    return this.salesforceService.sfOppToLink(sfId);
  }

  canAccessSalesforce(): boolean {
    return this.authService.canAccessSalesforce();
  }

  displaySavedList(list: ShortSavedList) {
    return list ? list.name + "(" + list.id + ")" : "";
  }

  onOppProgressUpdated(opp: Opportunity) {
    if (isCandidateOpportunity(opp)) {
      this.candidateOppUpdated.emit(opp);
    }
  }
}
