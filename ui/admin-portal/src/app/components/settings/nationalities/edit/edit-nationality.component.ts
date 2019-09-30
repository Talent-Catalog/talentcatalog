import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Nationality} from "../../../../model/nationality";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {NationalityService} from "../../../../services/nationality.service";

@Component({
  selector: 'app-edit-nationality',
  templateUrl: './edit-nationality.component.html',
  styleUrls: ['./edit-nationality.component.scss']
})
export class EditNationalityComponent implements OnInit {

  nationalityId: number;
  nationalityForm: FormGroup;
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private nationalityService: NationalityService) {
  }

  ngOnInit() {
    this.loading = true;
    this.nationalityService.get(this.nationalityId).subscribe(nationality => {
      this.nationalityForm = this.fb.group({
        name: [nationality.name],
        status: [nationality.status],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.nationalityService.update(this.nationalityId, this.nationalityForm.value).subscribe(
      (nationality) => {
        this.closeModal(nationality);
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
