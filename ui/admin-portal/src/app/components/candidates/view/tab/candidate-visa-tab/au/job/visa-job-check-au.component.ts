import {AfterViewInit, Component, Input, OnInit, ViewChild} from '@angular/core';
import {
  calculateAge,
  Candidate,
  CandidateIntakeData,
  CandidateVisa,
  CandidateVisaJobCheck,
  getIeltsScoreTypeString
} from "../../../../../../../model/candidate";
import {OccupationService} from "../../../../../../../services/occupation.service";
import {CandidateOccupationService} from "../../../../../../../services/candidate-occupation.service";
import {CandidateOccupation} from "../../../../../../../model/candidate-occupation";
import {CandidateEducationService} from "../../../../../../../services/candidate-education.service";
import {CandidateEducation} from "../../../../../../../model/candidate-education";
import {describeFamilyInDestination} from "../../../../../../../model/candidate-destination";
import {Occupation} from "../../../../../../../model/occupation";
import {NgbAccordion} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-visa-job-check-au',
  templateUrl: './visa-job-check-au.component.html',
  styleUrls: ['./visa-job-check-au.component.scss']
})
export class VisaJobCheckAuComponent implements OnInit, AfterViewInit {
  @Input() selectedJobCheck: CandidateVisaJobCheck;
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaCheckRecord: CandidateVisa;

  @ViewChild('visaJobAus') visaJobAus: NgbAccordion;

  candOccupations: CandidateOccupation[];
  candQualifications: CandidateEducation[];
  occupations: Occupation[];
  yrsExp: CandidateOccupation;
  familyInAus: string;
  candidateAge: number;

  error: string;

  constructor(private candidateEducationService: CandidateEducationService,
              private candidateOccupationService: CandidateOccupationService,
              private occupationService: OccupationService) {}

  ngOnInit() {
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

    // Get the list of all occupations
    this.occupationService.listOccupations().subscribe(
      (results) => {
        this.occupations = results;
      }
    )

    this.familyInAus = describeFamilyInDestination(this.visaCheckRecord?.country.id, this.candidateIntakeData);
    const dobDate = new Date(this.candidate.dob);
    this.candidateAge = calculateAge(dobDate);
  }

  ngAfterViewInit() {
    this.visaJobAus.expandAll();
  }


  get currentYear(): string {
    return new Date().getFullYear().toString();
  }

  get birthYear(): string {
    return this.candidate?.dob.toString().slice(0, 4);
  }

  get selectedOccupations(): CandidateOccupation {
    if (this.candOccupations) {
      this.yrsExp = this.candOccupations?.find(occ => occ.occupation.id === this.selectedJobCheck?.occupation?.id);
      return this.yrsExp;
    }
  }

  get ieltsScoreType(): string {
    return getIeltsScoreTypeString(this.candidate);
  }

}
