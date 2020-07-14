import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {SavedSearch} from "../../../../model/saved-search";
import {CandidateReviewStatusService} from "../../../../services/candidate-review-status.service";
import {CandidateReviewStatusItem} from "../../../../model/candidate-review-status-item";

@Component({
  selector: 'app-edit-candidate-review-status-item',
  templateUrl: './edit-candidate-review-status-item.component.html',
  styleUrls: ['./edit-candidate-review-status-item.component.scss']
})
export class EditCandidateReviewStatusItemComponent implements OnInit {

  savedSearch: SavedSearch;
  candidateReviewStatusItemId: number;

  form: FormGroup;

  candidateId: number;
  error;
  loading: boolean;
  saving: boolean;
  candidateReviewStatusItem: CandidateReviewStatusItem;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateReviewStatusService: CandidateReviewStatusService ) {
  }

  ngOnInit() {
    this.loading = true;


    this.form = this.fb.group({
      candidateId: [this.candidateId],
      savedSearchId: [this.savedSearch.id],
      reviewStatus: [null, [Validators.required]],
      comment: [null]
    });

    if (this.candidateReviewStatusItemId){
      this.candidateReviewStatusService.get(this.candidateReviewStatusItemId).subscribe(item => {
        this.candidateReviewStatusItem = item;
        this.form.controls["reviewStatus"].patchValue(item.reviewStatus);
        this.form.controls["comment"].patchValue(item.comment);
        this.loading = false;
      });
    } else {

    }
    this.loading = false;
  }

  onSave() {
    this.saving = true;
    if (this.candidateReviewStatusItemId){
      this.candidateReviewStatusService.update(this.candidateReviewStatusItemId, this.form.value).subscribe(
        (candidateReviewStatusItem) => {
          this.closeModal(candidateReviewStatusItem);
          this.saving = false;
        },
        (error) => {
          this.error = error;
          this.saving = false;
        });
    } else {
      this.candidateReviewStatusService.create(this.form.value).subscribe(
        (candidateReviewStatusItem) => {
          this.closeModal(candidateReviewStatusItem);
          this.saving = false;
        },
        (error) => {
          this.error = error;
          this.saving = false;
        });
    }

  }

  closeModal(candidateReviewStatusItem: CandidateReviewStatusItem) {
    this.activeModal.close(candidateReviewStatusItem);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
