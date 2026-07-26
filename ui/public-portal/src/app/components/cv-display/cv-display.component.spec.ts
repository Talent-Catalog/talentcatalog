/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {CommonModule} from '@angular/common';
import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {Candidate} from '../../model/candidate';
import {ExtendDatePipe} from '../../util/date-pipe';
import {CvDisplayComponent} from './cv-display.component';

describe('CvDisplayComponent', () => {
  let component: CvDisplayComponent;
  let fixture: ComponentFixture<CvDisplayComponent>;

  const candidate = {
    candidateNumber: '123456',
    country: {name: 'Afghanistan'},
    candidateJobExperiences: [],
    candidateEducations: [],
    candidateLanguages: [],
    candidateCertifications: []
  } as Candidate;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [CommonModule],
      declarations: [CvDisplayComponent, ExtendDatePipe]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CvDisplayComponent);
    component = fixture.componentInstance;
    component.candidate = candidate;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the candidate number and country', () => {
    const heading: HTMLElement =
      fixture.nativeElement.querySelector('.page-header h4');

    expect(heading.textContent).toContain('#123456 - Afghanistan');
  });

  it('should call window.print when print is requested', () => {
    const printSpy = spyOn(window, 'print');

    component.print();

    expect(printSpy).toHaveBeenCalled();
  });

  it('should identify text containing HTML', () => {
    expect(component.isHtml('<p>Software developer</p>')).toBeTrue();
  });

  it('should identify plain text as non-HTML', () => {
    expect(component.isHtml('Software developer')).toBeFalse();
  });
});
