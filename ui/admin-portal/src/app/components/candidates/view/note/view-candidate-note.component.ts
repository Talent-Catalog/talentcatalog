import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
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
  candidateNote: CandidateNote;
  loading: boolean;
  error;
  results: SearchResults<CandidateNote>;
  moreNotes: boolean;

  constructor(private candidateNoteService: CandidateNoteService,
              private modalService: NgbModal,
              private fb: FormBuilder) {
  }

  ngOnInit() {

  }

  ngOnChanges(changes: SimpleChanges) {
    this.editable = true;
    this.setUpForm();
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.loading = true;
      this.getNotes();
    }
  }

  setUpForm(){
    this.candidateNoteForm = this.fb.group({
      candidateId: [this.candidate.id],
      pageSize: 2,
      pageNumber: 0,
      sortDirection: 'DESC',
      sortFields: [['createdDate']]
    });
  };

  getNotes(){
    this.candidateNoteService.search(this.candidateNoteForm.value).subscribe(
      results => {
        this.results = results;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      })
    ;
  }

  editCandidateNote(candidateNote: CandidateNote) {
    const editCandidateNoteModal = this.modalService.open(EditCandidateNoteComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateNoteModal.componentInstance.candidateNote = candidateNote;

    editCandidateNoteModal.result
      .then((candidateNote) => this.candidateNote = candidateNote)
      .catch(() => { /* Isn't possible */ });

  }

  createCandidateNote() {
    const createCandidateNoteModal = this.modalService.open(CreateCandidateNoteComponent, {
      centered: true,
      backdrop: 'static'
    });

    createCandidateNoteModal.componentInstance.candidateId = this.candidate.id;

    createCandidateNoteModal.result
      .then((candidateNote) => this.candidateNote = candidateNote)
      .catch(() => { /* Isn't possible */ });

  }

  loadMore(){
    console.log(this.results);
    if(this.results.totalElements > this.results.content.length){
      this.candidateNoteForm.value.pageSize += 2;
      this.moreNotes = true;
      this.getNotes();
    }else{
      this.moreNotes = false;
    };
  }

}
