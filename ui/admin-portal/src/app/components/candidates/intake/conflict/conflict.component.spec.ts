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
import {ConflictComponent} from "./conflict.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {YesNo} from "../../../../model/candidate";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgbDatepickerModule} from "@ng-bootstrap/ng-bootstrap";
import {NgSelectModule} from "@ng-select/ng-select";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
describe('ConflictComponent', () => {
  let component: ConflictComponent;
  let fixture: ComponentFixture<ConflictComponent>;

  const mockCandidateService = {
    candidateIntakeData: {
      conflict: YesNo.Yes, // Mocking conflict data
      conflictNotes: 'Test conflict notes' // Mocking conflict notes data
    }
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ConflictComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,NgSelectModule,FormsModule,ReactiveFormsModule],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateService, useValue: mockCandidateService }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConflictComponent);
    component = fixture.componentInstance;

    component.candidateIntakeData = {
      conflict: YesNo.Yes, // Mocking conflict data
      conflictNotes: 'Test conflict notes' // Mocking conflict notes data
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with candidate data', () => {
    // Access the form control values
    const conflictControl = component.form.get('conflict');
    const conflictNotesControl = component.form.get('conflictNotes');
    // Check if form controls are initialized with the correct values
    expect(conflictControl.value).toEqual(mockCandidateService.candidateIntakeData.conflict);
    expect(conflictNotesControl.value).toEqual(mockCandidateService.candidateIntakeData.conflictNotes);
  });
});
