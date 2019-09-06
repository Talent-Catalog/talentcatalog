import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";

@Component({
  selector: 'app-registration-personal',
  templateUrl: './registration-personal.component.html',
  styleUrls: ['./registration-personal.component.scss']
})
export class RegistrationPersonalComponent implements OnInit {

  form: FormGroup;

  constructor(private fb: FormBuilder,
              private router: Router) { }

  ngOnInit() {
    this.form = this.fb.group({
      firstName: [null, Validators.required],
      lastName: [null, Validators.required],
      gender: [null, Validators.required],
      dob: [null, Validators.required],
    })
  }

  save() {
    // TODO save
    this.router.navigate(['register', 'location'])
  }

}
