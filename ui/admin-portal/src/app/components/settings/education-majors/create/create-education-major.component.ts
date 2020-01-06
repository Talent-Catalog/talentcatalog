import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {EducationMajor} from "../../../../model/education-major";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {EducationMajorService} from "../../../../services/education-major.service";

@Component({
  selector: 'app-create-education-major',
  templateUrl: './create-education-major.component.html',
  styleUrls: ['./create-education-major.component.scss']
})

export class CreateEducationMajorComponent implements OnInit {

  educationMajorForm: FormGroup;
  error;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private educationMajorService: EducationMajorService) {
  }

  ngOnInit() {
    this.educationMajorForm = this.fb.group({
      name: [null, Validators.required],
      status: [null, Validators.required],
    });
  }

  onSave() {
    this.saving = true;
    this.educationMajorService.create(this.educationMajorForm.value).subscribe(
      (educationMajor) => {
        this.closeModal(educationMajor)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(educationMajor: EducationMajor) {
    this.activeModal.close(educationMajor);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
