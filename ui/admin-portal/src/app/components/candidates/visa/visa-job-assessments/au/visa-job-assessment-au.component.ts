import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {IntakeComponentTabBase} from '../../../../util/intake/IntakeComponentTabBase';
import {CandidateVisa, CandidateVisaJobCheck} from '../../../../../model/candidate';
import {
  CandidateJobExperienceSearchRequest,
  CandidateJobExperienceService
} from "../../../../../services/candidate-job-experience.service";
import {CandidateService} from "../../../../../services/candidate.service";
import {CountryService} from "../../../../../services/country.service";
import {NationalityService} from "../../../../../services/nationality.service";
import {EducationLevelService} from "../../../../../services/education-level.service";
import {OccupationService} from "../../../../../services/occupation.service";
import {LanguageLevelService} from "../../../../../services/language-level.service";
import {CandidateNoteService} from "../../../../../services/candidate-note.service";
import {AuthService} from "../../../../../services/auth.service";
import {CandidateJobExperience} from "../../../../../model/candidate-job-experience";
import {CandidateOccupationService} from "../../../../../services/candidate-occupation.service";

@Component({
  selector: 'app-visa-job-assessment-au',
  templateUrl: './visa-job-assessment-au.component.html',
  styleUrls: ['./visa-job-assessment-au.component.scss']
})
export class VisaJobAssessmentAuComponent extends IntakeComponentTabBase implements OnInit, OnChanges {
  @Input() jobIndex: number;
  @Input() visaRecord: CandidateVisa;
  @Input() selectedJobCheck: CandidateVisaJobCheck;

  experiences: CandidateJobExperience[];

  constructor(candidateService: CandidateService,
              countryService: CountryService,
              nationalityService: NationalityService,
              educationLevelService: EducationLevelService,
              occupationService: OccupationService,
              languageLevelService: LanguageLevelService,
              noteService: CandidateNoteService,
              authService: AuthService,
              private candidateJobExperienceService: CandidateJobExperienceService,
              private candidateOccupationService: CandidateOccupationService) {
    super(candidateService, countryService, nationalityService, educationLevelService,
          occupationService, languageLevelService, noteService, authService)
    this.candidateJobExperienceService = candidateJobExperienceService
    this.candidateOccupationService = candidateOccupationService
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.selectedJobCheck && changes.selectedJobCheck.previousValue !== changes.selectedJobCheck.currentValue) {
      this.selectedJobCheck = changes.selectedJobCheck.currentValue;
    }
    const request: CandidateJobExperienceSearchRequest = {
      candidateId: this.candidate.id,
      occupationId: this.selectedJobCheck.id
    }
    this.experiences = [];
    this.candidateJobExperienceService.list(request).subscribe(
      (response) => {
        this.experiences = response;
      }, (error) => {
        this.error = error;
      }
    )
    console.log(this.experiences);
  }

  get currentYear(): string {
    return new Date().getFullYear().toString();
  }

  get birthYear(): string {
    return this.candidate.dob.toString().slice(0, 4);
  }

}
