import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {Candidate, CandidateDestination} from "../../../model/candidate";
import {CandidateService} from "../../../services/candidate.service";
import {RegistrationService} from "../../../services/registration.service";
import {CountryService} from "../../../services/country.service";
import {Country} from "../../../model/country";

@Component({
  selector: 'app-registration-destinations',
  templateUrl: './registration-destinations.component.html',
  styleUrls: ['./registration-destinations.component.scss']
})
export class RegistrationDestinationsComponent implements OnInit {
  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  candidate: Candidate;

  form: FormGroup;
  error: any;
  loading: boolean;
  saving: boolean;
  candidateDestinations: CandidateDestination[];
  destinations: Country[];

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService,
              private countryService: CountryService,
              public registrationService: RegistrationService) {
  }

  ngOnInit() {
    this.saving = false;

    this.countryService.listTBBDestinations().subscribe(
      (results) => {
        this.destinations = results;
      }
    )
    this.candidateService.getCandidateDestinations().subscribe(
      (candidate) => {
        this.candidate = candidate;
        this.candidateDestinations = candidate.candidateDestinations || [];
        this.loading = false;
      },(error) => {
        this.error = error;
        this.loading = false;
      }
    );

  }

  loadDropDownData() {

    // /* Load the survey types  */
    // this.surveyTypeService.listActiveSurveyTypes().subscribe(
    //   (response) => {
    //     /* Sort order with 'Other' showing last */
    //     const sortOrder = [1, 2, 3, 4, 5, 6, 7, 9, 8];
    //     this.surveyTypes = response
    //     .sort((a, b) => {
    //       return sortOrder.indexOf(a.id) - sortOrder.indexOf(b.id);
    //     })
    //     this._loading.surveyTypes = false;
    //   },
    //   (error) => {
    //     this.error = error;
    //     this._loading.surveyTypes = false;
    //   }
    // );
  }

  save(dir: string) {
    this.saving = true;


    //   this.candidateService.updateCandidateAdditionalInfo(this.form.value).subscribe(
    //     (candidate) => {
    //
    //       this.saving = false;
    //       if (dir === 'next') {
    //         this.onSave.emit();
    //         this.registrationService.next();
    //       } else {
    //         this.registrationService.back();
    //       }
    //     },
    //     (error) => {
    //       this.error = error;
    //       this.saving = false;
    //     }
    //   );
    // }

  }

  back() {

  }

  next() {

  }

  cancel() {
    this.onSave.emit();
  }

}
