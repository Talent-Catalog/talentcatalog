import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";

@Component({
  selector: 'app-registration-profession',
  templateUrl: './registration-profession.component.html',
  styleUrls: ['./registration-profession.component.scss']
})
export class RegistrationProfessionComponent implements OnInit {

  form: FormGroup;
  fields: {id: string, label: string}[] = [
    {id: 'tech', label: 'IT / Accounting / etc'},
    {id: 'medicine', label: 'Medicine'}
  ];
  years: {id: string, label: string}[] = [
    {id: 'lessThanOne', label: 'Less than a year'},
    {id: 'oneToTwo', label: '1 to 2 years'},
    {id: 'threeToFive', label: '3 to 5 years'},
    {id: 'fiveToTen', label: '5 to 10 years'},
    {id: 'moreThanTen', label: 'More than 10 years'}
  ];

  constructor(private fb: FormBuilder,
              private router: Router) { }

  ngOnInit() {
    this.form = this.fb.group({
      field: ['', Validators.required],
      yearsOfExperience: ['', Validators.required],
      hasSecondProfession: ['', Validators.required]
    });
  }

  save() {
    // TODO save
    this.router.navigate(['register', 'experience']);
  }

}
