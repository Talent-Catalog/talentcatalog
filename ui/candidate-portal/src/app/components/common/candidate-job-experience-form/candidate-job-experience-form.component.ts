import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CandidateJobExperience} from "../../../model/candidate-job-experience";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Country} from "../../../model/country";
import {CountryService} from "../../../services/country.service";
import {CandidateOccupation} from "../../../model/candidate-occupation";
import {CandidateOccupationService} from "../../../services/candidate-occupation.service";

@Component({
  selector: 'app-candidate-work-experience-form',
  templateUrl: './candidate-job-experience-form.component.html',
  styleUrls: ['./candidate-job-experience-form.component.scss']
})
export class CandidateJobExperienceFormComponent implements OnInit {

  @Input() experience: CandidateJobExperience;
  @Input() occupation: CandidateOccupation;
  @Input() occupations: CandidateOccupation[];
  @Input() countries: Country[];

  @Output() saved = new EventEmitter<CandidateJobExperience>();

  loading: boolean;
  saving: boolean;
  error: any;

  form: FormGroup;

  constructor(private fb: FormBuilder,
              private countryService: CountryService,
              private candidateOccupationService: CandidateOccupationService) { }

  ngOnInit() {
    this.loading = false;
    this.saving = false;
    this.error = null;

    /* Load missing countries */
    if (!this.countries) {
      this.loading = true;
      this.countryService.listCountries().subscribe(
        (response) => {
          this.loading = false;
          this.countries = response
        },
        (error) => {
          this.error = error;
          this.loading = false;
        });
    }

    /* Load missing candidate occupations */
    this.candidateOccupationService.listMyOccupations().subscribe(
      (response) => {
        this.occupations = response;
      },
      (error) => {
        this.error = error;
      });

    this.form = this.fb.group({
      companyName: ['', Validators.required],
      country: ['', Validators.required],
      candidateOccupationId: ['', Validators.required],
      role: ['', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      fullTime: [null, Validators.required],
      paid: [null, Validators.required],
      description: ['', Validators.required]
    });

    /* Patch form with candidates occupation */
    if (this.experience) {
      const exp = this.experience;
      this.form.controls['candidateOccupationId'].patchValue(exp.candidateOccupation.id || exp.candidateOccupationId);
    } else if (this.occupation) {
      this.form.controls['candidateOccupationId'].patchValue(this.occupation.id);
    }
  }

  save() {
    this.saving = true;
    const request = this.form.value;
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
    this.saved.emit(request);
  }

}
