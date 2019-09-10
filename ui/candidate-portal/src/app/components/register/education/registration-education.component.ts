import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";

@Component({
  selector: 'app-registration-education',
  templateUrl: './registration-education.component.html',
  styleUrls: ['./registration-education.component.scss']
})
export class RegistrationEducationComponent implements OnInit {

  form: FormGroup;
  education: string;
  educationLevels: {id: string, label: string}[] = [
      {id: 'lessHighSchool', label: 'Less than high school'},
      {id: 'highSchool', label: 'Completed High School'},
      {id: 'bachelorsDegree', label: "Have a Bachelor's Degree"},
      {id: 'mastersDegree', label: "Have a Master's Degree"},
      {id: 'doctorateDegree', label: 'Have a Doctorate Degree'}
    ];

  constructor(private fb: FormBuilder,
              private router: Router) { }

  ngOnInit() {
    this.form = this.fb.group({
      educationLevel: ['', Validators.required]
    });
  };

    save() {
      this.education = this.form.value.educationLevel;
      // TODO save
      if(this.education == 'mastersDegree' || this.education == 'doctorateDegree'){
        this.router.navigate(['register', 'education', 'masters']);
      }else if(this.education == 'bachelorsDegree'){
        this.router.navigate(['register', 'education', 'university']);
      }else{
        this.router.navigate(['register', 'education', 'school']);
      }
    };

}
