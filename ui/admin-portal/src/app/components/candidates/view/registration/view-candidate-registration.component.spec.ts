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
import {ViewCandidateRegistrationComponent} from "./view-candidate-registration.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {DatePipe} from "@angular/common";
import {By} from "@angular/platform-browser";
import {CandidateService} from "../../../../services/candidate.service";
import {NgbModal, NgbTooltip, NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";
import {MockCandidate} from "../../../../MockData/MockCandidate";

describe('ViewCandidateRegistrationComponent', () => {
  let component: ViewCandidateRegistrationComponent;
  let fixture: ComponentFixture<ViewCandidateRegistrationComponent>;
  let candidateService: jasmine.SpyObj<CandidateService>;
  let modalService: jasmine.SpyObj<NgbModal>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['get']);
    const modalServiceSpy = jasmine.createSpyObj('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      imports: [NgbTooltipModule],
      declarations: [ViewCandidateRegistrationComponent],
      providers: [
        DatePipe,
        { provide: CandidateService, useValue: candidateServiceSpy },
        { provide: NgbModal, useValue: modalServiceSpy }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    candidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    modalService = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateRegistrationComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show Verify+ scan badge when verify plus consent exists', () => {
    component.candidate.verifyPlusConsented = true;
    component.candidate.verifyPlusConsentedAt = '2026-09-04T11:05:00Z';
    fixture.detectChanges();

    const badgeEl: HTMLElement = fixture.nativeElement.querySelector('tc-badge');
    expect(badgeEl).not.toBeNull();
    expect(badgeEl.textContent).toContain('Verify+ scan');

    const tooltip = fixture.debugElement.query(By.directive(NgbTooltip))
      .injector.get(NgbTooltip);
    expect(tooltip.ngbTooltip).toBe(component.verifyPlusScanTooltip);
    expect(tooltip.ngbTooltip).toContain('Verify+ card scanned on');
  });

  it('should show date-unavailable tooltip when scan timestamp is missing', () => {
    component.candidate.verifyPlusConsented = true;
    component.candidate.verifyPlusConsentedAt = null;
    fixture.detectChanges();

    const badgeEl: HTMLElement = fixture.nativeElement.querySelector('tc-badge');
    expect(badgeEl).not.toBeNull();
    expect(badgeEl.textContent).toContain('Verify+ scan');

    const tooltip = fixture.debugElement.query(By.directive(NgbTooltip))
      .injector.get(NgbTooltip);
    expect(tooltip.ngbTooltip).toBe('Verify+ card scan date unavailable');
  });

  it('should hide Verify+ scan badge when verify plus consent is false', () => {
    component.candidate.verifyPlusConsented = false;
    component.candidate.verifyPlusConsentedAt = null;
    fixture.detectChanges();

    const badgeEl: HTMLElement = fixture.nativeElement.querySelector('tc-badge');
    expect(badgeEl).toBeNull();
  });
});
