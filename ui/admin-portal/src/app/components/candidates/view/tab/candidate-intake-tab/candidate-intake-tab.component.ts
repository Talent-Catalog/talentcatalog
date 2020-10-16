import {Component, Input, OnInit} from '@angular/core';
import {Candidate, CandidateIntakeData} from '../../../../../model/candidate';
import {CandidateService} from '../../../../../services/candidate.service';
import {forkJoin} from 'rxjs';
import {Nationality} from '../../../../../model/nationality';
import {NationalityService} from '../../../../../services/nationality.service';
import {Country} from '../../../../../model/country';
import {CountryService} from '../../../../../services/country.service';

@Component({
  selector: 'app-candidate-intake-tab',
  templateUrl: './candidate-intake-tab.component.html',
  styleUrls: ['./candidate-intake-tab.component.scss']
})
export class CandidateIntakeTabComponent implements OnInit {
  @Input() candidate: Candidate;
  candidateIntakeData: CandidateIntakeData;
  error: string;
  loading: boolean;
  nationalities: Nationality[];
  countries: Country[];

  constructor(
    private candidateService: CandidateService,
    private nationalityService: NationalityService,
    private countryService: CountryService
  ) { }

  ngOnInit(): void {
    //Load existing candidateIntakeData and other data needed by intake
    this.error = null;
    this.loading = true;
    forkJoin({
      'nationalities': this.nationalityService.listNationalities(),
      'countries': this.countryService.listCountries(),
      'intakeData':  this.candidateService.getIntakeData(this.candidate.id),
    }).subscribe(results => {
      this.loading = false;
      this.nationalities = results['nationalities'];
      this.countries = results['countries'];
      this.candidateIntakeData = results['intakeData'];
    }, error => {
      this.loading = false;
      this.error = error;
    });
  }

}
