import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService, DownloadCVRequest} from "../../../services/candidate.service";

/**
 * Modal component fills request to open/DL CV generated from given candidates profile.
 * Usage examples {@link ShowCandidatesComponent.downloadGeneratedCV},
 * {@link ViewCandidateComponent.downloadGeneratedCV}
 */
@Component({
  selector: 'app-download-cv',
  templateUrl: './download-cv.component.html',
  styleUrls: ['./download-cv.component.scss']
})
export class DownloadCvComponent implements OnInit {
  error = null;
  loading = null;
  saving = null;
  form: FormGroup;
  candidateId: number;

  constructor(private activeModal: NgbActiveModal,
              private candidateService: CandidateService,
              private fb: FormBuilder) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      name: [false],
      contact: [false],
    });
  }

  onSave() {
    const request: DownloadCVRequest = {
      candidateId: this.candidateId,
      showName: this.form.value.name,
      showContact: this.form.value.contact
    }
    const tab = window.open();
    this.candidateService.downloadCv(request).subscribe(
      result => {
        tab.location.href = URL.createObjectURL(result);
        this.closeModal()
      },
      error => {
        this.error = error;
      }
    );
  }

  closeModal() {
    this.activeModal.close();
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}
