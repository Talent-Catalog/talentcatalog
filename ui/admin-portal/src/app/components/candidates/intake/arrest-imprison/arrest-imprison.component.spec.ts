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
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormBuilder, FormControl, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ArrestImprisonComponent} from './arrest-imprison.component';
import {CandidateService} from '../../../../services/candidate.service';
import {YesNoUnsure} from '../../../../model/candidate';
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";

fdescribe('ArrestImprisonComponent', () => {
  let component: ArrestImprisonComponent;
  let fixture: ComponentFixture<ArrestImprisonComponent>;
  let fb: FormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ArrestImprisonComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule, NgSelectModule,FormsModule,ReactiveFormsModule],
      providers: [
        FormControl,
        FormBuilder,
        { provide: CandidateService }
      ]
    }).compileComponents();

    fb = TestBed.inject(FormBuilder);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArrestImprisonComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      arrestImprison: YesNoUnsure.Yes,
      arrestImprisonNotes: 'Some notes about the arrest'
    };
    component.editable = true; // Assuming editable is true for this test
    fixture.detectChanges(); // Trigger initial data binding
  });

  it('should initialize the form correctly with the provided candidate intake data', () => {
    expect(component.form).toBeTruthy();
    const arrestImprisonControl = component.form.get('arrestImprison');
    const arrestImprisonNotesControl = component.form.get('arrestImprisonNotes');

    expect(arrestImprisonControl.value).toBe('Yes');
    expect(arrestImprisonControl.disabled).toBeFalse();
    expect(arrestImprisonNotesControl.value).toBe('Some notes about the arrest');
    expect(arrestImprisonNotesControl.disabled).toBeFalse();
  });
});
