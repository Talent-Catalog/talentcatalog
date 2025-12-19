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

import {DestinationFamilyComponent} from './destination-family.component';
import {CandidateVisaCheckService} from "../../../../services/candidate-visa-check.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {By} from "@angular/platform-browser";

describe('DestinationFamilyComponent', () => {
  let component: DestinationFamilyComponent;
  let fixture: ComponentFixture<DestinationFamilyComponent>;
  let candidateVisaCheckService: jasmine.SpyObj<CandidateVisaCheckService>;

  beforeEach(async () => {
    const candidateVisaCheckServiceSpy = jasmine.createSpyObj('CandidateVisaCheckService', ['someMethod']);

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,ReactiveFormsModule, NgSelectModule],
      declarations: [DestinationFamilyComponent,AutosaveStatusComponent],
      providers: [
        UntypedFormBuilder,
        {provide: CandidateVisaCheckService, useValue: candidateVisaCheckServiceSpy}
      ]
    }).compileComponents();

    candidateVisaCheckService = TestBed.inject(CandidateVisaCheckService) as jasmine.SpyObj<CandidateVisaCheckService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DestinationFamilyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('location input should be displayed when visaDestinationFamily is a family relation', () => {
    component.form.controls['visaDestinationFamily'].setValue('Child');
    fixture.detectChanges();
    const textarea = fixture.debugElement.query(By.css('#visaDestinationFamilyLocation'));
    expect(textarea).toBeTruthy();
  });

  it('location input should not be displayed when visaDestinationFamily is "NoRelation" or null', () => {
    component.form.controls['visaDestinationFamily'].setValue('NoRelation');
    fixture.detectChanges();
    const textarea = fixture.debugElement.query(By.css('#visaDestinationFamilyLocation'));
    expect(textarea).toBeFalsy();

    component.form.controls['visaDestinationFamily'].setValue(null);
    fixture.detectChanges();
    expect(textarea).toBeFalsy();
  });
});
