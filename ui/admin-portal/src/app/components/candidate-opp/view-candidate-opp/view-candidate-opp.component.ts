import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {
  CandidateOpportunity,
  getCandidateOpportunityStageName
} from "../../../model/candidate-opportunity";
import {EditCandidateOppComponent} from "../edit-candidate-opp/edit-candidate-opp.component";
import {CandidateOpportunityParams} from "../../../model/candidate";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateOpportunityService} from "../../../services/candidate-opportunity.service";

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
    private candidateOpportunityService: CandidateOpportunityService,
    private modalService: NgbModal,
  ) { }

  ngOnInit(): void {
  }

  get getCandidateOpportunityStageName() {
    return getCandidateOpportunityStageName
  }

  get editable(): boolean {
    //todo Needs logic as who can update an opp.
    return true;
  }

  editOppProgress() {
    const editQuery = this.modalService.open(EditCandidateOppComponent, {size: 'lg'});
    editQuery.result
    .then((info: CandidateOpportunityParams) => {this.doUpdate(info);})
    .catch(() => { });
  }

  private doUpdate(info: CandidateOpportunityParams) {
    this.candidateOpportunityService.updateCandidateOpportunity(this.opp.id, info)
    .subscribe(opp => {
        //Emit an opp updated which will refresh the display
        this.candidateOppUpdated.emit(opp);
        this.updating = false;
      },
      err => {this.error = err; this.updating = false; }
    );

  }
}
