import {Component, Input, OnInit} from '@angular/core';
import {Candidate, CandidateIntakeData} from '../../../../model/candidate';
import {Country} from '../../../../model/country';
import {CandidateDestination} from '../../../../model/candidate-destination';
import {CandidateDestinationService} from '../../../../services/candidate-destination.service';

@Component({
  selector: 'app-destinations',
  templateUrl: './destinations.component.html',
  styleUrls: ['./destinations.component.scss']
})
export class DestinationsComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() countries: Country[];
  destinationCountries: Country[];
  countryNames: string[];
  error: boolean;
  saving: boolean;

  constructor(
    private candidateDestinationService: CandidateDestinationService
  ) {}

  ngOnInit(): void {
    // ADDING NEW DESTINATION COUNTRY? If adding a new country to the destination countries, just add the name in array below.
    this.countryNames = ["Australia", "Canada", "New Zealand", "United Kingdom"]
    this.destinationCountries = [];
    this.destinationCountries = this.countries.filter(c => this.countryNames.includes(c.name));
    // If there are no candidateDestinations for the candidate, create them. But if they are already there, don't create them.
    if (this.candidateIntakeData.candidateDestinations.length <= 0) {
      this.createIds(this.destinationCountries);
    }
    // Order alphatically
    this.candidateIntakeData.candidateDestinations.sort((a, b) => {
      if (a.country.name < b.country.name) { return -1; }
      if (a.country.name > b.country.name) { return 1; }
      return 0;
    })
  }

  createIds(countries: Country[]){
    for (const c of countries) {
      this.saving = true;
      const candidateDestination: CandidateDestination = {
        country: c,
      };
      this.candidateDestinationService.create(this.candidate.id, candidateDestination).subscribe(
        (destination) => {
          this.candidateIntakeData.candidateDestinations.push(destination)
          this.saving = false;
        },
        (error) => {
          this.error = error;
          this.saving = false;
        });
    }

  }

}
