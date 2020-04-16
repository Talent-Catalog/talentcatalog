import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output
} from '@angular/core';
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
import {LangChangeEvent, TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-registration-work-experience',
  templateUrl: './registration-work-experience.component.html',
  styleUrls: ['./registration-work-experience.component.scss']
})
export class RegistrationWorkExperienceComponent implements OnInit, OnDestroy {

  @Input() edit: boolean;

  @Output() onSave = new EventEmitter();

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
  experiencesByCandidateOccupation: {[id: number]: CandidateJobExperience[]};

  experienceFormOpen: boolean;
  occupation: CandidateOccupation;
  experience: CandidateJobExperience;

  subscription;

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private candidateOccupationService: CandidateOccupationService,
              private jobExperienceService: CandidateJobExperienceService,
              private countryService: CountryService,
              private translateService: TranslateService,
              public registrationService: RegistrationService) { }


  ngOnInit() {
    this.saving = false;
    this.countries = [];
    this.occupations = [];
    this.candidateJobExperiences = [];
    this.experiencesByCandidateOccupation = {};
    this.experienceFormOpen = false;

    /* Load the candidate data */
    this.candidateService.getCandidateJobExperiences().subscribe(
      (candidate) => {
        /* Extract the country id and candidate occupation id and store the properties on the object */
        this.candidateJobExperiences = candidate.candidateJobExperiences
          .map(exp => Object.assign(exp, {
            countryId: exp.country.id,
            candidateOccupationId: exp.candidateOccupation.id
          })) || [];
        this._loading.candidate = false;

        /* Load the candidate's occupations */
        this.candidateOccupationService.listMyOccupations().subscribe(
          (response) => {
            this.occupations = response;

            /* Populate experience map */
            this.populateExperienceMap();

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

    this.loadDropDownData();
    //listen for change of language and save
    this.subscription = this.translateService.onLangChange.subscribe((event: LangChangeEvent) => {
      this.loadDropDownData();
    });


  }

  loadDropDownData(){
    this._loading.countries = true;

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

  closeEdit() {
    this.onSave.emit();
  }

  back() {
    this.registrationService.back();
  }

  next() {
    this.registrationService.next();
  }

  get loading() {
    const l = this._loading;
    return l.candidate || l.countries || l.occupations;
  }

  handleDelete(exp: CandidateJobExperience) {
    this.saving = true;
    this.jobExperienceService.deleteJobExperience(exp.id).subscribe(
      () => {
        this.candidateJobExperiences = this.candidateJobExperiences.filter(e => e.id !== exp.id);
        this.populateExperienceMap();
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    );
  }

  handleEdit(exp: CandidateJobExperience) {
    this.experience = exp;
    this.experienceFormOpen = true;
  }

  addExperience(occupation: CandidateOccupation) {
    this.occupation = occupation;
    this.experienceFormOpen = true;
  }

  handleCancelled(exp: CandidateJobExperience) {
    this.experienceFormOpen = false;
    this.occupation = null;
  }

  handleSave(exp: CandidateJobExperience) {
    const index = this.candidateJobExperiences.findIndex(candExperience => candExperience.id === exp.id);
    exp = Object.assign(exp, {
      countryId: exp.country.id,
      candidateOccupationId: exp.candidateOccupation.id
    });
    if (index >= 0) {
      this.candidateJobExperiences[index] = exp;
    } else {
      this.candidateJobExperiences.push(exp);
    }
    this.populateExperienceMap();
    this.experienceFormOpen = false;
  }

  private populateExperienceMap() {
    this.experiencesByCandidateOccupation = {};
    for (let occ of this.occupations) {
      this.experiencesByCandidateOccupation[occ.id] = this.candidateJobExperiences
        .filter(exp => exp.candidateOccupation.id === occ.id)
        .sort((a, b) => a.id > b.id ? -1 : 1);
    }
  }

  completedJobExperiences(): boolean {
    let completed = true;

    for (const occupation of this.occupations) {
      if (this.experiencesByCandidateOccupation[occupation.id].length === 0) {
        completed = false;
        break;
      }
    }

    return completed;
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }
}
