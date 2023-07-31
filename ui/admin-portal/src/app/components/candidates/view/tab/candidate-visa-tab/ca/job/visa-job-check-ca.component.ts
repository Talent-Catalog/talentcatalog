import {Component, Input, OnInit} from '@angular/core';
import {
  Candidate,
  CandidateIntakeData,
  CandidateVisa,
  CandidateVisaJobCheck
} from "../../../../../../../model/candidate";
import {describeFamilyInDestination} from "../../../../../../../model/candidate-destination";

@Component({
  selector: 'app-visa-job-check-ca',
  templateUrl: './visa-job-check-ca.component.html',
  styleUrls: ['./visa-job-check-ca.component.scss']
})
export class VisaJobCheckCaComponent implements OnInit {
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaCheckRecord: CandidateVisa;
  selectedJobCheck: CandidateVisaJobCheck;
  familyInCanada: string;

  constructor() {}

  ngOnInit(): void {
    this.familyInCanada = describeFamilyInDestination(this.visaCheckRecord?.country.id, this.candidateIntakeData);
  }

  updateSelectedJob(index: number){
    this.selectedJobCheck = this.visaCheckRecord.candidateVisaJobChecks[index];
  }
}
