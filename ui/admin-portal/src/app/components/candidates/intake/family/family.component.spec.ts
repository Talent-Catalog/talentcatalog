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
import {FamilyComponent} from './family.component';
import {EnumOption, enumOptions} from '../../../../util/enum';
import {YesNo} from '../../../../model/candidate';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {CandidateService} from '../../../../services/candidate.service';
import {By} from '@angular/platform-browser';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";

describe('FamilyComponent', () => {
  let component: FamilyComponent;
  let fixture: ComponentFixture<FamilyComponent>;
  let candidateService: jasmine.SpyObj<CandidateService>;

  beforeEach(async () => {
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['']);

    await TestBed.configureTestingModule({
      declarations: [FamilyComponent],
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
    fixture = TestBed.createComponent(FamilyComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      familyMove: YesNo.Yes,
      familyMoveNotes: 'We need to move with extended family.'
    };

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with candidate intake data', () => {
    expect(component.form.value).toEqual({
      familyMove: 'Yes',
      familyMoveNotes: 'We need to move with extended family.'
    });
  });

  it('should display familyMoveNotes input when familyMove is "Yes"', () => {
    component.form.controls['familyMove'].setValue('Yes');
    fixture.detectChanges();

    const familyMoveNotesInput = fixture.debugElement.query(By.css('textarea[id="familyMoveNotes"]'));
    expect(familyMoveNotesInput).toBeTruthy();
  });

  it('should not display familyMoveNotes input when familyMove is "No"', () => {
    component.form.controls['familyMove'].setValue('No');
    fixture.detectChanges();

    const familyMoveNotesInput = fixture.debugElement.query(By.css('textarea[id="familyMoveNotes"]'));
    expect(familyMoveNotesInput).toBeFalsy();
  });

  it('should have the correct options for familyMove', () => {
    const familyMoveOptions: EnumOption[] = enumOptions(YesNo);
    expect(component.familyMoveOptions).toEqual(familyMoveOptions);
  });

  it('should show error message if error is set', () => {
    component.error = 'An error occurred';
    fixture.detectChanges();

    const errorMsg = fixture.debugElement.query(By.css('div')).nativeElement.textContent;
    expect(errorMsg).toContain('An error occurred');
  });
});
