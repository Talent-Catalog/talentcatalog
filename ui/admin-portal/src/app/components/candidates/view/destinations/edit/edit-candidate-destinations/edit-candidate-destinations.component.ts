import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateDestination} from "../../../../../../model/candidate-destination";
import {
  CandidateDestinationService,
  UpdateCandidateDestinationRequest
} from "../../../../../../services/candidate-destination.service";
import {EnumOption, enumOptions} from "../../../../../../util/enum";
import {YesNoUnsureLearn} from "../../../../../../model/candidate";

@Component({
  selector: 'app-edit-candidate-destinations',
  templateUrl: './edit-candidate-destinations.component.html',
  styleUrls: ['./edit-candidate-destinations.component.scss']
})
export class EditCandidateDestinationsComponent implements OnInit {
  candidateDestination: CandidateDestination;
  form: FormGroup;

  error;
  loading: boolean;
  saving: boolean;

  public destInterestOptions: EnumOption[] = enumOptions(YesNoUnsureLearn);

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateDestinationService: CandidateDestinationService) {
  }

  ngOnInit() {
    this.loading = true;
    this.form = this.fb.group({
      interest: [this.candidateDestination.interest],
      notes: [this.candidateDestination.notes]
    });
    this.loading = false;
  }

  onSave() {
    this.saving = true;
    let request: UpdateCandidateDestinationRequest = {
      interest: this.form.value.interest,
      notes: this.form.value.notes
    }
    this.candidateDestinationService.update(this.candidateDestination.id, request).subscribe(
      (candidateDestination) => {
        this.closeModal(candidateDestination);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidateDestination: CandidateDestination) {
    this.activeModal.close(candidateDestination);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}
