import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from "../../../../model/candidate";
import {CandidateNote} from "../../../../model/candidate-note";
import {CandidateNoteService} from "../../../../services/candidate-note.service";
import {EditCandidateNoteComponent} from "./edit/edit-candidate-note.component";
import {CreateCandidateNoteComponent} from "./create/create-candidate-note.component";
import {FormBuilder, FormGroup} from "@angular/forms";
import {SearchResults} from "../../../../model/search-results";

@Component({
  selector: 'app-view-candidate-note',
  templateUrl: './view-candidate-note.component.html',
  styleUrls: ['./view-candidate-note.component.scss']
})
export class ViewCandidateNoteComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  candidateNoteForm: FormGroup;
  loading: boolean;
  expanded: boolean;
  error;
  notes: CandidateNote[];
  hasMore: boolean;

  constructor(private candidateNoteService: CandidateNoteService,
              private modalService: NgbModal,
              private fb: FormBuilder) {
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges) {
    this.editable = true;
    this.expanded = false;
    this.notes = [];

    this.candidateNoteForm = this.fb.group({
      candidateId: [this.candidate.id],
      pageSize: 1,
      pageNumber: 0,
      sortDirection: 'DESC',
      sortFields: [['createdDate']]
    });

    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.doSearch();
    }

  }

  doSearch() {
    this.loading = true;
    this.candidateNoteService.search(this.candidateNoteForm.value).subscribe(
      results => {
        this.notes.push(...results.content);
        this.hasMore = results.totalPages > results.number+1;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      })
    ;

  }

  loadMore() {
    this.candidateNoteForm.controls['pageNumber'].patchValue(this.candidateNoteForm.value.pageNumber+1);
    this.doSearch();
  }

  editCandidateNote(candidateNote: CandidateNote) {
    const editCandidateNoteModal = this.modalService.open(EditCandidateNoteComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateNoteModal.componentInstance.candidateNote = candidateNote;

    editCandidateNoteModal.result
      .then((candidateNote) => this.doSearch())
      .catch(() => { /* Isn't possible */
      });

  }

  createCandidateNote() {
    const createCandidateNoteModal = this.modalService.open(CreateCandidateNoteComponent, {
      centered: true,
      backdrop: 'static'
    });

    createCandidateNoteModal.componentInstance.candidateId = this.candidate.id;

    createCandidateNoteModal.result
      .then((candidateNote) => this.doSearch())
      .catch(() => { /* Isn't possible */
      });

  }

}
