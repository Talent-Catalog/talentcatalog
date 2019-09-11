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
  professions: any[];
  hasSecondProfession: boolean;
  deleteAt: number;
  p: number;
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
    this.professions = [],
    this.hasSecondProfession = false;
    this.setUpForm();
  }

  setUpForm(){
    this.form = this.fb.group({
      field: ['', Validators.required],
      yearsOfExperience: ['', Validators.required],
    });
    console.log(this.form);
  }

  addMore(){
    console.log(this.form.value.field);
    this.hasSecondProfession = true;
    this.professions.push(this.form.value);
    this.setUpForm();
  }

  delete(p){
    this.professions = this.professions.filter(profession => profession !== p);
  }

  save() {
    console.log(this.professions);
    // TODO save
    this.router.navigate(['register', 'experience']);
  }

}
