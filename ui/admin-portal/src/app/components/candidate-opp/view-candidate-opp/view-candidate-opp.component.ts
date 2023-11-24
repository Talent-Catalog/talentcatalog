import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CandidateOpportunity, isCandidateOpportunity} from "../../../model/candidate-opportunity";
import {EditCandidateOppComponent} from "../edit-candidate-opp/edit-candidate-opp.component";
import {CandidateOpportunityParams} from "../../../model/candidate";
import {NgbModal, NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {CandidateOpportunityService} from "../../../services/candidate-opportunity.service";
import {SalesforceService} from "../../../services/salesforce.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {getOpportunityStageName, Opportunity} from "../../../model/opportunity";
import {ShortSavedList} from "../../../model/saved-list";
import {LocalStorageService} from "angular-2-local-storage";
import {JobChatType} from "../../../model/chat";
import {AuthenticationService} from "../../../services/authentication.service";

@Component({
  selector: 'app-view-candidate-opp',
  templateUrl: './view-candidate-opp.component.html',
  styleUrls: ['./view-candidate-opp.component.scss']
})
export class ViewCandidateOppComponent implements OnInit {
  @Input() opp: CandidateOpportunity;
  @Input() showBreadcrumb: boolean = true;
  @Output() candidateOppUpdated = new EventEmitter<CandidateOpportunity>();

  activeTabId: string;
  error: string;
  private lastTabKey: string = 'CaseLastTab';
  updating: boolean;
  candidateProspectTabVisible: boolean;
  candidateRecruitingTabVisible: boolean;
  candidateRecruitingTabTitle: string = 'CandidateRecruiting'

  constructor(
    private authorizationService: AuthorizationService,
    private authenticationService: AuthenticationService,
    private candidateOpportunityService: CandidateOpportunityService,
    private localStorageService: LocalStorageService,
    private modalService: NgbModal,
    private salesforceService: SalesforceService,

  ) { }

  ngOnInit(): void {
    this.selectDefaultTab();
    this.checkVisibility();

  }

  get getCandidateOpportunityStageName() {
    return getOpportunityStageName
  }

  get editable(): boolean {
    return this.authorizationService.canEditCandidateOpp(this.opp);
  }

  get JobChatType() {
    return JobChatType;
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
    return this.authorizationService.canAccessSalesforce();
  }

  displaySavedList(list: ShortSavedList) {
    return list ? list.name + "(" + list.id + ")" : "";
  }

  onOppProgressUpdated(opp: Opportunity) {
    if (isCandidateOpportunity(opp)) {
      this.candidateOppUpdated.emit(opp);
    }
  }

  private selectDefaultTab() {
    const defaultActiveTabID: string = this.localStorageService.get(this.lastTabKey);
    this.activeTabId = defaultActiveTabID;
  }

  onTabChanged(event: NgbNavChangeEvent) {
    this.setActiveTabId(event.nextId);
  }

  private setActiveTabId(id: string) {
    this.activeTabId = id;
    this.localStorageService.set(this.lastTabKey, id);
  }

  private checkVisibility() {
    const candidateStage = this.opp?.stage;
    const candidatePartner = this.opp?.candidate?.user?.partner;
    const recruiterPartner = this.opp?.jobOpp.recruiterPartner;
    const loggedInPartner = this.authenticationService.getLoggedInUser().partner;

    //User is recruiter for this opp or default job creator
    const userIsRecruitingPartner =
      loggedInPartner.defaultJobCreator || loggedInPartner.id == recruiterPartner?.id;

    //User is source partner responsible for candidate or default source partner
    const userIsCandidatePartner =
      loggedInPartner.defaultSourcePartner || loggedInPartner.id == candidatePartner?.id;

    this.candidateProspectTabVisible = userIsCandidatePartner;

    //todo Recruiters only see candidates past the CVReview stage.
    this.candidateRecruitingTabVisible = userIsCandidatePartner || userIsRecruitingPartner;

    //Label on candidateRecruiting chat depends on who the logged in user is.
    if (userIsCandidatePartner) {
      this.candidateRecruitingTabTitle = 'Chat with candidate & recruiter'
    } else if (userIsRecruitingPartner) {
      this.candidateRecruitingTabTitle = 'Chat with candidate & source partner'
    }
  }
}
