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
import {CvIconComponent} from "./cv-icon.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {AuthorizationService} from "../../../services/authorization.service";
import {CandidateAttachmentService} from "../../../services/candidate-attachment.service";
import {CandidateAttachment} from "../../../model/candidate-attachment";
import {Candidate} from "../../../model/candidate";
import {of, throwError} from "rxjs";
import {DebugElement} from "@angular/core";
import {By} from "@angular/platform-browser";

describe('CvIconComponent', () => {
  let component: CvIconComponent;
  let fixture: ComponentFixture<CvIconComponent>;
  let mockAuthService;
  let mockCandidateAttachmentService;
  const mockCanidiate = new MockCandidate();
  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj(['canViewCandidateCV']);
    mockCandidateAttachmentService = jasmine.createSpyObj(['downloadAttachments']);

    await TestBed.configureTestingModule({
      declarations: [CvIconComponent],
      providers: [
        { provide: AuthorizationService, useValue: mockAuthService },
        { provide: CandidateAttachmentService, useValue: mockCandidateAttachmentService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CvIconComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call getAttachments', () => {
    spyOn(component, 'getAttachments');
    component.ngOnInit();
    expect(component.getAttachments).toHaveBeenCalled();
  });

  it('should populate cvs with single attachment if provided', () => {
    const attachment: CandidateAttachment = mockCanidiate.candidateAttachments[0];
    component.attachment = attachment;
    component.getAttachments();
    expect(component.cvs).toEqual([attachment]);
  });

  it('should populate cvs with candidate attachments if no single attachment is provided', () => {
    const candidate: Candidate = mockCanidiate;
    component.candidate = candidate;
    component.getAttachments();
    expect(component.cvs).toEqual(candidate.candidateAttachments.filter(attachment => attachment.cv));
  });

  it('should return true if user can view CV and cvs are present', () => {
    mockAuthService.canViewCandidateCV.and.returnValue(true);
    component.cvs = mockCanidiate.candidateAttachments;
    expect(component.canOpen()).toBe(true);
  });

  it('should return false if user cannot view CV', () => {
    mockAuthService.canViewCandidateCV.and.returnValue(false);
    component.cvs = mockCanidiate.candidateAttachments;
    expect(component.canOpen()).toBe(false);
  });

  it('should return false if cvs are not present', () => {
    mockAuthService.canViewCandidateCV.and.returnValue(true);
    component.cvs = [];
    expect(component.canOpen()).toBe(false);
  });

  it('should set loading to false and call downloadAttachments', () => {
    mockCandidateAttachmentService.downloadAttachments.and.returnValue(of(null));
    component.cvs = mockCanidiate.candidateAttachments;
    component.candidate = mockCanidiate;
    component.openCVs();
    expect(component.loading).toBe(false);
    expect(mockCandidateAttachmentService.downloadAttachments).toHaveBeenCalledWith(component.candidate, component.cvs);
  });

  it('should set loading to false after successful download', () => {
    mockCandidateAttachmentService.downloadAttachments.and.returnValue(of(null));
    component.cvs = mockCanidiate.candidateAttachments;
    component.candidate = mockCanidiate;
    component.openCVs();
    expect(component.loading).toBe(false);
  });

  it('should set loading to false and error message if download fails', () => {
    const error = 'Download failed';
    mockCandidateAttachmentService.downloadAttachments.and.returnValue(throwError(error));
    component.cvs = mockCanidiate.candidateAttachments;
    component.candidate = mockCanidiate;
    component.openCVs();
    expect(component.loading).toBe(false);
    expect(component.error).toBe(error);
  });
  it('should display loading spinner when loading is true', () => {
    component.candidate = mockCanidiate;
    component.loading = true;
    fixture.detectChanges();
    const spinner: DebugElement = fixture.debugElement.query(By.css('.fa-spinner'));
    expect(spinner).toBeTruthy();
  });

  it('should call openCVs when link is clicked', () => {
    component.candidate = mockCanidiate;
    spyOn(component, 'openCVs');
    mockAuthService.canViewCandidateCV.and.returnValue(true);
    component.cvs = mockCanidiate.candidateAttachments;
    fixture.detectChanges();
    const link: DebugElement = fixture.debugElement.query(By.css('.cv-icon'));
    expect(link).toBeTruthy();
    link.triggerEventHandler('click', null);
    expect(component.openCVs).toHaveBeenCalled();
  });
});
