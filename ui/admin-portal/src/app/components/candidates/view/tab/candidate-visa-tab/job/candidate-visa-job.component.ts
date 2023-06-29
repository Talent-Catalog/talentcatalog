import {Component, Input, OnInit} from '@angular/core';
import {ShortJob} from "../../../../../../model/job";
import {HasNameSelectorComponent} from "../../../../../util/has-name-selector/has-name-selector.component";
import {
  CandidateVisaJobService,
  CreateCandidateVisaJobRequest
} from "../../../../../../services/candidate-visa-job.service";
import {ConfirmationComponent} from "../../../../../util/confirm/confirmation.component";
import {Candidate, CandidateIntakeData, CandidateVisa, CandidateVisaJobCheck} from "../../../../../../model/candidate";
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
  error: string;
  form: FormGroup;
  selectedIndex: number;
  selectedJobCheck: CandidateVisaJobCheck;
  jobIndex: number;

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
    /**
     * IF there are no existing visa job checks, return all the jobs associated with their candidate opportunities.
     * ELSE filter those jobs out from the jobs associated with their candidate opportunites.
     */
    if (!this.visaRecord?.candidateVisaJobChecks) {
      return this.candidate.candidateOpportunities.map(co => co.jobOpp);
    } else {
      const existingJobIds: number [] = this.visaRecord.candidateVisaJobChecks
        .map(jobCheck => jobCheck.jobOpp.id);

      return this.candidate.candidateOpportunities
        .map(co => co.jobOpp)
        .filter(jo => !existingJobIds.includes(jo.id))
    }
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

  createVisaJobCheck(jobOpp: ShortJob) {
    this.loading = true;
    let request: CreateCandidateVisaJobRequest = {
      jobOppId: jobOpp.id,
    }
    this.candidateVisaJobService.create(this.visaRecord.id, request).subscribe(
      (jobCheck) => {
        this.visaRecord?.candidateVisaJobChecks?.push(jobCheck);
        this.form.controls['jobIndex'].patchValue(this.visaRecord?.candidateVisaJobChecks?.lastIndexOf(jobCheck));
        this.changeJob(null);
        this.selectedJobCheck = jobCheck;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });

  }

  deleteJob(i: number) {
    const confirmationModal = this.modalService.open(ConfirmationComponent);
    const visaJobCheck: CandidateVisaJobCheck = this.visaRecord.candidateVisaJobChecks[i];
    confirmationModal.componentInstance.message =
      "Are you sure you want to delete the job check for " + visaJobCheck.jobOpp.name;
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
        this.changeJob(null);
        this.form.controls.jobIndex.patchValue(0);
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

  changeJob(event: Event) {
    this.jobIndex = this.form.controls.jobIndex.value;
    if (this.visaRecord.candidateVisaJobChecks) {
      this.selectedJobCheck = this.visaRecord.candidateVisaJobChecks[this.jobIndex];
    }
    //this.jobCheckAu.changeCheck(this.selectedJobCheck);
  }

}
