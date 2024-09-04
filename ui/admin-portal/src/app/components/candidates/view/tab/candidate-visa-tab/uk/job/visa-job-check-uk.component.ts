import {AfterViewInit, Component, Input, OnInit, ViewChild} from '@angular/core';
import {
  Candidate,
  CandidateIntakeData,
  CandidateVisa,
  CandidateVisaJobCheck
} from "../../../../../../../model/candidate";
import {NgbAccordion} from "@ng-bootstrap/ng-bootstrap";
import {CandidateEducationService} from "../../../../../../../services/candidate-education.service";
import {CandidateOccupationService} from "../../../../../../../services/candidate-occupation.service";
import {OccupationService} from "../../../../../../../services/occupation.service";
import {CandidateOpportunity} from "../../../../../../../model/candidate-opportunity";

@Component({
  selector: 'app-visa-job-check-uk',
  templateUrl: './visa-job-check-uk.component.html',
  styleUrls: ['./visa-job-check-uk.component.scss']
})
export class VisaJobCheckUkComponent implements OnInit, AfterViewInit {
  @Input() selectedJobCheck: CandidateVisaJobCheck;
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaCheckRecord: CandidateVisa;

  @ViewChild('visaJobUk') visaJobUk: NgbAccordion;

  candidateOpportunity: CandidateOpportunity;

  error: string;

  constructor(private candidateEducationService: CandidateEducationService,
              private candidateOccupationService: CandidateOccupationService,
              private occupationService: OccupationService) {}

  ngOnInit() {
    this.candidateOpportunity = this.candidate.candidateOpportunities
      .find(co => co.jobOpp.id == this.selectedJobCheck.jobOpp.id);
  }

  ngAfterViewInit() {
    if(this.visaJobUk){
      this.visaJobUk.expandAll();
    }
  }
}
