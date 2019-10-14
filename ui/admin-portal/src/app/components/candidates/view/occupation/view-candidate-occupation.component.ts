import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from "../../../../model/candidate";
import {Occupation} from "../../../../model/occupation";
import {CandidateOccupation} from "../../../../model/candidate-occupation";
import {CandidateService} from "../../../../services/candidate.service";
import {EditCountryComponent} from "../../../settings/countries/edit/edit-country.component";

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
  occupation: Occupation;
  candidateOccupation: CandidateOccupation;
  candidateOccupations: CandidateOccupation[];

  constructor(private candidateService: CandidateService,
              private modalService: NgbModal) { }

  ngOnInit() {
    this.occupation = {
      id: 1,
      name: "Accountant",
      status: "active"
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    console.log(changes);
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
  }

}
