import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Occupation} from "../../../../model/occupation";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {OccupationService} from "../../../../services/occupation.service";

@Component({
  selector: 'app-create-occupation',
  templateUrl: './create-occupation.component.html',
  styleUrls: ['./create-occupation.component.scss']
})

export class CreateOccupationComponent implements OnInit {

  occupationForm: FormGroup;
  error;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private occupationService: OccupationService) {
  }

  ngOnInit() {
    this.occupationForm = this.fb.group({
      name: [null, Validators.required],
      status: [null, Validators.required],
    });
  }

  onSave() {
    this.saving = true;
    this.occupationService.create(this.occupationForm.value).subscribe(
      (occupation) => {
        this.closeModal(occupation)
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
