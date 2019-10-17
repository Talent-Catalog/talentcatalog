import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from "../../../../model/candidate";
import {Occupation} from "../../../../model/occupation";
import {CandidateOccupation} from "../../../../model/candidate-occupation";
import {CandidateService} from "../../../../services/candidate.service";
import {CandidateOccupationService} from "../../../../services/candidate-occupation.service";
import {EditCountryComponent} from "../../../settings/countries/edit/edit-country.component";
import {SearchResults} from "../../../../model/search-results";

@Component({
  selector: 'app-view-candidate-occupation',
  templateUrl: './view-candidate-occupation.component.html',
  styleUrls: ['./view-candidate-occupation.component.scss']
})
export class ViewCandidateOccupationComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  loading: boolean;
  error;
  candidateOccupation: CandidateOccupation;
  results: CandidateOccupation[];

  constructor(private candidateService: CandidateService,
              private candidateOccupationService: CandidateOccupationService,
              private modalService: NgbModal) { }

  ngOnInit() {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.loading = true;

      this.candidateService.get(this.candidate.id).subscribe(
        candidate => {
            this.candidate = candidate;
            this.loading = false;
          },
        error => {
          this.error = error;
          this.loading = false;
        });
    }

    this.candidateOccupationService.get(this.candidate.id).subscribe(
      results => {
           this.results = results;
           this.loading = false;
         },
      error => {
         this.error = error;
         this.loading = false;
       }
     );
    }
}
