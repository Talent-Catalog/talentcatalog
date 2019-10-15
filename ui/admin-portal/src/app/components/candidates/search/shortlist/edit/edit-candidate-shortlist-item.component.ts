import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {SavedSearch} from "../../../../../model/saved-search";
import {CandidateShortlistService} from "../../../../../services/candidate-shortlist.service";
import {CandidateShortlistItem} from "../../../../../model/candidate-shortlist-item";

@Component({
  selector: 'app-edit-candidate-shortlist-item',
  templateUrl: './edit-candidate-shortlist-item.component.html',
  styleUrls: ['./edit-candidate-shortlist-item.component.scss']
})
export class EditCandidateShortlistItemComponent implements OnInit {

  savedSearch: SavedSearch;
  candidateShortListItemId: number;

  form: FormGroup;

  candidateId: number;
  error;
  loading: boolean;
  saving: boolean;
  candidateShortListItem : CandidateShortlistItem;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateShortlistService: CandidateShortlistService ) {
  }

  ngOnInit() {
    this.loading = true;


    this.form = this.fb.group({
      candidateId: [this.candidateId],
      savedSearchId: [this.savedSearch.id],
      shortlistStatus: [null, [Validators.required]],
      comment: [null]
    });

    if (this.candidateShortListItemId){
      this.candidateShortlistService.get(this.candidateShortListItemId).subscribe(item => {
        this.candidateShortListItem = item;
        this.form.controls["shortlistStatus"].patchValue(item.shortlistStatus);
        this.form.controls["comment"].patchValue(item.comment);
        this.loading = false;
      });
    } else {

    }
    this.loading = false;
  }

  onSave() {
    this.saving = true;
    if (this.candidateShortListItemId){
      this.candidateShortlistService.update(this.candidateShortListItemId, this.form.value).subscribe(
        (candidateShortlistItem) => {
          this.closeModal(candidateShortlistItem);
          this.saving = false;
        },
        (error) => {
          this.error = error;
          this.saving = false;
        });
    } else {
      this.candidateShortlistService.create(this.form.value).subscribe(
        (candidateShortlistItem) => {
          this.closeModal(candidateShortlistItem);
          this.saving = false;
        },
        (error) => {
          this.error = error;
          this.saving = false;
        });
    }

  }

  closeModal(candidateShortlistItem: CandidateShortlistItem) {
    this.activeModal.close(candidateShortlistItem);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
