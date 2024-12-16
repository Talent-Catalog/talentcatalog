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
import {ResidenceStatusComponent} from "./residence-status.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {CandidateService} from "../../../../services/candidate.service";
import {ResidenceStatus} from "../../../../model/candidate";

describe('ResidenceStatusComponent', () => {
  let component: ResidenceStatusComponent;
  let fixture: ComponentFixture<ResidenceStatusComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ResidenceStatusComponent,AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, NgSelectModule, FormsModule, ReactiveFormsModule ],
      providers: [
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ResidenceStatusComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      residenceStatus: ResidenceStatus.Other,
      residenceStatusNotes: 'Living in a refugee camp'
    };
    fixture.detectChanges();
  });

  it('should initialize the form controls with the correct default values', () => {
    expect(component.form.get('residenceStatus').value).toBe(ResidenceStatus.Other);
    expect(component.form.get('residenceStatusNotes').value).toBe('Living in a refugee camp');
  });

  it('should display the residence status notes textarea when residenceStatus is not null or NoResponse', () => {
    component.form.patchValue({ residenceStatus: 'Refugee' });
    fixture.detectChanges();
    const notesTextarea = fixture.nativeElement.querySelector('#residenceStatusNotes');
    expect(notesTextarea).toBeTruthy();
  });

  it('should not display the residence status notes textarea when residenceStatus is null or NoResponse', () => {
    component.form.patchValue({ residenceStatus: 'NoResponse' });
    fixture.detectChanges();
    const notesTextarea = fixture.nativeElement.querySelector('#residenceStatusNotes');
    expect(notesTextarea).toBeFalsy();
  });
});
