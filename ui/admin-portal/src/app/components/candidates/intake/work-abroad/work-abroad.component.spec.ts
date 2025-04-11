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
import {WorkAbroadComponent} from "./work-abroad.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {CandidateService} from "../../../../services/candidate.service";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {YesNo} from "../../../../model/candidate";

describe('WorkAbroadComponent', () => {
  let component: WorkAbroadComponent;
  let fixture: ComponentFixture<WorkAbroadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WorkAbroadComponent, AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, ReactiveFormsModule, NgSelectModule ],
      providers: [
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkAbroadComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      workAbroad: YesNo.Yes,
      workAbroadNotes: 'Studied in Germany for 2 years'
    };
    fixture.detectChanges();
  });

  it('should initialize the form controls with the correct default values', () => {
    expect(component.form.get('workAbroad').value).toBe(YesNo.Yes);
    expect(component.form.get('workAbroadNotes').value).toBe('Studied in Germany for 2 years');
  });

  it('should display workAbroadNotes field when workAbroad is Yes', () => {
    component.form.get('workAbroad').setValue('Yes');
    fixture.detectChanges();
    const notesField = fixture.nativeElement.querySelector('#workAbroadNotes');
    expect(notesField).not.toBeNull();
  });

  it('should hide workAbroadNotes field when workAbroad is No', () => {
    component.form.get('workAbroad').setValue('No');
    fixture.detectChanges();
    const notesField = fixture.nativeElement.querySelector('#workAbroadNotes');
    expect(notesField).toBeNull();
  });

  it('should display workAbroadNotes field when workAbroad is changed from No to Yes', () => {
    component.form.get('workAbroad').setValue('No');
    fixture.detectChanges();
    let notesField = fixture.nativeElement.querySelector('#workAbroadNotes');
    expect(notesField).toBeNull();

    component.form.get('workAbroad').setValue('Yes');
    fixture.detectChanges();
    notesField = fixture.nativeElement.querySelector('#workAbroadNotes');
    expect(notesField).not.toBeNull();
  });

  it('should initialize the form controls correctly when candidateIntakeData is null', () => {
    component.candidateIntakeData = null;
    component.ngOnInit();
    fixture.detectChanges();

    expect(component.form.get('workAbroad').value).toBeNull();
    expect(component.form.get('workAbroadNotes').value).toBeNull();
  });
});
