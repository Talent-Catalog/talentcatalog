import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from "../../../../model/candidate";
import {CandidateService} from "../../../../services/candidate.service";

@Component({
  selector: 'app-view-contact-candidate',
  templateUrl: './view-contact-candidate.component.html',
  styleUrls: ['./view-contact-candidate.component.scss']
})
export class ViewContactCandidateComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;

  loading: boolean;
  error;

  constructor(private candidateService: CandidateService,
              private route: ActivatedRoute) { }

  ngOnInit() {

  }

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
  }


}
