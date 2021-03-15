import {Component, Input} from '@angular/core';
import {Candidate, CandidateIntakeData, CandidateJobCheck, CandidateVisaCheck} from '../../../../model/candidate';
import {Nationality} from '../../../../model/nationality';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {CandidateVisaCheckService} from '../../../../services/candidate-visa-check.service';
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

@Component({
  selector: 'app-visa-job-assessments',
  templateUrl: './visa-job-assessments.component.html',
  styleUrls: ['./visa-job-assessments.component.scss']
})
export class VisaJobAssessmentsComponent extends IntakeComponentTabBase {

  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaRecord: CandidateVisaCheck;
  loading: boolean;
  form: FormGroup;
  @Input() nationalities: Nationality[];
  saving: boolean;
  jobIndex: number;
  selectedJobCheck: CandidateJobCheck;
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
             private candidateVisaCheckService: CandidateVisaCheckService,
             private modalService: NgbModal,
             private fb: FormBuilder) {
    super(candidateService, countryService, nationalityService, educationLevelService, occupationService, languageLevelService, noteService, authService)
  }

  onDataLoaded(init: boolean) {
    if (init) {
       this.visaRecord.jobChecks = [{
         name: 'Accountant - NAB'
       }, {
         name: 'Chartered Accountant - Comm Bank'
       }]
      this.currentYear = new Date().getFullYear().toString();
      this.birthYear = this.candidate.dob.toString().slice(0, 4);

      //If we have some visa checks, select the first one
      if (this.visaRecord?.jobChecks.length > 0) {
        this.jobIndex = 0;
      }
      this.form = this.fb.group({
        jobName: [this.jobIndex]
      });

      this.changeJobOpp(null);
    }
  }

  addRecord() {
    const modal = this.modalService.open(CreateVisaJobAssessementComponent);

    modal.result
      .then((selection: string) => {
        if (selection) {
          this.visaRecord.jobChecks.push({
            name: selection
          });
        }
      })
      .catch(() => {
        //User cancelled selection
      });
  }

  createRecord(selection: string) {
    this.loading = true;
    this.visaRecord.jobChecks.push({
      name: selection
    });
    // this.candidateVisaCheckService.create(this.candidate.id, request)
    //   .subscribe(
    //     (visaCheck) => {
    //       this.candidateIntakeData.candidateVisaChecks.push(visaCheck)
    //       this.loading = false;
    //     },
    //     (error) => {
    //       this.error = error;
    //       this.loading = false;
    //     });

  }

  deleteRecord(i: number) {
    const confirmationModal = this.modalService.open(ConfirmationComponent);
    const visaCheck: CandidateVisaCheck = this.candidateIntakeData.candidateVisaChecks[i];

    confirmationModal.componentInstance.message =
      "Are you sure you want to delete the visa check for " + visaCheck.country.name;
    confirmationModal.result
      .then((result) => {
        if (result === true) {
          this.doDelete(i, visaCheck);
        }
      })
      .catch(() => {});
  }

  private doDelete(i: number, visaCheck: CandidateVisaCheck) {
    this.loading = true;
    // this.candidateVisaCheckService.delete(visaCheck.id).subscribe(
    //   (done) => {
    //     this.loading = false;
    //     this.candidateIntakeData.candidateVisaChecks.splice(i, 1);
    //     this.changeVisaCountry(null);
    //   },
    //   (error) => {
    //     this.error = error;
    //     this.loading = false;
    //   });
  }

  changeJobOpp(event: Event) {
    this.jobIndex = this.form.controls.jobName.value;
    if (this.visaRecord.jobChecks) {
      this.selectedJobCheck = this.visaRecord.jobChecks[this.jobIndex];
    }
  }

  get selectedCountry(): string {
    return this.visaRecord?.country?.name;
  }

}
