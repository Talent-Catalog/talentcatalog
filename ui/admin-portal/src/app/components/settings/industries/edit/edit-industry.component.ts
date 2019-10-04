import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Industry} from "../../../../model/industry";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {IndustryService} from "../../../../services/industry.service";

@Component({
  selector: 'app-edit-industry',
  templateUrl: './edit-industry.component.html',
  styleUrls: ['./edit-industry.component.scss']
})
export class EditIndustryComponent implements OnInit {

  industryId: number;
  industryForm: FormGroup;
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private industryService: IndustryService) {
  }

  ngOnInit() {
    this.loading = true;
    this.industryService.get(this.industryId).subscribe(industry => {
      this.industryForm = this.fb.group({
        name: [industry.name, Validators.required],
        status: [industry.status, Validators.required],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.industryService.update(this.industryId, this.industryForm.value).subscribe(
      (industry) => {
        this.closeModal(industry);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(industry: Industry) {
    this.activeModal.close(industry);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
