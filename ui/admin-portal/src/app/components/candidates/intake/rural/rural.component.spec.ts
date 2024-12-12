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
import {RuralComponent} from "./rural.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {CandidateService} from "../../../../services/candidate.service";
import {YesNoUnsure} from "../../../../model/candidate";

describe('RuralComponent', () => {
  let component: RuralComponent;
  let fixture: ComponentFixture<RuralComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RuralComponent,AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, ReactiveFormsModule, NgSelectModule ],
      providers: [
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RuralComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      intRecruitRural: YesNoUnsure.Yes,
      intRecruitRuralNotes: 'I am open to rural areas if there is good internet connectivity.'
    };
    fixture.detectChanges();
  });

  it('should initialize the form controls with the correct default values', () => {
    expect(component.form.get('intRecruitRural').value).toBe(YesNoUnsure.Yes);
    expect(component.form.get('intRecruitRuralNotes').value).toBe('I am open to rural areas if there is good internet connectivity.');
  });

  it('should display the intRecruitRuralNotes textarea', () => {
    const intRecruitRuralNotesTextarea = fixture.nativeElement.querySelector('#intRecruitRuralNotes');
    expect(intRecruitRuralNotesTextarea).toBeTruthy();
  });
});
