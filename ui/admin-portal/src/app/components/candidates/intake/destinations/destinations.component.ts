import {Component, Input, OnInit} from '@angular/core';
import {
  Candidate,
  CandidateDestination,
  CandidateIntakeData
} from '../../../../model/candidate';
import {Country} from '../../../../model/country';
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
    //todo Move these to server config.
    // ADDING NEW DESTINATION COUNTRY? If adding a new country to the
    // destination countries, just add the name in array below.
    this.countryNames = ["Australia", "Canada", "New Zealand", "United Kingdom"]
    this.destinationCountries = [];
    this.destinationCountries = this.countries.filter(c => this.countryNames.includes(c.name));

    //todo I think we always need to create ids otherwise adding a new one won't work.
    // If there are no candidateDestinations for the candidate, create them. But if they are already there, don't create them.
    if (this.candidateIntakeData.candidateDestinations.length <= 0) {
      this.createIds(this.destinationCountries);
    }

    //todo Better to sort on server
    // Order alphatically
    this.candidateIntakeData.candidateDestinations.sort((a, b) => {
      if (a.country.name < b.country.name) { return -1; }
      if (a.country.name > b.country.name) { return 1; }
      return 0;
    })
  }

  //todo Not really creating ids, creating CandidateDestinations
  //Modify so that it is a createIfNecessary - and do all in one go.
  createIds(countries: Country[]) {
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
