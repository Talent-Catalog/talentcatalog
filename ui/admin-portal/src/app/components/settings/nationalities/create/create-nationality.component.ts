import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Nationality} from "../../../../model/nationality";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {NationalityService} from "../../../../services/nationality.service";

@Component({
  selector: 'app-create-nationality',
  templateUrl: './create-nationality.component.html',
  styleUrls: ['./create-nationality.component.scss']
})
export class CreateNationalityComponent implements OnInit {

  nationalityForm: FormGroup;
  error;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private nationalityService: NationalityService) {
  }

  ngOnInit() {
    this.nationalityForm = this.fb.group({
      name: [null, Validators.required],
      status: [null, Validators.required],
    });
  }

  onSave() {
    this.saving = true;
    this.nationalityService.create(this.nationalityForm.value).subscribe(
      (nationality) => {
        this.closeModal(nationality)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(nationality: Nationality) {
    this.activeModal.close(nationality);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
