import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {countries} from "../../../model/countries";

@Component({
  selector: 'app-registration-school',
  templateUrl: './registration-school.component.html',
  styleUrls: ['./registration-school.component.scss']
})
export class RegistrationSchoolComponent implements OnInit {

  form: FormGroup;
  // TODO create list of years
  countries: string[];
  years: number[] = [
    1991,1992,1993,1994
  ]

  constructor(private fb: FormBuilder,
              private router: Router ) { }

  ngOnInit() {
    this.countries = countries;
    this.form = this.fb.group({
      schoolName: ['', Validators.required],
      countryStudied: ['', Validators.required],
      completedSchool: [false, Validators.required],
      graduationYear: ['', Validators.required]
    })
  }

  save() {
  // TODO save
     this.router.navigate(['register', 'language']);
  }

}
