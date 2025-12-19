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

import {WorkStatusComponent} from "./work-status.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {CandidateService} from "../../../../services/candidate.service";
import {YesNoUnemployedOther} from "../../../../model/candidate";

describe('WorkStatusComponent', () => {
  let component: WorkStatusComponent;
  let fixture: ComponentFixture<WorkStatusComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WorkStatusComponent, AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, ReactiveFormsModule, NgSelectModule ],
      providers: [
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkStatusComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      workDesired: YesNoUnemployedOther.Yes,
      workDesiredNotes: 'Sample notes'
    };
    fixture.detectChanges();
  });

  it('should initialize the form controls with the correct default values', () => {
    expect(component.form.get('workDesired').value).toBe(YesNoUnemployedOther.Yes);
    expect(component.form.get('workDesiredNotes').value).toBe('Sample notes');
  });

  it('should update the form controls when candidateIntakeData changes', () => {
    component.candidateIntakeData = {
      workDesired: YesNoUnemployedOther.No,
      workDesiredNotes: 'Updated notes'
    };
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.form.get('workDesired').value).toBe(YesNoUnemployedOther.No);
    expect(component.form.get('workDesiredNotes').value).toBe('Updated notes');
  });

  it('should initialize the form controls correctly when candidateIntakeData is null', () => {
    component.candidateIntakeData = null;
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.form.get('workDesired').value).toBeUndefined();
    expect(component.form.get('workDesiredNotes').value).toBeUndefined();
  });
});

