import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Country} from "../../../../model/country";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CountryService} from "../../../../services/country.service";

@Component({
  selector: 'app-create-country',
  templateUrl: './create-country.component.html',
  styleUrls: ['./create-country.component.scss']
})

export class CreateCountryComponent implements OnInit {

  countryForm: FormGroup;
  error;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private countryService: CountryService) {
  }

  ngOnInit() {
    this.countryForm = this.fb.group({
      name: [null, Validators.required],
      status: [null, Validators.required],
    });
  }

  onSave() {
    this.saving = true;
    this.countryService.create(this.countryForm.value).subscribe(
      (country) => {
        this.closeModal(country)
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
