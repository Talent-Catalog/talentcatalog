import {Component, Input, OnInit} from '@angular/core';
import {IntakeComponentTabBase} from "../../../../../../util/intake/IntakeComponentTabBase";
import {CandidateService} from "../../../../../../../services/candidate.service";
import {CountryService} from "../../../../../../../services/country.service";
import {EducationLevelService} from "../../../../../../../services/education-level.service";
import {OccupationService} from "../../../../../../../services/occupation.service";
import {LanguageLevelService} from "../../../../../../../services/language-level.service";
import {CandidateNoteService} from "../../../../../../../services/candidate-note.service";
import {AuthService} from "../../../../../../../services/auth.service";
import {CandidateVisa, CandidateVisaJobCheck} from "../../../../../../../model/candidate";

@Component({
  selector: 'app-visa-job-check-ca',
  templateUrl: './visa-job-check-ca.component.html',
  styleUrls: ['./visa-job-check-ca.component.scss']
})
export class VisaJobCheckCaComponent extends IntakeComponentTabBase implements OnInit {
  @Input() visaRecord: CandidateVisa;
  selectedJob: CandidateVisaJobCheck;
  selectedJobIndex: number;

  constructor(candidateService: CandidateService,
              countryService: CountryService,
              educationLevelService: EducationLevelService,
              occupationService: OccupationService,
              languageLevelService: LanguageLevelService,
              noteService: CandidateNoteService,
              authService: AuthService) {
    super(candidateService, countryService, educationLevelService,
      occupationService, languageLevelService, noteService, authService)
  }

  ngOnInit(): void {
  }

  updateSelectedJob(index: number){
    this.selectedJob = this.visaRecord.candidateVisaJobChecks[index];
  }

}
