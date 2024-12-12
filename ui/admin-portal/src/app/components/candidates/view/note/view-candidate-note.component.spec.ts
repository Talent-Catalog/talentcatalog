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
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {ViewCandidateNoteComponent} from "./view-candidate-note.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateNoteService} from "../../../../services/candidate-note.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {CandidateNote} from "../../../../model/candidate-note";
import {CreateCandidateNoteComponent} from "./create/create-candidate-note.component";
import {EditCandidateNoteComponent} from "./edit/edit-candidate-note.component";
import {MockUser} from "../../../../MockData/MockUser";
import {SearchResults} from "../../../../model/search-results";
import {UpdatedByComponent} from "../../../util/user/updated-by/updated-by.component";
import {UserPipe} from "../../../util/user/user.pipe";

describe('ViewCandidateNoteComponent', () => {
  let component: ViewCandidateNoteComponent;
  let fixture: ComponentFixture<ViewCandidateNoteComponent>;
  let mockCandidateNoteService: jasmine.SpyObj<CandidateNoteService>;
  let mockModalService: jasmine.SpyObj<NgbModal>;

  const mockUser = new MockUser();
  const mockCandidate = new MockCandidate();
  const mockCandidateNotes: CandidateNote[] = [{
    id: 1,
    title: 'Note 1',
    comment: 'Comment',
    noteType: 'Test',
    createdBy: mockUser,
    createdDate: 2012,
    updatedBy: mockUser,
    updatedDate: 2013,
  }] as CandidateNote[];

  const mockSearchResults: SearchResults<CandidateNote> = {
    number: 0,
    size: 10,
    totalElements: 1,
    totalPages: 1,
    first: true,
    last: true,
    content: mockCandidateNotes,
  };


  beforeEach(async () => {
    const modalServiceSpy = jasmine.createSpyObj('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      declarations: [ViewCandidateNoteComponent, UpdatedByComponent, UserPipe],
      providers: [
        { provide: NgbModal, useValue: modalServiceSpy }
      ]
    })
    .compileComponents();

    mockCandidateNoteService = TestBed.inject(CandidateNoteService) as jasmine.SpyObj<CandidateNoteService>;
    mockModalService = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateNoteComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    component.notes = mockCandidateNotes;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should open create note modal', () => {
    const mockModalRef = { componentInstance: { candidateId: null } };
    mockModalService.open.and.returnValue(mockModalRef as any);

    component.createCandidateNote();

    expect(mockModalService.open).toHaveBeenCalledWith(CreateCandidateNoteComponent, {
      centered: true,
      backdrop: 'static'
    });
    expect(mockModalRef.componentInstance.candidateId).toEqual(component.candidate.id);
  });

  it('should open edit note modal', () => {
    const mockModalRef = { componentInstance: { candidateNote: null } };
    mockModalService.open.and.returnValue(mockModalRef as any);

    component.editCandidateNote(mockCandidateNotes[0]);

    expect(mockModalService.open).toHaveBeenCalledWith(EditCandidateNoteComponent, {
      centered: true,
      backdrop: 'static'
    });
    expect(mockModalRef.componentInstance.candidateNote).toEqual(mockCandidateNotes[0]);
  });
});
