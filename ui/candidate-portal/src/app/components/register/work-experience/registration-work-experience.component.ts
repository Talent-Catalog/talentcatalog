import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
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
  selector: 'app-registration-work-experience',
  templateUrl: './registration-work-experience.component.html',
  styleUrls: ['./registration-work-experience.component.scss']
})
export class RegistrationWorkExperienceComponent implements OnInit {

  error: any;
  saving: boolean;
  _loading = {
    candidate: true,
    countries: true,
    occupations: true
  };

  form: FormGroup;
  candidateJobExperiences: CandidateJobExperience[];
  countries: Country[];
  occupations: CandidateOccupation[];
  addingWorkExperience: boolean;
  occupation: CandidateOccupation;
  experiencesByOccupation: {[id: number]: CandidateJobExperience[]};
  addingExperience: boolean;

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private candidateOccupationService: CandidateOccupationService,
              private jobExperienceService: CandidateJobExperienceService,
              private countryService: CountryService,
              public registrationService: RegistrationService) { }


  ngOnInit() {
    this.saving = false;
    this.countries = [];
    this.occupations = [];
    this.candidateJobExperiences = [];
    this.addingWorkExperience = false;
    this.experiencesByOccupation = {};
    this.addingExperience = false;

    /* Load the candidate data */
    this.candidateService.getCandidateJobExperiences().subscribe(
      (candidate) => {
        console.log(candidate);
        this.candidateJobExperiences = candidate.candidateJobExperiences || [];
        this._loading.candidate = false;

        /* Load the candidate's occupations */
        this.candidateOccupationService.listMyOccupations().subscribe(
          (response) => {
            this.occupations = response;

            /* Populate experience map */
            for (let occ of this.occupations) {
              this.experiencesByOccupation[occ.id] = this.candidateJobExperiences.filter(exp => exp.candidateOccupation.id === occ.id);
            }

            this._loading.occupations = false;
          },
          (error) => {
            this.error = error;
            this._loading.occupations = false;
          }
        );
      },
      (error) => {
        this.error = error;
        this._loading.candidate = false;
      }
    );

    /* Load countries */
    this.countryService.listCountries().subscribe(
      (response) => {
        this.countries = response;
        this._loading.countries = false;
      },
      (error) => {
        this.error = error;
        this._loading.countries = false;
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

  get loading() {
    const l = this._loading;
    return l.candidate || l.countries || l.occupations;
  }

  handleDelete(exp: CandidateJobExperience) {
    window.alert('This feature is still under construction.');
  }

  handleEdit(exp: CandidateJobExperience) {
    window.alert('This feature is still under construction.');
  }
}
