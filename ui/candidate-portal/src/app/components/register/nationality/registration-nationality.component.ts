import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {nationalities} from "../../../model/nationality";

@Component({
  selector: 'app-registration-nationality',
  templateUrl: './registration-nationality.component.html',
  styleUrls: ['./registration-nationality.component.scss']
})
export class RegistrationNationalityComponent implements OnInit {

  form: FormGroup;
  nationalities: string[];

  constructor(private fb: FormBuilder,
              private router: Router) { }

  ngOnInit() {
    this.nationalities = nationalities;
    this.form = this.fb.group({
      nationality: ['', Validators.required],
      registeredWithUN: ['', Validators.required],
      registrationId: ['', Validators.required]
    })
  }

  formValid() {
    const form = this.form.value;
    return form.nationality && form.registeredWithUN && (form.registeredWithUN === 'true' ? form.registrationId : true);
  }

  save() {
    // TODO save
    this.router.navigate(['register', 'profession']);
  }

}
