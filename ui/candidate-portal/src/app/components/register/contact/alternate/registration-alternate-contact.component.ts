import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {Router} from "@angular/router";

@Component({
  selector: 'app-registration-alternate-contact',
  templateUrl: './registration-alternate-contact.component.html',
  styleUrls: ['./registration-alternate-contact.component.scss']
})
export class RegistrationAlternateContactComponent implements OnInit {

  form: FormGroup;

  constructor(private fb: FormBuilder,
              private router: Router) { }

  ngOnInit() {
    this.form = this.fb.group({
      phone: [''],
      whatsapp: [''],
      email: ['']
    });
  }

  formValid() {
    const form = this.form.value;
    return form.phone || form.whatsapp || form.email;
  }

  save() {
    // TODO save
    this.router.navigate(['register', 'personal']);
  }

}
