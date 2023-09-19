import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
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
  @Input() visaCheckRecord: CandidateVisa;
  @Output() selectedJobIndex = new EventEmitter<number>();
  selectedIndex: number;
  loading: boolean;
  error: string;
  form: FormGroup;

  constructor(private candidateVisaCheckService: CandidateVisaCheckService,
              private candidateVisaJobService: CandidateVisaJobService,
              private modalService: NgbModal,
              private localStorageService: LocalStorageService,
              private fb: FormBuilder) { }

  ngOnInit(): void {
    if (this.hasJobChecks) {
      //If exists, get the last selected visa check from local storage. If nothing there, get the first one.
      const memoryIndex: number = this.localStorageService.get('VisaJobCheckIndex');
      if (memoryIndex) {
        this.selectedIndex = memoryIndex;
      } else {
        this.selectedIndex = 0;
      }
    }
    this.form = this.fb.group({
      jobIndex: [this.selectedIndex]
    });
    this.selectedJobIndex.emit(this.selectedIndex);
  }

  private get filteredSfJobs(): ShortJob[] {
    /**
     * IF there are no existing visa job checks, return all the jobs associated with the candidate's candidate opportunities.
     * ELSE filter those jobs out from the jobs associated with their candidate opportunities
     * SO that we avoid double ups of visa job checks for the same job.
     */
    if (!this.hasJobChecks) {
      return this.candidate.candidateOpportunities.map(co => co.jobOpp);
    } else {
      /**
       * NOTE: Some job checks don't have a SF Job Opp associated as these were entered in an earlier version of the code.
       */
      const existingJobIds: number [] = this.visaCheckRecord.candidateVisaJobChecks
        .map(jobCheck => jobCheck?.jobOpp?.id);

      return this.candidate.candidateOpportunities
        .map(co => co.jobOpp)
        .filter(jo => !existingJobIds.includes(jo.id))
    }
  }

  get hasJobOpps() {
    return this.candidate.candidateOpportunities?.length > 0;
  }

  get hasJobChecks() {
    return this.visaCheckRecord?.candidateVisaJobChecks?.length > 0;
  }

  addJob() {
    const modal = this.modalService.open(HasNameSelectorComponent);
    modal.componentInstance.hasNames = this.filteredSfJobs;
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
    this.candidateVisaJobService.create(this.visaCheckRecord.id, request).subscribe(
      (jobCheck) => {
        this.visaCheckRecord?.candidateVisaJobChecks?.push(jobCheck);
        this.form.controls['jobIndex'].patchValue(this.visaCheckRecord?.candidateVisaJobChecks?.lastIndexOf(jobCheck));
        this.setSelectedIndex()
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });

  }

  deleteJob(i: number) {
    const confirmationModal = this.modalService.open(ConfirmationComponent);
    const visaJobCheck: CandidateVisaJobCheck = this.visaCheckRecord.candidateVisaJobChecks[i];
    const jobName = visaJobCheck.jobOpp ? visaJobCheck.jobOpp.name : visaJobCheck.name
    confirmationModal.componentInstance.message =
      "Are you sure you want to delete the job check for " + jobName + '?';
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
        this.visaCheckRecord.candidateVisaJobChecks.splice(i, 1);
        this.form.controls.jobIndex.patchValue(0);
        this.setSelectedIndex();
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

  /**
   * This emits the selected job index to the outer component to display AND stores the selected index in the local storage.
   */
  setSelectedIndex() {
    this.selectedIndex = this.form.controls.jobIndex.value;
    this.selectedJobIndex.emit(this.selectedIndex);
    this.localStorageService.set('VisaJobCheckIndex', this.selectedIndex)
  }
}
