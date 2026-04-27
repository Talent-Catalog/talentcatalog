/*
 * Copyright (c) 2024 Talent Catalog.
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

import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from '../../../../model/candidate';
import {CandidateNote} from '../../../../model/candidate-note';
import {CandidateNoteService} from '../../../../services/candidate-note.service';
import {EditCandidateNoteComponent} from './edit/edit-candidate-note.component';
import {CreateCandidateNoteComponent} from './create/create-candidate-note.component';

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

  loading: boolean;
  expanded: boolean;
  error;
  notes: CandidateNote[];
  fullPanel: boolean = false;

  constructor(private candidateNoteService: CandidateNoteService,
              private modalService: NgbModal) {
  }

  ngOnInit() {

  }

  ngOnChanges(changes: SimpleChanges) {
    this.characterLimit = this.characterLimit ? this.characterLimit : 100;
    this.expanded = false;
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
