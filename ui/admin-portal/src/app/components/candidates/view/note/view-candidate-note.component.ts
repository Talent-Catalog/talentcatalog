/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from '../../../../model/candidate';
import {CandidateNote} from '../../../../model/candidate-note';
import {CandidateNoteService} from '../../../../services/candidate-note.service';
import {EditCandidateNoteComponent} from './edit/edit-candidate-note.component';
import {CreateCandidateNoteComponent} from './create/create-candidate-note.component';
import {UntypedFormBuilder, UntypedFormGroup} from '@angular/forms';

@Component({
  selector: 'app-view-candidate-note',
  templateUrl: './view-candidate-note.component.html',
  styleUrls: ['./view-candidate-note.component.scss']
})
export class ViewCandidateNoteComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;
  @Input() characterLimit: number;
  @Output() onResize = new EventEmitter();

  candidateNoteForm: UntypedFormGroup;
  loading: boolean;
  expanded: boolean;
  error;
  notes: CandidateNote[];
  hasMore: boolean;
  fullPanel: boolean = false;

  constructor(private candidateNoteService: CandidateNoteService,
              private modalService: NgbModal,
              private fb: UntypedFormBuilder) {
  }

  ngOnInit() {
    // Subscribe when a new note is made
    this.candidateNoteService.newNote$.subscribe(
      () => this.reload()
    );
    // Subscribe when a note is updated
    this.candidateNoteService.updatedNote$.subscribe(
      () => this.reload()
    );
  }

  ngOnChanges(changes: SimpleChanges) {
    this.characterLimit = this.characterLimit ? this.characterLimit : 100;
    this.expanded = false;
    this.notes = [];

    this.candidateNoteForm = this.fb.group({
      candidateId: [this.candidate.id],
      pageSize: 10,
      pageNumber: 0,
      sortDirection: 'DESC',
      sortFields: [['createdDate']]
    });

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

  reload() {
    this.loading = true;
    this.candidateNoteForm.controls['pageNumber'].patchValue(0);
    this.candidateNoteService.search(this.candidateNoteForm.value).subscribe(
      results => {
        this.notes = results.content;
        this.hasMore = results.totalPages > results.number + 1;
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

  }

  createCandidateNote() {
    const createCandidateNoteModal = this.modalService.open(CreateCandidateNoteComponent, {
      centered: true,
      backdrop: 'static'
    });

    createCandidateNoteModal.componentInstance.candidateId = this.candidate.id;

  }

  showNote(note){
    note.showDetail = !note.showDetail;
  }

  resize(){
    this.fullPanel = !this.fullPanel;
    this.onResize.emit()
  }


}
