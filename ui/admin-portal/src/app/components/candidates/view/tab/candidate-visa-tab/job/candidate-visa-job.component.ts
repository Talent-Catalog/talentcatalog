import {Component, Input, OnInit} from '@angular/core';
import {ShortJob} from "../../../../../../model/job";
import {
  HasNameSelectorComponent
} from "../../../../../util/has-name-selector/has-name-selector.component";
import {
  CandidateVisaJobService,
  CreateCandidateVisaJobRequest
} from "../../../../../../services/candidate-visa-job.service";
import {ConfirmationComponent} from "../../../../../util/confirm/confirmation.component";
import {
  Candidate,
  CandidateIntakeData,
  CandidateVisa,
  CandidateVisaJobCheck
} from "../../../../../../model/candidate";
import {CandidateVisaCheckService} from "../../../../../../services/candidate-visa-check.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, FormGroup} from "@angular/forms";
import {LocalStorageService} from "angular-2-local-storage";

@Component({
  selector: 'app-candidate-visa-job',
  templateUrl: './candidate-visa-job.component.html',
  styleUrls: ['./candidate-visa-job.component.scss']
})
export class CandidateVisaJobComponent implements OnInit {
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaRecord: CandidateVisa;
  loading: boolean;
  form: FormGroup;
  selectedIndex: number;

  constructor(private candidateVisaCheckService: CandidateVisaCheckService,
              private candidateVisaJobService: CandidateVisaJobService,
              private modalService: NgbModal,
              private localStorageService: LocalStorageService,
              private fb: FormBuilder) { }

  ngOnInit(): void {
    if (this.visaRecord.candidateVisaJobChecks.length > 0) {
      //todo need to set index in local storage
      //If exists, get the last selected visa check from local storage. If nothing there, get the first one.
      const index: number = this.localStorageService.get('VisaJobCheckIndex');
      if (index) {
        this.selectedIndex = index;
      } else {
        this.selectedIndex = 0;
      }
    }
    this.form = this.fb.group({
      visaJob: [this.selectedIndex]
    });
  }

  private get filteredJobs(): ShortJob[] {
    //todo need to filter existing jobs
    return this.candidate.candidateOpportunities.map(co => co.jobOpp);
  }

  // addRecord() {
  //   const modal = this.modalService.open(CreateVisaJobAssessementComponent);
  //
  //   modal.result
  //   .then((request: CreateCandidateVisaJobRequest) => {
  //     if (request) {
  //       this.createRecord(request)
  //     }
  //   })
  //   .catch(() => {
  //     //User cancelled selection
  //   });
  // }

  createVisaJobCheck(jobId: number) {
    this.loading = true;
    let request: CreateCandidateVisaJobRequest = {
      jobId: jobId,
    }
    this.candidateVisaJobService.create(this.visaRecord.id, request)
    .subscribe(
      (jobCheck) => {
        this.visaRecord?.candidateVisaJobChecks?.push(jobCheck);
        this.form.controls['jobIndex'].patchValue(this.visaRecord?.candidateVisaJobChecks?.lastIndexOf(jobCheck));
        this.changeJobOpp(null);
        this.selectedJobCheck = jobCheck;
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

  addJob() {
    const modal = this.modalService.open(HasNameSelectorComponent);
    modal.componentInstance.hasNames = this.filteredJobs;
    modal.componentInstance.label = "Candidate's Job Opportunities";

    modal.result
    .then((selection: ShortJob) => {
      if (selection) {
        this.createVisaJobCheck(selection);
      }
    })
    .catch(() => {
      //User cancelled selection
    });
  }
  //
  // createRecord(job: ShortJob) {
  //   this.loading = true;
  //   const request: CreateCandidateVisaJobRequest = {
  //     name: job.name,
  //     jobId: job.id
  //   };
  //   this.candidateVisaJobService.create(this.candidate.id, request).subscribe(
  //     (visaCheck) => {
  //       this.candidateIntakeData.candidateVisaChecks.push(visaCheck);
  //       this.form.controls['visaCountry'].patchValue(this.candidateIntakeData.candidateVisaChecks.lastIndexOf(visaCheck));
  //       this.changeVisaCountry(null)
  //       this.loading = false;
  //     },
  //     (error) => {
  //       this.error = error;
  //       this.loading = false;
  //     });
  //
  // }
  //
  // deleteJob(i: number) {
  //   const confirmationModal = this.modalService.open(ConfirmationComponent);
  //   const visaCheck: CandidateVisa = this.candidateIntakeData.candidateVisaChecks[i];
  //
  //   confirmationModal.componentInstance.message =
  //     "Are you sure you want to delete the visa check for " + visaCheck.country.name;
  //   confirmationModal.result
  //   .then((result) => {
  //     if (result === true) {
  //       this.doDelete(i, visaCheck);
  //     }
  //   })
  //   .catch(() => {});
  // }
  //
  // private doDelete(i: number, visaCheck: CandidateVisa) {
  //   // this.loading = true;
  //   // this.candidateVisaCheckService.delete(visaCheck.id).subscribe(
  //   //   (done) => {
  //   //     this.loading = false;
  //   //     this.candidateIntakeData.candidateVisaChecks.splice(i, 1);
  //   //     this.changeVisaCountry(null);
  //   //   },
  //   //   (error) => {
  //   //     this.error = error;
  //   //     this.loading = false;
  //   //   });
  // }
  //
  // changeJob(event: Event) {
  //   //   this.selectedIndex = this.form.controls.visaCountry.value;
  //   //   this.selectedCountry = this.candidateIntakeData
  //   //     .candidateVisaChecks[this.selectedIndex]?.country?.name;
  // }

}
