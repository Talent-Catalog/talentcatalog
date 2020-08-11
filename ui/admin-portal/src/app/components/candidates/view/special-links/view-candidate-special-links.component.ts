import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges
} from '@angular/core';
import {Candidate} from "../../../../model/candidate";
import {CandidateService} from "../../../../services/candidate.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EditCandidateSpecialLinksComponent} from "./edit/edit-candidate-special-links.component";

@Component({
  selector: 'app-view-candidate-special-links',
  templateUrl: './view-candidate-special-links.component.html',
  styleUrls: ['./view-candidate-special-links.component.scss']
})
export class ViewCandidateSpecialLinksComponent implements OnInit, OnChanges {
  @Input() candidate: Candidate;
  @Input() editable: boolean;
  loading: boolean;
  error;

  constructor(private candidateService: CandidateService,
              private modalService: NgbModal) { }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
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

  editSpecialLinks() {
    const editCandidateModal = this.modalService.open(EditCandidateSpecialLinksComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateModal.componentInstance.candidateId = this.candidate.id;

    editCandidateModal.result
      .then((candidate) => this.candidate = candidate)
      .catch(() => { /* Isn't possible */ });

  }

  createCandidateFolder() {
    this.candidateService.createCandidateFolder(this.candidate.id).subscribe(
      candidate => {
        this.candidate = candidate;
      },
      error => {
        this.error = error;
      });
  }
}
