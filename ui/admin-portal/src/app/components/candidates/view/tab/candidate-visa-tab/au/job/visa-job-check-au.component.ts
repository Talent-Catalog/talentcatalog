import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {
  CandidateDestination,
  CandidateIntakeData,
  CandidateVisa,
  CandidateVisaJobCheck,
  getIeltsScoreTypeString
} from "../../../../../../../model/candidate";
import {IntakeComponentTabBase} from "../../../../../../util/intake/IntakeComponentTabBase";
import {CandidateService} from "../../../../../../../services/candidate.service";
import {CountryService} from "../../../../../../../services/country.service";
import {EducationLevelService} from "../../../../../../../services/education-level.service";
import {OccupationService} from "../../../../../../../services/occupation.service";
import {LanguageLevelService} from "../../../../../../../services/language-level.service";
import {CandidateNoteService} from "../../../../../../../services/candidate-note.service";
import {AuthService} from "../../../../../../../services/auth.service";
import {CandidateOccupationService} from "../../../../../../../services/candidate-occupation.service";
import {CandidateOccupation} from "../../../../../../../model/candidate-occupation";
import {CandidateEducationService} from "../../../../../../../services/candidate-education.service";
import {CandidateEducation} from "../../../../../../../model/candidate-education";
import {describeFamilyInDestination} from "../../../../../../../model/candidate-destination";

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
  familyInAus: string;

  constructor(candidateService: CandidateService,
              countryService: CountryService,
              educationLevelService: EducationLevelService,
              occupationService: OccupationService,
              languageLevelService: LanguageLevelService,
              noteService: CandidateNoteService,
              authService: AuthService,
              private candidateEducationService: CandidateEducationService,
              private candidateOccupationService: CandidateOccupationService) {
    super(candidateService, countryService, educationLevelService,
      occupationService, languageLevelService, noteService, authService)
    this.candidateEducationService = candidateEducationService
    this.candidateOccupationService = candidateOccupationService
  }

  ngOnInit() {
    console.log(this.visaRecord)
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
    //todo make the value not undefined if no family is there (e.g. put 'No family') instead of undefined in undefined
    this.ausDest = this.candidateIntakeData.candidateDestinations.find(d => d.country.name === 'Australia')

    this.familyInAus = describeFamilyInDestination(this.visaRecord?.country.id, this.candidateIntakeData);
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
      this.yrsExp = this.candOccupations?.find(occ => occ.occupation.id === this.selectedJobCheck?.occupation?.id);
      return this.yrsExp;
    }
  }

  get candidateAge(): number {
    if (this.candidate?.dob) {
      const timeDiff = Math.abs(Date.now() - new Date(this.candidate?.dob).getTime());
      return Math.floor(timeDiff / (1000 * 3600 * 24) / 365.25);
    }
  }

  get ieltsScoreType(): string {
    return getIeltsScoreTypeString(this.candidate);
  }

}
