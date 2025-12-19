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

import {CvPreviewComponent} from "./cv-preview.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {Candidate} from "../../../model/candidate";
import {AttachmentType, CandidateAttachment} from "../../../model/candidate-attachment";
import {DebugElement} from "@angular/core";
import {By} from "@angular/platform-browser";
import {SafePipe} from "../../../pipes/safe.pipe";

describe('CvPreviewComponent', () => {
  let component: CvPreviewComponent;
  let fixture: ComponentFixture<CvPreviewComponent>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CvPreviewComponent,SafePipe]
    }).compileComponents();

    fixture = TestBed.createComponent(CvPreviewComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call updateCvUrlForPreview when candidate input changes', () => {
    spyOn(component, 'updateCvUrlForPreview');
    const candidate: Candidate = mockCandidate;
    component.candidate = candidate;
    fixture.detectChanges();
    component.ngOnChanges({ candidate: { previousValue: null, currentValue: candidate, firstChange: true, isFirstChange: () => true } });
    expect(component.updateCvUrlForPreview).toHaveBeenCalled();
  });

  it('should set cvUrl and loading correctly for listShareableCv', () => {
    const candidate: Candidate = mockCandidate;
    component.candidate = candidate;
    component.updateCvUrlForPreview();
    expect(component.cvUrl).toBe('https://example.com/list_cv.pdf');
    expect(component.loading).toBe(false);
  });

  it('should set cvUrl and loading correctly for shareableCv', () => {
    const candidate: Candidate = mockCandidate;
    component.candidate = candidate;
    component.updateCvUrlForPreview();
    expect(component.cvUrl).toBe('https://example.com/list_cv.pdf');
    expect(component.loading).toBe(false);
  });

  it('should set cvUrl to null and loading to false if no previewable CV is available', () => {
    const candidate: Candidate = null;
    component.candidate = candidate;
    component.updateCvUrlForPreview();
    expect(component.cvUrl).toBeNull();
    expect(component.loading).toBe(false);
  });

  it('should return true for non-doc/docx files', () => {
    const cv: CandidateAttachment = mockCandidate.candidateAttachments[0];
    cv.type = AttachmentType.googlefile;
    expect(component.canPreviewCv(cv)).toBeTrue();
  });

  it('should return true for non-file attachment types', () => {
    const cv: CandidateAttachment = mockCandidate.candidateAttachments[0];
    expect(component.canPreviewCv(cv)).toBeTrue();
  });

  it('should display loading spinner when loading is true', () => {
    component.loading = true;
    fixture.detectChanges();
    const spinner: DebugElement = fixture.debugElement.query(By.css('.fa-spinner'));
    expect(spinner).toBeTruthy();
  });

  it('should display iframe when cvUrl is present', () => {
    component.loading = false;
    component.cvUrl = 'https://example.com/cv.pdf';
    fixture.detectChanges();
    const iframe: DebugElement = fixture.debugElement.query(By.css('iframe'));
    expect(iframe).toBeTruthy();
  });

  it('should display noCv template when cvUrl is not present', () => {
    component.loading = false;
    component.cvUrl = null;
    fixture.detectChanges();
    const noCv: DebugElement = fixture.debugElement.query(By.css('.text-muted'));
    expect(noCv).toBeTruthy();
  });
});
