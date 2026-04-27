/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, EventEmitter, Input, OnInit, Output, QueryList, ViewChildren} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {Candidate, CandidateDestination} from "../../../model/candidate";
import {CandidateService} from "../../../services/candidate.service";
import {RegistrationService} from "../../../services/registration.service";
import {CountryService} from "../../../services/country.service";
import {Country} from "../../../model/country";
import {DestinationComponent} from "./destination/destination.component";
import {forkJoin, Observable} from "rxjs";
import {
  CandidateDestinationService,
  CreateCandidateDestinationRequest,
  UpdateCandidateDestinationRequest
} from "../../../services/candidate-destination.service";

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

  form: UntypedFormGroup;
  error: any;
  loading: boolean;
  saving: boolean;
  candidateDestinations: CandidateDestination[];
  destinations: Country[];
  destinationForms: any[];

  constructor(private fb: UntypedFormBuilder,
              private candidateService: CandidateService,
              private countryService: CountryService,
              private candidateDestinationService: CandidateDestinationService,
              public registrationService: RegistrationService) {
  }

  ngOnInit() {
    this.saving = false;
    this.loading = true;

    this.countryService.listTCDestinations().subscribe(
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

    if (this.destinationForms && this.destinationForms.length > 0) {
      let destinations$: Observable<CandidateDestination>[] = [];
      for (const dest of this.destinationForms) {
        if (dest.id != null) {
          let request: UpdateCandidateDestinationRequest = {
            interest: dest.interest,
            notes: dest.notes
          }
          destinations$.push(this.candidateDestinationService.update(dest.id, request))
        } else {
          let request: CreateCandidateDestinationRequest = {
            countryId: dest.countryId,
            interest: dest.interest,
            notes: dest.notes
          }
          destinations$.push(this.candidateDestinationService.create(this.candidate.id, request))
        }
      }
      this.error = null;
      forkJoin(destinations$).subscribe(
        (candidateDestinations) => {
          if (dir === 'next') {
            this.onSave.emit();
            this.registrationService.next();
          } else {
            this.registrationService.back();
          }
          this.saving = false;
        },
        (error) => {
          this.error = error;
          this.saving = false;
        }
      )
    } else {
      this.saving = false;
      if (dir === 'next') {
        this.onSave.emit();
        this.registrationService.next();
      } else {
        this.registrationService.back();
      }
    }
  }

  back() {
    this.save('back');
  }

  next() {
    this.destinationForms = [];
    // Create request of form data that has been changed (touched) - no need to update forms that are unchanged
    this.destinationFormComponents.map(d => {
      if (d.form.dirty) {
        this.destinationForms.push(d.form.value)
      }
    });
    this.save('next');
  }

  cancel() {
    this.onSave.emit();
  }

  get validationPassed(): boolean {
    let complete = true;

    for (const d of this.destinationFormComponents) {
      if (d.form.invalid) {
        complete = false;
        break;
      }
    }
    return complete;
  }

}
