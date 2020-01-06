import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Occupation} from "../../../../model/occupation";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {OccupationService} from "../../../../services/occupation.service";

@Component({
  selector: 'app-edit-occupation',
  templateUrl: './edit-occupation.component.html',
  styleUrls: ['./edit-occupation.component.scss']
})
export class EditOccupationComponent implements OnInit {

  occupationId: number;
  occupationForm: FormGroup;
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private occupationService: OccupationService) {
  }

  ngOnInit() {
    this.loading = true;
    this.occupationService.get(this.occupationId).subscribe(occupation => {
      this.occupationForm = this.fb.group({
        name: [occupation.name, Validators.required],
        status: [occupation.status, Validators.required],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.occupationService.update(this.occupationId, this.occupationForm.value).subscribe(
      (occupation) => {
        this.closeModal(occupation);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(occupation: Occupation) {
    this.activeModal.close(occupation);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
