import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {countries} from "../../../model/countries";
import {CandidateService} from "../../../services/candidate.service";
import {WorkExperienceService} from "../../../services/work-experience.service";
import {WorkExperience} from "../../../model/work-experience";

@Component({
  selector: 'app-registration-work-experience',
  templateUrl: './registration-work-experience.component.html',
  styleUrls: ['./registration-work-experience.component.scss']
})
export class RegistrationWorkExperienceComponent implements OnInit {

  error: any;
  loading: boolean;
  saving: boolean;

  form: FormGroup;
  countries: string[];
  experiences: WorkExperience[];

  startDate: Date;
  e: number;

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private workExperienceService: WorkExperienceService) { }


  ngOnInit() {
    this.countries = countries;
    this.experiences = []
    this.setUpForm();
  }

  setUpForm(){
    this.form = this.fb.group({
    companyName: ['', Validators.required],
    countryId: ['', Validators.required],
    role: ['', Validators.required],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    fullTime: [false, Validators.required],
    paid: [false, Validators.required],
    description: ['', Validators.required]
    })
  }

  addMore() {
    this.saving = true;
    this.workExperienceService.createWorkExperience(this.form.value).subscribe(
      (response) => {
        this.experiences.push(response);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    );
    console.log(this.form.value)
    this.setUpForm();
    }

  delete(e){
    this.experiences = this.experiences.filter(experience => experience !== e);
  }

  save() {
    // TODO save
    console.log(this.experiences);
    this.router.navigate(['register', 'education']);
  }

}
