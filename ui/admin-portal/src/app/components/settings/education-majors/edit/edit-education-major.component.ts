import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {EducationMajor} from "../../../../model/education-major";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {EducationMajorService} from "../../../../services/education-major.service";

@Component({
  selector: 'app-edit-education-major',
  templateUrl: './edit-education-major.component.html',
  styleUrls: ['./edit-education-major.component.scss']
})
export class EditEducationMajorComponent implements OnInit {

  educationMajorId: number;
  educationMajorForm: FormGroup;
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private educationMajorService: EducationMajorService) {
  }

  ngOnInit() {
    this.loading = true;
    this.educationMajorService.get(this.educationMajorId).subscribe(educationMajor => {
      this.educationMajorForm = this.fb.group({
        name: [educationMajor.name, Validators.required],
        status: [educationMajor.status, Validators.required],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.educationMajorService.update(this.educationMajorId, this.educationMajorForm.value).subscribe(
      (educationMajor) => {
        this.closeModal(educationMajor);
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
