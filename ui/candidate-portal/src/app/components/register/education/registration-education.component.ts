import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";

@Component({
  selector: 'app-registration-education',
  templateUrl: './registration-education.component.html',
  styleUrls: ['./registration-education.component.scss']
})
export class RegistrationEducationComponent implements OnInit {

  form: FormGroup;
  education: string;
  educationLevels: {id: string, label: string}[] = [
      {id: 'lessHighSchool', label: 'Less than High School'},
      {id: 'highSchool', label: 'Completed High School'},
      {id: 'bachelorsDegree', label: "Have a Bachelor's Degree"},
      {id: 'mastersDegree', label: "Have a Master's Degree"},
      {id: 'doctorateDegree', label: 'Have a Doctorate Degree'}
    ];
  error: any;
  // Component states
  loading: boolean;
  saving: boolean;

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService) { }

  ngOnInit() {
    this.loading = true;
    this.saving = false;
    this.form = this.fb.group({
      educationLevel: ['', Validators.required]
    });
    this.candidateService.getCandidateEducationLevel().subscribe(
      (response) => {
        this.form.patchValue({
          educationLevel: response.educationLevel,
        });
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  };

    save() {
      console.log(this.form);
      this.candidateService.updateCandidateEducationLevel(this.form.value).subscribe(
        (response) => {
          this.education = this.form.value.educationLevel;
          if(this.education == 'mastersDegree' || this.education == 'doctorateDegree'){
            this.router.navigate(['register', 'education', 'masters']);
          }else if(this.education == 'bachelorsDegree'){
            this.router.navigate(['register', 'education', 'university']);
          }else{
            this.router.navigate(['register', 'education', 'school']);
          }
        },
        (error) => {
          this.error = error;
        }
      );
    };

}
