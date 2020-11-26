import {Component, Input, OnInit} from '@angular/core';
import {Candidate, CandidateIntakeData, CandidateVisaCheck} from '../../../../model/candidate';
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

@Component({
  selector: 'app-visa-job-assessment',
  templateUrl: './visa-job-assessment.component.html',
  styleUrls: ['./visa-job-assessment.component.scss']
})
export class VisaJobAssessmentComponent extends IntakeComponentTabBase implements OnInit {

  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  loading: boolean;
  form: FormGroup;
  @Input() nationalities: Nationality[];
  saving: boolean;
  selectedIndex: number;
  selectedCountry: string;
  jobChecks: string[];

  constructor(candidateService: CandidateService,
             countryService: CountryService,
             nationalityService: NationalityService,
             educationLevelService: EducationLevelService,
             occupationService: OccupationService,
             languageLevelService: LanguageLevelService,
             private candidateVisaCheckService: CandidateVisaCheckService,
             private modalService: NgbModal,
             private fb: FormBuilder) {
    super(candidateService, countryService, nationalityService, educationLevelService, occupationService, languageLevelService)
  }

  onDataLoaded(init: boolean) {
    if (init) {
      //If we have some visa checks, select the first one
      if (this.jobChecks.length > 0) {
        this.selectedIndex = 0;
      }
      this.form = this.fb.group({
        jobName: [this.selectedIndex]
      });

      this.changeJobOpp(null);
    }
  }

  ngOnInit(): void {
    this.jobChecks = [];
    this.form = this.fb.group({
      jobName: [this.selectedIndex]
    });
  }

  addRecord() {
    const modal = this.modalService.open(CreateVisaJobAssessementComponent);

    modal.result
      .then((selection: string) => {
        if (selection) {
          this.jobChecks.push(selection);
        }
      })
      .catch(() => {
        //User cancelled selection
      });
  }

  createRecord(selection: string) {
    this.loading = true;
    this.jobChecks.push(selection);
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

  get jobCheckOpps() {
    return this.jobChecks;
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
    this.selectedIndex = this.form.controls.visaCountry.value;
    this.selectedCountry = this.candidateIntakeData
      .candidateVisaChecks[this.selectedIndex]?.country?.name;
  }

}
