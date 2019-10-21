import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CandidateJobExperience} from "../../../model/candidate-job-experience";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Country} from "../../../model/country";
import {CountryService} from "../../../services/country.service";
import {CandidateOccupation} from "../../../model/candidate-occupation";
import {CandidateOccupationService} from "../../../services/candidate-occupation.service";
import {CandidateJobExperienceService} from "../../../services/candidate-job-experience.service";

@Component({
  selector: 'app-candidate-work-experience-form',
  templateUrl: './candidate-job-experience-form.component.html',
  styleUrls: ['./candidate-job-experience-form.component.scss']
})
export class CandidateJobExperienceFormComponent implements OnInit {

  @Input() experience: CandidateJobExperience;
  @Input() candidateOccupation: CandidateOccupation;
  @Input() candidateOccupations: CandidateOccupation[];
  @Input() countries: Country[];

  @Output() formSaved = new EventEmitter<CandidateJobExperience>();
  @Output() formClosed = new EventEmitter<CandidateJobExperience>();

  loading: boolean;
  saving: boolean;
  error: any;

  form: FormGroup;

  constructor(private fb: FormBuilder,
              private countryService: CountryService,
              private candidateOccupationService: CandidateOccupationService,
              private jobExperienceService: CandidateJobExperienceService) { }

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
        this.candidateOccupations = response;
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
    } else if (this.candidateOccupation) {
      this.form.controls['candidateOccupationId'].patchValue(this.candidateOccupation.id);
    }
  }

  save() {
    this.saving = true;
    const request = this.form.value;
    this.jobExperienceService.createJobExperience(this.form.value).subscribe(
      (response) => {
        response.candidateOccupation = this.candidateOccupation;
        this.formSaved.emit(response);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    );
    this.formSaved.emit(request);
  }

}
