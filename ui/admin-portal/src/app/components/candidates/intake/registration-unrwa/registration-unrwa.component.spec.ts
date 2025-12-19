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
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NgSelectModule} from '@ng-select/ng-select';
import {RegistrationUnrwaComponent} from './registration-unrwa.component';
import {CandidateService} from '../../../../services/candidate.service';
import {NotRegisteredStatus, YesNoUnsure} from "../../../../model/candidate";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
describe('RegistrationUnrwaComponent', () => {
  let component: RegistrationUnrwaComponent;
  let fixture: ComponentFixture<RegistrationUnrwaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RegistrationUnrwaComponent,AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, NgSelectModule, FormsModule, ReactiveFormsModule ],
      providers: [
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationUnrwaComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      unrwaRegistered: YesNoUnsure.Yes,
      unrwaNumber: '67890',
      unrwaFile: 4,
      unrwaNotRegStatus: NotRegisteredStatus.NA,
      unrwaNotes: 'Test note'
    };
    component.showAll = true;
    fixture.detectChanges();
  });

  it('should initialize the form controls with the correct default values', () => {
    expect(component.form.get('unrwaRegistered').value).toBe(YesNoUnsure.Yes);
    expect(component.form.get('unrwaNumber').value).toBe('67890');
    expect(component.form.get('unrwaFile').value).toBe(4);
    expect(component.form.get('unrwaNotRegStatus').value).toBe(NotRegisteredStatus.NA);
    expect(component.form.get('unrwaNotes').value).toBe('Test note');
  });

  it('should show additional fields when registered with UNRWA', () => {
    component.form.patchValue({ unrwaRegistered: 'Yes' });
    fixture.detectChanges();
    expect(component.form.get('unrwaNumber').enabled).toBeTrue();
    expect(component.form.get('unrwaFile').enabled).toBeTrue();
  });

  it('should show not registered status field when not registered with UNRWA', () => {
    component.form.patchValue({ unrwaRegistered: 'No' });
    fixture.detectChanges();
    expect(component.form.get('unrwaNotRegStatus').enabled).toBeTrue();
  });

  it('should display additional notes when there is a registration status', () => {
    component.form.patchValue({ unrwaRegistered: 'Yes' });
    fixture.detectChanges();
    expect(component.hasNotes).toBeTrue();
    expect(component.form.get('unrwaNotes').enabled).toBeTrue();
  });

  it('should not display additional notes when status is null or NoResponse', () => {
    component.form.patchValue({ unrwaRegistered: null });
    fixture.detectChanges();
    expect(component.hasNotes).toBeFalse();

    component.form.patchValue({ unrwaRegistered: 'NoResponse' });
    fixture.detectChanges();
    expect(component.hasNotes).toBeFalse();
  });
});
