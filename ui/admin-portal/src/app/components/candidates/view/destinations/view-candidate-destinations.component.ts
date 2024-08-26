import {Component, Input, OnInit, SimpleChanges} from '@angular/core';
import {Candidate} from "../../../../model/candidate";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {
  EditCandidateDestinationsComponent
} from "./edit/edit-candidate-destinations/edit-candidate-destinations.component";
import {CandidateDestinationService} from "../../../../services/candidate-destination.service";
import {CandidateDestination} from "../../../../model/candidate-destination";

@Component({
  selector: 'app-view-candidate-destinations',
  templateUrl: './view-candidate-destinations.component.html',
  styleUrls: ['./view-candidate-destinations.component.scss']
})
export class ViewCandidateDestinationsComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  loading: boolean;
  error;
  candidateDestinations: CandidateDestination[];
  emptyDestinations: boolean;

  constructor(private candidateDestinationService: CandidateDestinationService,
              private modalService: NgbModal) { }

  ngOnInit() {

  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.doSearch();
    }
  }

  doSearch() {
    this.loading = true;
    this.candidateDestinationService.list(this.candidate.id).subscribe(
      candidateDestinations => {
        this.candidateDestinations = candidateDestinations;
        this.loading = false;
        this.emptyDestinations = this.checkForEmptyDestinations();
      },
      error => {
        this.error = error;
        this.loading = false;
      })
    ;
  }

  editDestinationsDetails(destination: CandidateDestination) {
    const editCandidateDestinationsModal = this.modalService.open(EditCandidateDestinationsComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateDestinationsModal.componentInstance.candidateDestination = destination;

    editCandidateDestinationsModal.result
      .then((candidateDestination) => {
        let i = this.candidateDestinations.findIndex(cd => cd.id === candidateDestination.id);
        this.candidateDestinations[i] = candidateDestination;
      } )
      .catch(() => { /* Isn't possible */ });

  }
  // Some candidates have 'empty' candidate destinations so run a check to only display them if they have data.
  // These were created automatically in old code, having a country ID & candidate ID but no useful data inside.
  // Now we have moved candidate destinations to the registration so all candidates will have to have these filled out.
  checkForEmptyDestinations() {
    let empty = false;
    if (this.candidateDestinations.length === 0) {
      empty = true;
    } else if (this.candidateDestinations.length > 0) {
      empty = this.candidateDestinations.every(cd => cd.interest == null && cd.notes == null);
    }
    return empty;
  }
}
