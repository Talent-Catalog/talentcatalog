import {Component, Input, OnInit} from '@angular/core';
import {
  Candidate,
  CandidateIntakeData,
  CandidateVisa,
  CandidateVisaJobCheck,
  getDestinationPathwayInfoLink,
  IeltsStatus
} from "../../../../../../../model/candidate";
import {describeFamilyInDestination} from "../../../../../../../model/candidate-destination";
import {CandidateEducationService} from "../../../../../../../services/candidate-education.service";
import {CandidateOccupationService} from "../../../../../../../services/candidate-occupation.service";
import {CandidateOccupation} from "../../../../../../../model/candidate-occupation";
import {CandidateEducation} from "../../../../../../../model/candidate-education";
import {JobService} from "../../../../../../../services/job.service";
import {Job} from "../../../../../../../model/job";

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
  selectedJob: Job;
  familyInCanada: string;
  partnerIeltsString: string;

  error: string;

  constructor(private candidateEducationService: CandidateEducationService,
              private candidateOccupationService: CandidateOccupationService,
              private jobService: JobService) {}

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

    this.jobService.get(this.selectedJobCheck.jobOpp.id).subscribe(
      (response) => {
        this.selectedJob = response;
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

  openPathwaysLink() {
    let url = getDestinationPathwayInfoLink(this.visaCheckRecord.country.id);
    if (url) {
      //Open link in new window
      window.open(url, "_blank");
    }
  }
}
