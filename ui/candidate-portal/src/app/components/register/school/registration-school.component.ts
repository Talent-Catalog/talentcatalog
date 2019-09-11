import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {countries} from "../../../model/countries";
import {years} from "../../../model/years";

@Component({
  selector: 'app-registration-school',
  templateUrl: './registration-school.component.html',
  styleUrls: ['./registration-school.component.scss']
})
export class RegistrationSchoolComponent implements OnInit {

  form: FormGroup;
  // TODO create list of years
  countries: string[];
  years: number[];

  constructor(private fb: FormBuilder,
              private router: Router ) { }

  ngOnInit() {
    this.countries = countries;
    this.years = years;
    this.form = this.fb.group({
      schoolName: ['', Validators.required],
      countryStudied: ['', Validators.required],
      completedSchool: [false, Validators.required],
      graduationYear: ['']
    })
  }

  save() {
  // TODO save
    console.log(this.form);
     this.router.navigate(['register', 'language']);
  }

}
