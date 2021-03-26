import {Component, Input, ViewChild} from '@angular/core';
import {Candidate, CandidateIntakeData, CandidateVisa, CandidateVisaJobCheck} from '../../../../model/candidate';
import {Nationality} from '../../../../model/nationality';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ConfirmationComponent} from '../../../util/confirm/confirmation.component';
import {CreateVisaJobAssessementComponent} from './modal/create-visa-job-assessement.component';
import {FormBuilder, FormGroup} from '@angular/forms';
import {IntakeComponentTabBase} from '../../../util/intake/IntakeComponentTabBase';
import {CandidateService} from '../../../../services/candidate.service';
import {CountryService} from '../../../../services/country.service';
import {NationalityService} from '../../../../services/nationality.service';
import {EducationLevelService} from '../../../../services/education-level.service';
import {OccupationService} from '../../../../services/occupation.service';
import {LanguageLevelService} from '../../../../services/language-level.service';
import {CandidateNoteService} from "../../../../services/candidate-note.service";
import {AuthService} from "../../../../services/auth.service";
import {CandidateVisaJobService, CreateCandidateVisaJobRequest} from "../../../../services/candidate-visa-job.service";
import {VisaJobAssessmentAuComponent} from "./au/visa-job-assessment-au.component";

@Component({
  selector: 'app-visa-job-assessments',
  templateUrl: './visa-job-assessments.component.html',
  styleUrls: ['./visa-job-assessments.component.scss']
})
export class VisaJobAssessmentsComponent extends IntakeComponentTabBase {

  @ViewChild('jobCheck', {static: true}) jobCheckAu: VisaJobAssessmentAuComponent;

  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaRecord: CandidateVisa;
  loading: boolean;
  form: FormGroup;
  @Input() nationalities: Nationality[];
  saving: boolean;
  jobIndex: number;
  selectedJobCheck: CandidateVisaJobCheck;
  currentYear: string;
  birthYear: string;

  constructor(candidateService: CandidateService,
             countryService: CountryService,
             nationalityService: NationalityService,
             educationLevelService: EducationLevelService,
             occupationService: OccupationService,
             languageLevelService: LanguageLevelService,
             noteService: CandidateNoteService,
             authService: AuthService,
             private candidateVisaJobService: CandidateVisaJobService,
             private modalService: NgbModal,
             private fb: FormBuilder) {
    super(candidateService, countryService, nationalityService, educationLevelService, occupationService, languageLevelService, noteService, authService)
  }

  onDataLoaded(init: boolean) {
    if (init) {
      if (this.visaRecord) {
        this.currentYear = new Date().getFullYear().toString();
        this.birthYear = this.candidate.dob.toString().slice(0, 4);

        //If we have some visa checks, select the first one
        if (this.visaRecord?.candidateVisaJobChecks?.length > 0) {
          this.jobIndex = 0;
        }
      }

      this.form = this.fb.group({
        jobIndex: [this.jobIndex]
      });

      this.changeJobOpp(null);
    }
  }

  addRecord() {
    const modal = this.modalService.open(CreateVisaJobAssessementComponent);

    modal.result
      .then((request: CreateCandidateVisaJobRequest) => {
        if (request) {
          this.createRecord(request)
        }
      })
      .catch(() => {
        //User cancelled selection
      });
  }

  createRecord(request: CreateCandidateVisaJobRequest) {
    this.loading = true;
    this.candidateVisaJobService.create(this.visaRecord.id, request)
      .subscribe(
        (jobCheck) => {
          this.visaRecord?.candidateVisaJobChecks?.push(jobCheck)
          this.loading = false;
        },
        (error) => {
          this.error = error;
          this.loading = false;
        });

  }

  deleteRecord(i: number) {
    const confirmationModal = this.modalService.open(ConfirmationComponent);
    const visaJobCheck: CandidateVisaJobCheck = this.visaRecord.candidateVisaJobChecks[i];

    confirmationModal.componentInstance.message =
      "Are you sure you want to delete the job check for " + visaJobCheck.name;
    confirmationModal.result
      .then((result) => {
        if (result === true) {
          this.doDelete(i, visaJobCheck);
        }
      })
      .catch(() => {});
  }

  private doDelete(i: number, visaJobCheck: CandidateVisaJobCheck) {
    this.loading = true;
    this.candidateVisaJobService.delete(visaJobCheck.id).subscribe(
      (done) => {
        this.loading = false;
        this.visaRecord.candidateVisaJobChecks.splice(i, 1);
        this.changeJobOpp(null);
        this.form.controls.jobIndex.patchValue(0);
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

  changeJobOpp(event: Event) {
    this.jobIndex = this.form.controls.jobIndex.value;
    if (this.visaRecord.candidateVisaJobChecks) {
      this.selectedJobCheck = this.visaRecord.candidateVisaJobChecks[this.jobIndex];
    }
    //this.jobCheckAu.changeCheck(this.selectedJobCheck);
  }

  get selectedCountry(): string {
    return this.visaRecord?.country?.name;
  }

}
