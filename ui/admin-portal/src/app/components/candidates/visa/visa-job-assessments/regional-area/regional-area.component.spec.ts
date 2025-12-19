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
import {UntypedFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {NgSelectModule} from '@ng-select/ng-select';
import {RegionalAreaComponent} from './regional-area.component';
import {CandidateVisaCheckService} from '../../../../../services/candidate-visa-check.service';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {YesNo} from '../../../../../model/candidate';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";

describe('RegionalAreaComponent', () => {
  let component: RegionalAreaComponent;
  let fixture: ComponentFixture<RegionalAreaComponent>;
  let candidateVisaCheckService: jasmine.SpyObj<CandidateVisaCheckService>;

  beforeEach(async () => {
    const candidateVisaCheckServiceSpy = jasmine.createSpyObj('CandidateVisaCheckService', ['someMethod']);

    await TestBed.configureTestingModule({
      declarations: [RegionalAreaComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateVisaCheckService, useValue: candidateVisaCheckServiceSpy }
      ]
    }).compileComponents();

    candidateVisaCheckService = TestBed.inject(CandidateVisaCheckService) as jasmine.SpyObj<CandidateVisaCheckService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RegionalAreaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); // ngOnInit() is called here
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with visaJobRegional control', () => {
    expect(component.form.contains('visaJobRegional')).toBeTrue();
  });

  it('should display error message when error is set', () => {
    component.error = 'Some error message';
    fixture.detectChanges();
    const errorElement: HTMLElement = fixture.nativeElement.querySelector('div');
    expect(errorElement.textContent).toContain('Some error message');
  });

  it('should initialize form control with value from visaJobCheck', () => {
    component.visaJobCheck = { id: 123, regional: YesNo.Yes };
    component.ngOnInit();
    expect(component.form.value.visaJobId).toBe(123);
    expect(component.form.value.visaJobRegional).toBe(YesNo.Yes );
  });

  it('should set regionalAreaOptions correctly', () => {
    const expectedOptions: EnumOption[] = enumOptions(YesNo);
    expect(component.regionalAreaOptions).toEqual(expectedOptions);
  });

  it('should render form elements when editable is true', () => {
    component.editable = true;
    fixture.detectChanges();
    const regionalElement = fixture.nativeElement.querySelector('#visaJobRegional');
    expect(regionalElement).toBeTruthy();
  });

  it('should not render form elements when editable is false', () => {
    component.editable = false;
    fixture.detectChanges();
    const regionalElement = fixture.nativeElement.querySelector('#visaJobRegional');
    expect(regionalElement).toBeFalsy();
  });
});
