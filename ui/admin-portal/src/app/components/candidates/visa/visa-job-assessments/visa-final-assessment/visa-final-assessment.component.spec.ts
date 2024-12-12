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
import {VisaFinalAssessmentComponent} from './visa-final-assessment.component';
import {By} from '@angular/platform-browser';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {MockCandidate} from "../../../../../MockData/MockCandidate";

describe('VisaFinalAssessmentComponent', () => {
  let component: VisaFinalAssessmentComponent;
  let fixture: ComponentFixture<VisaFinalAssessmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,ReactiveFormsModule,NgSelectModule],
      declarations: [VisaFinalAssessmentComponent,AutosaveStatusComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaFinalAssessmentComponent);
    component = fixture.componentInstance;
    component.candidate = new MockCandidate();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display loading spinner when loading is true', () => {
    component.loading = true;
    fixture.detectChanges();
    const loadingElement = fixture.debugElement.query(By.css('.fa-spinner'));
    expect(loadingElement).toBeTruthy();
  });

  it('should not display loading spinner or error message when loading is false and no error', () => {
    component.loading = false;
    fixture.detectChanges();
    const loadingElement = fixture.debugElement.query(By.css('.fa-spinner'));
    const errorElement: HTMLElement = fixture.nativeElement.querySelector('.error');
    expect(loadingElement).toBeFalsy();
    expect(errorElement).toBeFalsy();
  });

});
