import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {WorkExperienceService} from "../../../services/work-experience.service";
import {WorkExperience} from "../../../model/work-experience";
import {Country} from "../../../model/country";
import {CountryService} from "../../../services/country.service";

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
  experiences: WorkExperience[];
  countries: Country[];
  startDate: Date;
  e: number;

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private workExperienceService: WorkExperienceService,
              private countryService: CountryService) { }


  ngOnInit() {
    this.countries = [];
    this.experiences = [];
    this.saving = false;
    this.loading = true;

    /* Load the candidate data */
    this.candidateService.getCandidateWorkExperiences().subscribe(
      (candidate) => {
        console.log(candidate);
        this.experiences = candidate.workExperiences || [];

        /* Wait for the candidate then load the countries */
        this.countryService.listCountries().subscribe(
          (response) => {
            this.countries = response;
            this.loading = false;
            this.setUpForm();
          },
          (error) => {
            this.error = error;
            this.loading = false;
          }
        );
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  setUpForm(){
    this.form = this.fb.group({
      companyName: ['', Validators.required],
      country: ['', Validators.required],
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

  delete(experience){
    this.saving = true;
    this.workExperienceService.deleteWorkExperience(experience.id).subscribe(
      () => {
        this.experiences = this.experiences.filter(e => e !== experience);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    );
  }

  save() {
    // TODO save
    console.log(this.experiences);
    this.router.navigate(['register', 'education']);
  }

}
