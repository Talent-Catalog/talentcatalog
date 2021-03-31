import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {
  CandidateDestination,
  CandidateIntakeData,
  CandidateVisa,
  CandidateVisaJobCheck
} from "../../../../../../../model/candidate";
import {IntakeComponentTabBase} from "../../../../../../util/intake/IntakeComponentTabBase";
import {CandidateService} from "../../../../../../../services/candidate.service";
import {CountryService} from "../../../../../../../services/country.service";
import {NationalityService} from "../../../../../../../services/nationality.service";
import {EducationLevelService} from "../../../../../../../services/education-level.service";
import {OccupationService} from "../../../../../../../services/occupation.service";
import {LanguageLevelService} from "../../../../../../../services/language-level.service";
import {CandidateNoteService} from "../../../../../../../services/candidate-note.service";
import {AuthService} from "../../../../../../../services/auth.service";
import {CandidateOccupationService} from "../../../../../../../services/candidate-occupation.service";
import {CandidateOccupation} from "../../../../../../../model/candidate-occupation";
import {CandidateEducationService} from "../../../../../../../services/candidate-education.service";
import {CandidateEducation} from "../../../../../../../model/candidate-education";

@Component({
  selector: 'app-visa-job-check-au',
  templateUrl: './visa-job-check-au.component.html',
  styleUrls: ['./visa-job-check-au.component.scss']
})
export class VisaJobCheckAuComponent extends IntakeComponentTabBase implements OnInit, OnChanges {
  @Input() jobIndex: number;
  @Input() visaRecord: CandidateVisa;
  @Input() selectedJobCheck: CandidateVisaJobCheck;
  @Input() candidateIntakeData: CandidateIntakeData;

  candOccupations: CandidateOccupation[];
  candQualifications: CandidateEducation[];
  yrsExp: CandidateOccupation;
  ausDest: CandidateDestination;

  constructor(candidateService: CandidateService,
              countryService: CountryService,
              nationalityService: NationalityService,
              educationLevelService: EducationLevelService,
              occupationService: OccupationService,
              languageLevelService: LanguageLevelService,
              noteService: CandidateNoteService,
              authService: AuthService,
              private candidateEducationService: CandidateEducationService,
              private candidateOccupationService: CandidateOccupationService) {
    super(candidateService, countryService, nationalityService, educationLevelService,
      occupationService, languageLevelService, noteService, authService)
    this.candidateEducationService = candidateEducationService
    this.candidateOccupationService = candidateOccupationService
  }

  ngOnInit() {
    super.ngOnInit();

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
    // Get the candidate Australia destinations
    this.ausDest = this.candidateIntakeData.candidateDestinations.find(d => d.country.name === 'Australia')
  }

  ngOnChanges(changes: SimpleChanges) {

  }

  get currentYear(): string {
    return new Date().getFullYear().toString();
  }

  get birthYear(): string {
    return this.candidate?.dob.toString().slice(0, 4);
  }

  get selectedOccupations(): CandidateOccupation {
    if (this.candOccupations) {
      this.yrsExp = this.candOccupations?.find(occ => occ.occupation.id === this.selectedJobCheck?.occupation.id);
      return this.yrsExp;
    }
  }

}
