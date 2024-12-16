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
import {ViewCandidateSpecialLinksComponent} from "./view-candidate-special-links.component";
import {CandidateService} from "../../../../services/candidate.service";
import {AuthorizationService} from "../../../../services/authorization.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {Candidate} from "../../../../model/candidate";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {of, throwError} from "rxjs";

describe('ViewCandidateSpecialLinksComponent', () => {
  let component: ViewCandidateSpecialLinksComponent;
  let fixture: ComponentFixture<ViewCandidateSpecialLinksComponent>;
  let candidateService: jasmine.SpyObj<CandidateService>;
  let authService: jasmine.SpyObj<AuthorizationService>;
  let modalService: jasmine.SpyObj<NgbModal>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['createCandidateFolder', 'createUpdateLiveCandidate', 'updateCandidate']);
    const authServiceSpy = jasmine.createSpyObj('AuthorizationService', ['canAccessSalesforce']);
    const modalServiceSpy = jasmine.createSpyObj('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateSpecialLinksComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        { provide: CandidateService, useValue: candidateServiceSpy },
        { provide: AuthorizationService, useValue: authServiceSpy },
        { provide: NgbModal, useValue: modalServiceSpy }
      ]
    }).compileComponents();

    candidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    authService = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    modalService = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateSpecialLinksComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with input properties', () => {
    const candidate: Candidate =
    component.candidate = mockCandidate;
    component.editable = true;

    fixture.detectChanges();

    expect(component.candidate).toEqual(candidate);
    expect(component.editable).toBe(true);
  });

  it('should handle createCandidateFolder correctly', () => {
    const candidate: Candidate = mockCandidate;
    const updatedCandidate: Candidate = { ...candidate, folderlink: 'https://drive.google.com' };
    component.candidate = candidate;

    candidateService.createCandidateFolder.and.returnValue(of(updatedCandidate));

    component.createCandidateFolder();

    fixture.detectChanges();
    expect(component.loading).toBe(false);
    expect(candidateService.updateCandidate).toHaveBeenCalled()
  });

  it('should handle createCandidateFolder error', () => {
    const candidate: Candidate = mockCandidate;
    const errorResponse = 'Error creating folder';
    component.candidate = candidate;

    candidateService.createCandidateFolder.and.returnValue(throwError(errorResponse));

    component.createCandidateFolder();

    fixture.detectChanges();
    expect(component.loading).toBe(false);
    expect(component.error).toBe(errorResponse);
  });

  it('should handle createUpdateSalesforce correctly', () => {
    const candidate: Candidate =mockCandidate;
    const updatedCandidate: Candidate = { ...candidate, sflink: 'https://salesforce.com' };
    component.candidate = candidate;

    candidateService.createUpdateLiveCandidate.and.returnValue(of(updatedCandidate));

    component.createUpdateSalesforce();

    fixture.detectChanges();
    expect(component.loading).toBe(false);
    expect(candidateService.updateCandidate).toHaveBeenCalled()
  });

  it('should handle createUpdateSalesforce error', () => {
    const candidate: Candidate = mockCandidate;
    const errorResponse = 'Error updating Salesforce';
    component.candidate = candidate;

    candidateService.createUpdateLiveCandidate.and.returnValue(throwError(errorResponse));

    component.createUpdateSalesforce();

    fixture.detectChanges();
    expect(component.loading).toBe(false);
    expect(component.error).toBe(errorResponse);
  });


  it('should check Salesforce access', () => {
    authService.canAccessSalesforce.and.returnValue(true);

    expect(component.canAccessSalesforce()).toBe(true);
    expect(authService.canAccessSalesforce).toHaveBeenCalled();
  });
});
