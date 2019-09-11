import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {countries} from "../../../model/countries";
import {years} from "../../../model/years";

@Component({
  selector: 'app-registration-masters',
  templateUrl: './registration-masters.component.html',
  styleUrls: ['./registration-masters.component.scss']
})
export class RegistrationMastersComponent implements OnInit {

  form: FormGroup;
  countries: string[];
  years: number[];

  constructor(private fb: FormBuilder,
                          private router: Router ) { }

  ngOnInit() {
    this.countries = countries;
    this.years = years;
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
     console.log(this.form)
     this.router.navigate(['register', 'education', 'university']);
  }

}
