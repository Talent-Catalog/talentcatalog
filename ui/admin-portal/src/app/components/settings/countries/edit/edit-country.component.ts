import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Country} from "../../../../model/country";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CountryService} from "../../../../services/country.service";

@Component({
  selector: 'app-edit-country',
  templateUrl: './edit-country.component.html',
  styleUrls: ['./edit-country.component.scss']
})
export class EditCountryComponent implements OnInit {

  countryId: number;
  countryForm: FormGroup;
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private countryService: CountryService) {
  }

  ngOnInit() {
    this.loading = true;
    this.countryService.get(this.countryId).subscribe(country => {
      this.countryForm = this.fb.group({
        name: [country.name, Validators.required],
        status: [country.status, Validators.required],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.countryService.update(this.countryId, this.countryForm.value).subscribe(
      (country) => {
        this.closeModal(country);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(country: Country) {
    this.activeModal.close(country);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
