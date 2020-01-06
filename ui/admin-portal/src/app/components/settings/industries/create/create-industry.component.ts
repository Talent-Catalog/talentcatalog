import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Industry} from "../../../../model/industry";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {IndustryService} from "../../../../services/industry.service";

@Component({
  selector: 'app-create-industry',
  templateUrl: './create-industry.component.html',
  styleUrls: ['./create-industry.component.scss']
})

export class CreateIndustryComponent implements OnInit {

  industryForm: FormGroup;
  error;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private industryService: IndustryService) {
  }

  ngOnInit() {
    this.industryForm = this.fb.group({
      name: [null, Validators.required],
      status: [null, Validators.required],
    });
  }

  onSave() {
    this.saving = true;
    this.industryService.create(this.industryForm.value).subscribe(
      (industry) => {
        this.closeModal(industry)
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
