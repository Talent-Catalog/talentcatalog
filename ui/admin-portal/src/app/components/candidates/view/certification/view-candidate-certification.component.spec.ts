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

import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {
  ViewCandidateJobExperienceComponent
} from "../occupation/experience/view-candidate-job-experience.component";
import {CandidateJobExperienceService} from "../../../../services/candidate-job-experience.service";
import {CandidateService} from "../../../../services/candidate.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Candidate} from "../../../../model/candidate";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {CandidateOccupation} from "../../../../model/candidate-occupation";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {By} from "@angular/platform-browser";
import {throwError} from "rxjs";

describe('ViewCandidateCertificationComponent', () => {
  let component: ViewCandidateJobExperienceComponent;
  let fixture: ComponentFixture<ViewCandidateJobExperienceComponent>;
  let mockCandidateJobExperienceService: jasmine.SpyObj<CandidateJobExperienceService>;
  let mockCandidateService: jasmine.SpyObj<CandidateService>;
  let mockModalService: jasmine.SpyObj<NgbModal>;

  // Mock data
  const mockCandidate: Candidate = new MockCandidate();

  const mockCandidateOccupation: CandidateOccupation = {
    id: 1,
    occupation: { id: 1, name: 'Software Developer' },
    yearsExperience: 5,
    migrationOccupation: 'Engineer'
  } as CandidateOccupation;

  beforeEach(async () => {
    // Create spy objects for services
    mockCandidateJobExperienceService = jasmine.createSpyObj('CandidateJobExperienceService', ['delete']);
    mockCandidateService = jasmine.createSpyObj('CandidateService', ['updateCandidate']);
    mockModalService = jasmine.createSpyObj('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateJobExperienceComponent],
      providers: [
        { provide: CandidateJobExperienceService, useValue: mockCandidateJobExperienceService },
        { provide: CandidateService, useValue: mockCandidateService },
        { provide: NgbModal, useValue: mockModalService }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA] // Ignore custom directives like appHighlightSearch
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateJobExperienceComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    component.candidateOccupation = mockCandidateOccupation;
    component.editable = true;
    component.adminUser = true;
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display error state', () => {
    component.error = 'Failed to load data';
    component.loading = false;

    fixture.detectChanges();

    const errorElement =
      fixture.debugElement.query(By.css('tc-alert[type="danger"]'));
    expect(errorElement).toBeTruthy();
    expect(errorElement.nativeElement.textContent).toContain('Failed to load data');
  });

  it('should render candidate occupation details', () => {
    component.loading = false;
    fixture.detectChanges();
    const cardHeader = fixture.debugElement.query(By.css('tc-card-header'));
    expect(cardHeader.nativeElement.textContent).toContain('Software Developer (5 years)');
    expect(cardHeader.nativeElement.textContent).toContain('Migrated Occupation: Engineer');
  });

  it('should display empty state when no experiences', () => {
    component.experiences = [];
    component.loading = false;
    fixture.detectChanges();
    const emptyMessage = fixture.debugElement.query(By.css('p'));
    expect(emptyMessage.nativeElement.textContent).toContain('No job experience data has been entered by this candidate.');
  });

  it('should emit deleteOccupation event when deleting occupation with no experiences', () => {
    component.experiences = [];
    const emitSpy = jasmine.createSpy('deleteOccupation');
    component.deleteOccupation.subscribe(emitSpy);

    component.deleteCandidateOccupation();
    expect(emitSpy).toHaveBeenCalledWith(mockCandidateOccupation);
  });

  it('should handle error when deleting job experience', fakeAsync(() => {
    const jobExperience = mockCandidate.candidateJobExperiences[0];
    mockModalService.open.and.returnValue({
      componentInstance: { message: null },
      result: Promise.resolve(true)
    } as any);
    mockCandidateJobExperienceService.delete.and.returnValue(throwError('Delete error'));

    component.deleteCandidateJobExperience(jobExperience);
    tick();

    expect(mockCandidateJobExperienceService.delete).toHaveBeenCalledWith(jobExperience.id);
    expect(component.error).toBe('Delete error');
    expect(component.loading).toBeFalse();
  }));

  it('should not show edit buttons when editable is false', () => {
    component.editable = false;
    component.loading = false;
    fixture.detectChanges();
    const createButton = fixture.debugElement.query(By.css('.btn-primary'));
    const editOccupationButton = fixture.debugElement.query(By.css('.btn-secondary'));
    const editJobExperienceButton = fixture.debugElement.query(By.css('.btn-default'));
    expect(createButton).toBeNull();
    expect(editOccupationButton).toBeNull();
    expect(editJobExperienceButton).toBeNull();
  });
});
