import {Component, Input, OnInit} from '@angular/core';
import {
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

@Component({
  selector: 'app-visa-job-check-au',
  templateUrl: './visa-job-check-au.component.html',
  styleUrls: ['./visa-job-check-au.component.scss']
})
export class VisaJobCheckAuComponent implements OnInit {
  @Input() selectedJobCheck: CandidateVisaJobCheck;
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaCheckRecord: CandidateVisa;

  candOccupations: CandidateOccupation[];
  candQualifications: CandidateEducation[];
  occupations: Occupation[];
  yrsExp: CandidateOccupation;
  familyInAus: string;

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

  get candidateAge(): string {
    if (this.candidate?.dob) {
      const timeDiff = Math.abs(Date.now() - new Date(this.candidate?.dob).getTime());
      return Math.floor(timeDiff / (1000 * 3600 * 24) / 365.25).toString(2);
    }
  }

  get ieltsScoreType(): string {
    return getIeltsScoreTypeString(this.candidate);
  }

}
