import {Component, Input, OnInit} from '@angular/core';
import {
  Candidate,
  CandidateIntakeData,
  CandidateVisa,
  CandidateVisaJobCheck,
  IeltsStatus
} from "../../../../../../../model/candidate";
import {describeFamilyInDestination} from "../../../../../../../model/candidate-destination";
import {CandidateEducationService} from "../../../../../../../services/candidate-education.service";
import {CandidateOccupationService} from "../../../../../../../services/candidate-occupation.service";
import {CandidateOccupation} from "../../../../../../../model/candidate-occupation";
import {CandidateEducation} from "../../../../../../../model/candidate-education";

@Component({
  selector: 'app-visa-job-check-ca',
  templateUrl: './visa-job-check-ca.component.html',
  styleUrls: ['./visa-job-check-ca.component.scss']
})
export class VisaJobCheckCaComponent implements OnInit {
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaCheckRecord: CandidateVisa;

  candOccupations: CandidateOccupation[];
  candQualifications: CandidateEducation[];
  selectedJobCheck: CandidateVisaJobCheck;
  familyInCanada: string;
  partnerIeltsString: string;

  error: string;

  constructor(private candidateEducationService: CandidateEducationService,
              private candidateOccupationService: CandidateOccupationService) {}

  ngOnInit(): void {
    // Get the candidate occupations
    this.candidateOccupationService.get(this.candidate.id).subscribe(
      (response) => {
        this.candOccupations = response;
      }, (error) => {
        this.error = error;
      }
    )
    // Get the candidate qualifications
    this.candidateEducationService.list(this.candidate.id).subscribe(
      (response) => {
        this.candQualifications = response;
      }, (error) => {
        this.error = error;
      }
    )

    this.familyInCanada = describeFamilyInDestination(this.visaCheckRecord?.country.id, this.candidateIntakeData);
    this.partnerIeltsString = IeltsStatus[this.candidateIntakeData?.partnerIelts]
    + (this.candidateIntakeData?.partnerIeltsScore ? ', Score: ' + this.candidateIntakeData.partnerIeltsScore : null);
  }



  updateSelectedJob(index: number){
    this.selectedJobCheck = this.visaCheckRecord.candidateVisaJobChecks[index];
  }

  protected readonly IeltsStatus = IeltsStatus;
}
