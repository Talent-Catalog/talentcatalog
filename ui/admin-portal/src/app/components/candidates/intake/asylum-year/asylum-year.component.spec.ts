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
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from '@angular/forms';
import {AsylumYearComponent} from './asylum-year.component';
import {CandidateService} from '../../../../services/candidate.service';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {NgbDatepickerModule} from "@ng-bootstrap/ng-bootstrap";
import {DatePickerComponent} from "../../../util/date-picker/date-picker.component";

describe('AsylumYearComponent', () => {
  let component: AsylumYearComponent;
  let fixture: ComponentFixture<AsylumYearComponent>;
  let fb: UntypedFormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AsylumYearComponent, AutosaveStatusComponent, DatePickerComponent],
      imports: [HttpClientTestingModule,NgbDatepickerModule, NgSelectModule,FormsModule,ReactiveFormsModule],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateService}
      ]
    }).compileComponents();

    fb = TestBed.inject(UntypedFormBuilder);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AsylumYearComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      asylumYear: '2021' // Example asylum year
    };
    fixture.detectChanges(); // Trigger initial data binding
  });

  it('should initialize the form correctly with the provided asylum year data', () => {
    expect(component.form).toBeTruthy();
    const asylumYearControl = component.form.get('asylumYear');

    expect(asylumYearControl.value).toBe('2021');
  });
});
