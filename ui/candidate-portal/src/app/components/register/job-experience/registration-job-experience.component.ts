import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {CandidateJobExperienceService} from "../../../services/candidate-job-experience.service";
import {CandidateJobExperience} from "../../../model/candidate-job-experience";
import {Country} from "../../../model/country";
import {CountryService} from "../../../services/country.service";
import {CandidateOccupation} from "../../../model/candidate-occupation";
import {CandidateOccupationService} from "../../../services/candidate-occupation.service";
import {RegistrationService} from "../../../services/registration.service";

@Component({
  selector: 'app-registration-job-experience',
  templateUrl: './registration-job-experience.component.html',
  styleUrls: ['./registration-job-experience.component.scss']
})
export class RegistrationJobExperienceComponent implements OnInit {

  error: any;
  loading: boolean;
  saving: boolean;
  form: FormGroup;
  candidateJobExperiences: CandidateJobExperience[];
  countries: Country[];
  occupations: CandidateOccupation[];
  startDate: Date;
  e: number;

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private candidateOccupationService: CandidateOccupationService,
              private jobExperienceService: CandidateJobExperienceService,
              private countryService: CountryService,
              public registrationService: RegistrationService) { }


  ngOnInit() {
    this.countries = [];
    this.occupations = [];
    this.candidateJobExperiences = [];
    this.saving = false;
    this.loading = true;

    /* Load the candidate data */
    this.candidateService.getCandidateJobExperiences().subscribe(
      (candidate) => {
        console.log(candidate);
        this.candidateJobExperiences = candidate.candidateJobExperiences || [];

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

        /* Wait for the candidate then load the candidate's occupations */
        this.candidateOccupationService.listMyOccupations().subscribe(
          (response) => {
            this.occupations = response;
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
      candidateOccupationId: ['', Validators.required],
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
    this.jobExperienceService.createJobExperience(this.form.value).subscribe(
      (response) => {
        this.candidateJobExperiences.push(response);
        this.setUpForm();
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    );
    }

  delete(experience){
    this.saving = true;
    this.jobExperienceService.deleteJobExperience(experience.id).subscribe(
      () => {
        this.candidateJobExperiences = this.candidateJobExperiences.filter(e => e !== experience);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    );
  }

  save(dir: string) {
    if (dir === 'next') {
      this.registrationService.next();
    } else {
      this.registrationService.back();
    }
  }

  back() {
    this.save('back');
  }

  next() {
    this.save('next');
  }
}
