import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../../services/candidate.service";
import {CountryService} from "../../../../../services/country.service";
import {Candidate} from "../../../../../model/candidate";

@Component({
  selector: 'app-edit-candidate-media-willingness',
  templateUrl: './edit-candidate-media-willingness.component.html',
  styleUrls: ['./edit-candidate-media-willingness.component.scss']
})
export class EditCandidateMediaWillingnessComponent implements OnInit {

  candidateId: number;

  candidateForm: UntypedFormGroup;

  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private candidateService: CandidateService,
              private countryService: CountryService ) {
  }

  ngOnInit() {
    this.loading = true;

    this.candidateService.get(this.candidateId).subscribe(candidate => {
      this.candidateForm = this.fb.group({
        mediaWillingness: [candidate.mediaWillingness],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.candidateService.updateMedia(this.candidateId, this.candidateForm.value).subscribe(
      (candidate) => {
        this.closeModal(candidate);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidate: Candidate) {
    this.activeModal.close(candidate);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}
