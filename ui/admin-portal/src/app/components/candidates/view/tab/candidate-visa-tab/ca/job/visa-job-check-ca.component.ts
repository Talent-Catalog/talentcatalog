import {AfterViewInit, Component, Input, OnInit, ViewChild} from '@angular/core';
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
import {NgbAccordion} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-visa-job-check-ca',
  templateUrl: './visa-job-check-ca.component.html',
  styleUrls: ['./visa-job-check-ca.component.scss']
})
export class VisaJobCheckCaComponent implements OnInit, AfterViewInit {
  @Input() selectedJobCheck: CandidateVisaJobCheck;
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaCheckRecord: CandidateVisa;

  @ViewChild('visaJobCanada') visaJobCanada: NgbAccordion;

  candOccupations: CandidateOccupation[];
  candQualifications: CandidateEducation[];

  familyInCanada: string;
  partnerIeltsString: string;
  pathwaysInfoLink: string;

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

    // Process & fetch values that need to be displayed.
    this.familyInCanada = describeFamilyInDestination(this.visaCheckRecord?.country.id, this.candidateIntakeData);
    if (this.candidateIntakeData?.partnerIelts) {
      this.partnerIeltsString = IeltsStatus[this.candidateIntakeData?.partnerIelts] +
        (this.candidateIntakeData?.partnerIeltsScore ? ', Score: ' + this.candidateIntakeData.partnerIeltsScore : null);
    } else {
      this.partnerIeltsString = null;
    }
    this.pathwaysInfoLink = getDestinationPathwayInfoLink(this.visaCheckRecord.country.id);
  }

  ngAfterViewInit() {
    this.visaJobCanada.expandAll();
  }
}




