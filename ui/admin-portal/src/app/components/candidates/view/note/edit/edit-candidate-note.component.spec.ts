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
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {EditCandidateNoteComponent} from "./edit-candidate-note.component";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateNoteService} from "../../../../../services/candidate-note.service";
import {CountryService} from "../../../../../services/country.service";
import {MockUser} from "../../../../../MockData/MockUser";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {CandidateNote} from "../../../../../model/candidate-note";
import {SearchResults} from "../../../../../model/search-results";
import {of} from "rxjs";
import {NgxWigModule} from "ngx-wig";

describe('EditCandidateNoteComponent', () => {
  let component: EditCandidateNoteComponent;
  let fixture: ComponentFixture<EditCandidateNoteComponent>;
  let mockActiveModal: any;
  let mockCandidateNoteService: any;
  let mockCountryService: any;

  const mockUser = new MockUser();
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

  beforeEach(waitForAsync(() => {
    mockActiveModal = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);
    mockCandidateNoteService = jasmine.createSpyObj('CandidateNoteService', ['update']);
    mockCountryService = jasmine.createSpyObj('CountryService', ['listCountries']);

    TestBed.configureTestingModule({
      declarations: [ EditCandidateNoteComponent ],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule,NgxWigModule],
      providers: [
        UntypedFormBuilder,
        { provide: NgbActiveModal, useValue: mockActiveModal },
        { provide: CandidateNoteService, useValue: mockCandidateNoteService },
        { provide: CountryService, useValue: mockCountryService }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateNoteComponent);
    component = fixture.componentInstance;
    component.candidateNote = mockCandidateNotes[0];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize component with provided candidate note data', () => {
    // Simulate the response from the countryService
    const countries = ['Country1', 'Country2']; // Sample countries data
    mockCountryService.listCountries.and.returnValue(of(countries));

    component.ngOnInit();

    expect(component.loading).toBe(false);
    expect(component.candidateForm.get('title').value).toBe(mockCandidateNotes[0].title);
    expect(component.candidateForm.get('comment').value).toBe(mockCandidateNotes[0].comment);
  });
});
