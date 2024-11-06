import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {Job, SearchJobRequest} from "../../../model/job";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {JobService} from "../../../services/job.service";
import {OpportunityOwnershipType} from "../../../model/opportunity";

@Component({
  selector: 'app-select-job-copy',
  templateUrl: './select-job-copy.component.html',
  styleUrls: ['./select-job-copy.component.scss']
})
export class SelectJobCopyComponent implements OnInit {

  form: UntypedFormGroup;
  loading: boolean;
  jobsToCopy: Job[];
  error: string;

  constructor(
    private activeModal: NgbActiveModal,
    private fb: UntypedFormBuilder,
    private jobService: JobService) { }

  ngOnInit(): void {
    this.loading = true;
    let searchRequest: SearchJobRequest = {
      sfOppClosed: true,
      jobNameAndIdOnly: true,
      ownedByMyPartner: true,
      ownershipType: OpportunityOwnershipType.AS_JOB_CREATOR,
    }
    this.jobService.search(searchRequest).subscribe(
      (response) => {
        this.jobsToCopy = response;
        this.loading = false;
      },
      error => {
        this.error = error;
      });

    this.form = this.fb.group({
      jobToCopyId: [],
    });
  }

  onCancel() {
    this.activeModal.dismiss();
  }

  onSelect() {
    this.activeModal.close(this.form.value.jobToCopyId);
  }

}
