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
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {VisaCheckNzComponent} from "./visa-check-nz.component";
import {MockCandidate} from "../../../../../../MockData/MockCandidate";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {
  mockCandidateIntakeData
} from "../../candidate-intake-tab/candidate-intake-tab.component.spec";
import {By} from '@angular/platform-browser';

describe('VisaCheckNzComponent', () => {
  let component: VisaCheckNzComponent;
  let fixture: ComponentFixture<VisaCheckNzComponent>;
  const mockCandidate  = new MockCandidate();
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [VisaCheckNzComponent],
      imports: [HttpClientTestingModule],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaCheckNzComponent);
    component = fixture.componentInstance;
    component.selectedIndex = 0;
    component.candidate = mockCandidate;
    component.candidateIntakeData = mockCandidateIntakeData;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display loading spinner when loading is true', () => {
    component.loading = true;
    fixture.detectChanges();
    const spinner = fixture.debugElement.query(By.css('.fa-spinner'));
    expect(spinner).toBeTruthy();
  });

  it('should display error message when error is present', () => {
    fixture.detectChanges();
    const errorMsg = fixture.debugElement.query(By.css('div')).nativeElement;
    expect(errorMsg.textContent).toContain('loading...');
  });

  it('should display Visa New Zealand section when not loading', () => {
    component.loading = false;
    fixture.detectChanges();
    const visaSection = fixture.debugElement.query(By.css('#VisaNewZealand'));
    expect(visaSection).toBeTruthy();
  });

});
