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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import { HealthIssuesComponent } from './health-issues.component';
import { EnumOption, enumOptions } from '../../../../util/enum';
import { YesNo } from '../../../../model/candidate';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { CandidateService } from '../../../../services/candidate.service';
import { By } from '@angular/platform-browser';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

describe('HealthIssuesComponent', () => {
  let component: HealthIssuesComponent;
  let fixture: ComponentFixture<HealthIssuesComponent>;
  let candidateService: jasmine.SpyObj<CandidateService>;

  beforeEach(async () => {
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['']);

    await TestBed.configureTestingModule({
      declarations: [HealthIssuesComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,NgSelectModule,FormsModule,ReactiveFormsModule],
      providers: [
        { provide: CandidateService, useValue: candidateServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();

    candidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HealthIssuesComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      healthIssues: 'Yes',
      healthIssuesNotes: 'I have a chronic condition.'
    };
    component.editable = true;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with candidate intake data', () => {
    expect(component.form.value).toEqual({
      healthIssues: 'Yes',
      healthIssuesNotes: 'I have a chronic condition.'
    });
  });

  it('should display healthIssuesNotes input when healthIssues is "Yes"', () => {
    component.form.controls['healthIssues'].setValue('Yes');
    fixture.detectChanges();

    const healthIssuesNotesInput = fixture.debugElement.query(By.css('textarea[id="healthIssuesNotes"]'));
    expect(healthIssuesNotesInput).toBeTruthy();
  });

  it('should have the correct options for healthIssues', () => {
    const healthIssuesOptions: EnumOption[] = enumOptions(YesNo);
    expect(component.healthIssuesOptions).toEqual(healthIssuesOptions);
  });

  it('should show error message if error is set', () => {
    component.error = 'An error occurred';
    fixture.detectChanges();

    const errorMsg = fixture.debugElement.query(By.css('div')).nativeElement.textContent;
    expect(errorMsg).toContain('An error occurred');
  });
});
