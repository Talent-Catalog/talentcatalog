import {AfterViewInit, Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {CandidateJobExperience} from "../../../model/candidate-job-experience";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Country} from "../../../model/country";
import {CountryService} from "../../../services/country.service";
import {CandidateOccupation} from "../../../model/candidate-occupation";
import {CandidateOccupationService} from "../../../services/candidate-occupation.service";
import {CandidateJobExperienceService} from "../../../services/candidate-job-experience.service";

@Component({
  selector: 'app-candidate-job-experience-form',
  templateUrl: './candidate-job-experience-form.component.html',
  styleUrls: ['./candidate-job-experience-form.component.scss']
})
export class CandidateJobExperienceFormComponent implements OnInit, AfterViewInit {

  @Input() candidateJobExperience: CandidateJobExperience;
  @Input() candidateOccupation: CandidateOccupation;
  @Input() candidateOccupations: CandidateOccupation[];
  @Input() countries: Country[];

  @Output() formSaved = new EventEmitter<CandidateJobExperience>();
  @Output() formClosed = new EventEmitter<CandidateJobExperience>();

  @ViewChild('top') top: ElementRef;

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
      id: [this.candidateJobExperience ? this.candidateJobExperience.id : null],
      companyName: [this.candidateJobExperience ? this.candidateJobExperience.companyName : '', Validators.required],
      country: [this.candidateJobExperience ? this.candidateJobExperience.countryId : '', Validators.required],
      candidateOccupationId: [this.candidateJobExperience ? this.candidateJobExperience.candidateOccupationId : '', Validators.required],
      role: [this.candidateJobExperience ? this.candidateJobExperience.role : '', Validators.required],
      startDate: [this.candidateJobExperience ? this.candidateJobExperience.startDate : '', Validators.required],
      endDate: [this.candidateJobExperience ? this.candidateJobExperience.endDate : ''],
      fullTime: [this.candidateJobExperience ? this.candidateJobExperience.fullTime : null, Validators.required],
      paid: [this.candidateJobExperience ? this.candidateJobExperience.paid : null, Validators.required],
      description: [this.candidateJobExperience ? this.candidateJobExperience.description : '', Validators.required]
    });

    /* Patch form with candidates occupation */
    if (this.candidateJobExperience) {
      const exp = this.candidateJobExperience;
      this.form.controls['candidateOccupationId'].patchValue(exp.candidateOccupation.id || exp.candidateOccupationId);
    } else if (this.candidateOccupation && !this.candidateJobExperience) {
      this.form.controls['candidateOccupationId'].patchValue(this.candidateOccupation.id);
    }
  }

  ngAfterViewInit(): void {
    this.top.nativeElement.scrollIntoView()
  }

  save() {
    this.saving = true;
    if (this.form.value.id) {
      this.jobExperienceService.updateJobExperience(this.form.value).subscribe(
        (response) => this.emitSaveEvent(response),
        (error) => {
          this.error = error;
          this.saving = false;
        }
      );
    } else {
      this.jobExperienceService.createJobExperience(this.form.value).subscribe(
        (response) => {
          response.candidateOccupation = this.candidateOccupation;
          this.emitSaveEvent(response)
        },
        (error) => {
          this.error = error;
          this.saving = false;
        }
      );
    }
  }

  emitSaveEvent(exp: CandidateJobExperience) {
    this.formSaved.emit(exp);
    this.saving = false;
  }

  cancel() {
    this.formClosed.emit();
  }

}
