import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {EducationLevel} from "../../../../model/education-level";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {EducationLevelService} from "../../../../services/education-level.service";

@Component({
  selector: 'app-edit-education-level',
  templateUrl: './edit-education-level.component.html',
  styleUrls: ['./edit-education-level.component.scss']
})
export class EditEducationLevelComponent implements OnInit {

  educationLevelId: number;
  educationLevelForm: FormGroup;
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private educationLevelService: EducationLevelService) {
  }

  ngOnInit() {
    this.loading = true;
    this.educationLevelService.get(this.educationLevelId).subscribe(educationLevel => {
      this.educationLevelForm = this.fb.group({
        level: [educationLevel.level, Validators.required],
        name: [educationLevel.name, Validators.required],
        status: [educationLevel.status, Validators.required],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.educationLevelService.update(this.educationLevelId, this.educationLevelForm.value).subscribe(
      (educationLevel) => {
        this.closeModal(educationLevel);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(educationLevel: EducationLevel) {
    this.activeModal.close(educationLevel);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
