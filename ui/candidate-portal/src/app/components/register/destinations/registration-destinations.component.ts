import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  QueryList,
  ViewChildren
} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {Candidate, CandidateDestination} from "../../../model/candidate";
import {CandidateService} from "../../../services/candidate.service";
import {RegistrationService} from "../../../services/registration.service";
import {CountryService} from "../../../services/country.service";
import {Country} from "../../../model/country";
import {DestinationComponent} from "./destination/destination.component";

@Component({
  selector: 'app-registration-destinations',
  templateUrl: './registration-destinations.component.html',
  styleUrls: ['./registration-destinations.component.scss']
})
export class RegistrationDestinationsComponent implements OnInit {
  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  @ViewChildren(DestinationComponent) destinationFormComponents: QueryList<DestinationComponent>

  candidate: Candidate;

  form: FormGroup;
  error: any;
  loading: boolean;
  saving: boolean;
  candidateDestinations: CandidateDestination[];
  destinations: Country[];
  candidateDestinationsRequest: CandidateDestination[];

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService,
              private countryService: CountryService,
              public registrationService: RegistrationService) {
  }

  ngOnInit() {
    this.saving = false;
    this.loading = true;

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

  fetchDestination(countryId: number) {
     return this.candidateDestinations?.find(d => d.country.id === countryId)
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
    this.saving = true;
    let request = [];
    this.destinationFormComponents.map(d => request.push(d.form.value));
    console.log(request)
  }

  cancel() {
    this.onSave.emit();
  }

}
