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
import {MilitaryServiceComponent} from "./military-service.component";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";

describe('MilitaryServiceComponent', () => {
  let component: MilitaryServiceComponent;
  let fixture: ComponentFixture<MilitaryServiceComponent>;
  let fb: FormBuilder;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ MilitaryServiceComponent,AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, NgSelectModule, FormsModule, ReactiveFormsModule ],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MilitaryServiceComponent);
    component = fixture.componentInstance;
    fb = TestBed.inject(FormBuilder);
    component.editable = false;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with correct default values and disable controls if not editable', () => {
    component.form.get('militaryService').setValue(null);
    component.form.get('militaryWanted').setValue(null);
    component.form.get('militaryNotes').setValue(null);
    component.form.get('militaryStart').setValue(null);
    component.form.get('militaryEnd').setValue(null);

    expect(component.form.get('militaryService').value).toBeNull(); // Verify default value
    expect(component.form.get('militaryWanted').value).toBeNull();
    expect(component.form.get('militaryNotes').value).toBeNull();
    expect(component.form.get('militaryStart').value).toBeNull();
    expect(component.form.get('militaryEnd').value).toBeNull();
    expect(component.form.get('militaryService').disabled).toBeTrue(); // Check if control is disabled
    expect(component.form.get('militaryWanted').disabled).toBeTrue();
    expect(component.form.get('militaryNotes').disabled).toBeTrue();
    expect(component.form.get('militaryStart').disabled).toBeTrue();
    expect(component.form.get('militaryEnd').disabled).toBeTrue();
  });

});

