import {Component, Input, OnInit} from '@angular/core';
import {Candidate, CandidateIntakeData} from "../../../../../model/candidate";
import {CandidateService} from "../../../../../services/candidate.service";
import {forkJoin} from "rxjs";
import {Nationality} from "../../../../../model/nationality";
import {NationalityService} from "../../../../../services/nationality.service";

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

  constructor(
    private candidateService: CandidateService,
    private nationalityService: NationalityService
  ) { }

  ngOnInit(): void {
    //Load existing candidateIntakeData and other data needed by intake
    this.error = null;
    this.loading = true;
    forkJoin({
      'nationalities': this.nationalityService.listNationalities(),
      'intakeData':  this.candidateService.getIntakeData(this.candidate.id),
    }).subscribe(results => {
      this.loading = false;
      this.nationalities = results['nationalities'];
      this.candidateIntakeData = results['intakeData'];
    }, error => {
      this.loading = false;
      this.error = error;
    });
  }

}
