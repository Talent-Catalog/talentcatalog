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
import {WorkPermitComponent} from "./work-permit.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {CandidateService} from "../../../../services/candidate.service";
import {WorkPermitValidity, YesNoUnsure} from "../../../../model/candidate";

describe('WorkPermitComponent', () => {
  let component: WorkPermitComponent;
  let fixture: ComponentFixture<WorkPermitComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WorkPermitComponent, AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, ReactiveFormsModule, NgSelectModule ],
      providers: [
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkPermitComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      workPermit: WorkPermitValidity.YesDesired,
      workPermitDesired: YesNoUnsure.Yes,
      workPermitDesiredNotes: 'Sample notes'
    };
    fixture.detectChanges();
  });

  it('should initialize the form controls with the correct default values', () => {
    expect(component.form.get('workPermit').value).toBe(WorkPermitValidity.YesDesired);
    expect(component.form.get('workPermitDesired').value).toBe(YesNoUnsure.Yes);
    expect(component.form.get('workPermitDesiredNotes').value).toBe('Sample notes');
  });

  it('should update the form controls when candidateIntakeData changes', () => {
    component.candidateIntakeData = {
      workPermit: WorkPermitValidity.No,
      workPermitDesired: YesNoUnsure.Unsure,
      workPermitDesiredNotes: 'Updated notes'
    };
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.form.get('workPermit').value).toBe(WorkPermitValidity.No);
    expect(component.form.get('workPermitDesired').value).toBe(YesNoUnsure.Unsure);
    expect(component.form.get('workPermitDesiredNotes').value).toBe('Updated notes');
  });

  it('should initialize the form controls correctly when candidateIntakeData is null', () => {
    component.candidateIntakeData = null;
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.form.get('workPermit').value).toBeUndefined();
    expect(component.form.get('workPermitDesired').value).toBeUndefined();
    expect(component.form.get('workPermitDesiredNotes').value).toBeUndefined();
  });
});
