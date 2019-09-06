import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {Router} from "@angular/router";

@Component({
  selector: 'app-registration-additional-contact',
  templateUrl: './registration-additional-contact.component.html',
  styleUrls: ['./registration-additional-contact.component.scss']
})
export class RegistrationAdditionalContactComponent implements OnInit {

  form: FormGroup;

  constructor(private router: Router,
              private fb: FormBuilder) { }

  ngOnInit() {
    this.form = this.fb.group({
      phone: [''],
      whatsapp: ['']
    })
  }

  formValid() {
    return this.form.value.phone || this.form.value.whatsapp;
  }

  save() {
    // TODO save
    this.router.navigate(['register', 'personal']);
  }
}
