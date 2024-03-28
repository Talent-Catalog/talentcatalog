import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateNoteService, CreateCandidateNoteRequest} from "../../../services/candidate-note.service";

@Component({
  selector: 'app-old-intake-input',
  templateUrl: './old-intake-input.component.html',
  styleUrls: ['./old-intake-input.component.scss']
})
export class OldIntakeInputComponent implements OnInit {

  candidateId: number;
  formName: string;

  form: FormGroup;

  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateNoteService: CandidateNoteService) {
  }

  ngOnInit() {
    this.form = this.fb.group({
      oldIntakeDate: [null],
      oldIntakeAdmin: [null],
    });
  }

  onSave() {
    this.saving = true;
    const noteRequest: CreateCandidateNoteRequest = {
      candidateId: this.candidateId,
      title: 'Original intake data entered: ' + this.formName + ' took place on ' + this.form.value.oldIntakeDate + ' by ' + this.form.value.oldIntakeAdmin + '.',
      comment: 'See details below on who/when this data was entered into the TC. Can find original document in candidates Google drive.'
    };
    this.activeModal.close(noteRequest);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
