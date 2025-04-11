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
import {CreateCandidateNoteComponent} from "./create-candidate-note.component";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgxWigModule} from "ngx-wig";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateNoteService} from "../../../../../services/candidate-note.service";
import {CountryService} from "../../../../../services/country.service";
import {of} from "rxjs";

describe('CreateCandidateNoteComponent', () => {
  let component: CreateCandidateNoteComponent;
  let fixture: ComponentFixture<CreateCandidateNoteComponent>;
  let mockActiveModal: any;
  let mockCandidateNoteService: any;
  let mockCountryService: any;

  beforeEach(waitForAsync(() => {
    mockActiveModal = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);
    mockCandidateNoteService = jasmine.createSpyObj('CandidateNoteService', ['create']);
    mockCountryService = jasmine.createSpyObj('CountryService', ['listCountries']);

    TestBed.configureTestingModule({
      declarations: [ CreateCandidateNoteComponent ],
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
    fixture = TestBed.createComponent(CreateCandidateNoteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should save candidate note successfully', () => {
    const candidateNote = {
      id: 1,
      title: 'Test Note',
      comment: 'This is a test note'
    };

    // Set up form values
    component.candidateForm.setValue({
      candidateId: 1,
      title: 'Test Note',
      comment: 'This is a test note'
    });

    // Simulate the response from the candidateNoteService
    mockCandidateNoteService.create.and.returnValue(of(candidateNote));

    // Call onSave method
    component.onSave();

    // Check if the candidateNoteService create method was called with correct parameters
    expect(mockCandidateNoteService.create).toHaveBeenCalledWith({
      candidateId: 1,
      title: 'Test Note',
      comment: 'This is a test note'
    });

    // Check if activeModal.close was called with the correct parameter
    expect(mockActiveModal.close).toHaveBeenCalledWith(candidateNote);

    // Check if saving flag is set to false after saving
    expect(component.saving).toBe(false);
  });

});
