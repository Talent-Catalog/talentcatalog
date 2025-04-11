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
import {ReturnHomeSafeComponent} from "./return-home-safe.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {CandidateService} from "../../../../services/candidate.service";
import {YesNoUnsure} from "../../../../model/candidate";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

describe('ReturnHomeSafeComponent', () => {
  let component: ReturnHomeSafeComponent;
  let fixture: ComponentFixture<ReturnHomeSafeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReturnHomeSafeComponent,AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, ReactiveFormsModule, NgSelectModule ],
      providers: [
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ReturnHomeSafeComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      returnHomeSafe: YesNoUnsure.Yes
    };
    component.editable = false;  // Set editable to false to test the disabled state
    fixture.detectChanges();
  });

  it('should initialize the form control with the correct default value', () => {
    expect(component.form.get('returnHomeSafe').value).toBe(YesNoUnsure.Yes);
  });

  it('should disable the form control when the component is not editable', () => {
    const control = component.form.get('returnHomeSafe');
    expect(control.disabled).toBeTrue();
  });

  it('should enable the form control when the component is editable', () => {
    component.editable = true;  // Set editable to true to test the enabled state
    component.ngOnInit();  // Reinitialize the form
    fixture.detectChanges();

    const control = component.form.get('returnHomeSafe');
    expect(control.disabled).toBeFalse();
  });
});
