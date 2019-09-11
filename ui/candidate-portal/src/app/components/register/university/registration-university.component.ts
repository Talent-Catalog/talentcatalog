import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {countries} from "../../../model/countries";

@Component({
  selector: 'app-registration-university',
  templateUrl: './registration-university.component.html',
  styleUrls: ['./registration-university.component.scss']
})
export class RegistrationUniversityComponent implements OnInit {

  form: FormGroup;
  countries: string[];
  // TODO create list of years
  years: number[] = [
    1991,1992,1993,1994
  ]

  constructor(private fb: FormBuilder,
              private router: Router ) { }

  ngOnInit() {
    this.countries = countries;
    this.form = this.fb.group({
      degree: ['', Validators.required],
      countryStudied: ['', Validators.required],
      university: ['', Validators.required],
      major: ['', Validators.required],
      graduationYear: ['', Validators.required]
     })
  }

  save() {
  // TODO save
     this.router.navigate(['register', 'education', 'school']);
  }

}
