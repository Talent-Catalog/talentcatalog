import {Component} from '@angular/core';
import {calculateAge, getIeltsScoreTypeString} from "../../../../../../../model/candidate";
import {OccupationService} from "../../../../../../../services/occupation.service";
import {CandidateOccupationService} from "../../../../../../../services/candidate-occupation.service";
import {CandidateOccupation} from "../../../../../../../model/candidate-occupation";
import {CandidateEducationService} from "../../../../../../../services/candidate-education.service";
import {Occupation} from "../../../../../../../model/occupation";
import {CandidateVisaJobService} from "../../../../../../../services/candidate-visa-job.service";
import {VisaJobCheckBase} from "../../../../../../util/visa/visaJobCheckBase";

@Component({
  selector: 'app-visa-job-check-au',
  templateUrl: './visa-job-check-au.component.html',
  styleUrls: ['./visa-job-check-au.component.scss']
})
export class VisaJobCheckAuComponent extends VisaJobCheckBase {
  occupations: Occupation[];
  yrsExp: CandidateOccupation;
  candidateAge: number;

  error: string;
  loading: boolean;

  constructor(candidateEducationService: CandidateEducationService,
              candidateOccupationService: CandidateOccupationService,
              candidateVisaJobService: CandidateVisaJobService,
              private occupationService: OccupationService) {
    super(candidateEducationService, candidateOccupationService, candidateVisaJobService);
  }

  ngOnInit() {
    super.ngOnInit();
    // Get the list of all occupations
    this.occupationService.listOccupations().subscribe(
      (results) => {
        this.occupations = results;
      }
    )

    const dobDate = new Date(this.candidate.dob);
    this.candidateAge = calculateAge(dobDate);
  }

  get ieltsScoreType(): string {
    return getIeltsScoreTypeString(this.candidate);
  }

}
