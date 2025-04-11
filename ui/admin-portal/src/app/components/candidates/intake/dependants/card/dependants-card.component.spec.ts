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
import {DependantsCardComponent} from "./dependants-card.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../../services/candidate.service";
import {CandidateDependantService} from "../../../../../services/candidate-dependant.service";
import {DependantRelations, Gender, Registrations, YesNo} from "../../../../../model/candidate";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {DatePickerComponent} from "../../../../util/date-picker/date-picker.component";
import {NgbDatepickerModule} from "@ng-bootstrap/ng-bootstrap";
import {of} from "rxjs";

describe('DependantsCardComponent', () => {
  let component: DependantsCardComponent;
  let fixture: ComponentFixture<DependantsCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DependantsCardComponent,AutosaveStatusComponent,DatePickerComponent],
      imports: [HttpClientTestingModule,NgbDatepickerModule,NgSelectModule,FormsModule,ReactiveFormsModule],
      providers: [UntypedFormBuilder, CandidateService, CandidateDependantService]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DependantsCardComponent);
    component = fixture.componentInstance;
    component.myRecordIndex = 0; // Set any required inputs or variables
    component.candidateIntakeData = {
      candidateDependants: [{
        id: 1, // Mock ID
        relation: DependantRelations.Child, // Mock relation
        dob: '2000-01-01', // Mock date of birth
        gender: Gender.male, // Mock gender
        name: 'John', // Mock name
        registered: Registrations.UNHCR, // Mock registration status
        healthConcern: YesNo.Yes, // Mock health concern
        registeredNumber: '123', // Mock registration number
        registeredNotes: 'Test notes', // Mock registration notes
        healthNotes: 'Health notes' // Mock health notes
      }]
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should initialize with provided candidate data', () => {
    expect(component.myRecord).toBeTruthy();
    expect(component.dependantRelationship).toBe(DependantRelations.Child);
    expect(component.dependantAge).toBeGreaterThan(0);
    expect(component.dependantRegistered).toBe(Registrations.UNHCR);
  });

  it('should delete dependant when doDelete is called', () => {
    spyOn(component.delete, 'emit');
    spyOn(TestBed.inject(CandidateDependantService), 'delete').and.returnValue(of());

    component.doDelete();

    expect(TestBed.inject(CandidateDependantService).delete).toHaveBeenCalledWith(1); // Assuming the ID is set to 1 in this test
    expect(component.delete.emit).toHaveBeenCalled();
  });
});
