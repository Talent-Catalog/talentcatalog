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

import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {PotentialDuplicateIconComponent} from './potential-duplicate-icon.component';
import {CandidateService} from '../../../../services/candidate.service';
import {AuthorizationService} from '../../../../services/authorization.service';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {MockCandidate} from '../../../../MockData/MockCandidate';
import {By} from '@angular/platform-browser';
import {of, throwError} from 'rxjs';

describe('PotentialDuplicateIconComponent', () => {
  let component: PotentialDuplicateIconComponent;
  let fixture: ComponentFixture<PotentialDuplicateIconComponent>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let authorizationServiceSpy: jasmine.SpyObj<AuthorizationService>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;
  let mockCandidate: MockCandidate;

  beforeEach(async () => {
    const candidateService = jasmine.createSpyObj<CandidateService>('CandidateService', ['fetchPotentialDuplicates']);
    const authorizationService = jasmine.createSpyObj<AuthorizationService>('AuthorizationService', ['canViewCandidateName']);
    const modalService = jasmine.createSpyObj<NgbModal>('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      declarations: [PotentialDuplicateIconComponent],
      providers: [
        {provide: CandidateService, useValue: candidateService},
        {provide: AuthorizationService, useValue: authorizationService},
        {provide: NgbModal, useValue: modalService},
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PotentialDuplicateIconComponent);
    component = fixture.componentInstance;
    mockCandidate = new MockCandidate();
    mockCandidate.id = 1;
    component.candidate = mockCandidate;
    candidateServiceSpy = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    authorizationServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    modalServiceSpy = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;

    component.candidate.potentialDuplicate = true;
    authorizationServiceSpy.canViewCandidateName.and.returnValue(true);

    fixture.detectChanges();
  });

  afterEach(() => {
    // Reset component state to prevent side effects
    component.candidate = mockCandidate;
    component.loading = false;
    component.error = null;
    candidateServiceSpy.fetchPotentialDuplicates.calls.reset();
    fixture.destroy();
  });

  it('should not display icon when candidate is not a potential duplicate', () => {
    component.candidate.potentialDuplicate = false;
    fixture.detectChanges();

    const potentialDuplicateElement = fixture.debugElement.query(By.css('span i.fa-person-circle-exclamation'));
    expect(potentialDuplicateElement).toBeNull();
  });

  it('should not display icon when user cannot view candidate name', () => {
    authorizationServiceSpy.canViewCandidateName.and.returnValue(false);
    component.candidate.potentialDuplicate = true;
    fixture.detectChanges();

    const potentialDuplicateElement = fixture.debugElement.query(By.css('span i.fa-person-circle-exclamation'));
    expect(potentialDuplicateElement).toBeNull();
  });

  it('should display icon when candidate is a potential duplicate and user can view name', () => {
    component.candidate.potentialDuplicate = true;
    authorizationServiceSpy.canViewCandidateName.and.returnValue(true);
    fixture.detectChanges();

    const potentialDuplicateElement = fixture.debugElement.query(By.css('span i.fa-person-circle-exclamation'));
    expect(potentialDuplicateElement).toBeTruthy();
  });


  it('should emit refresh event when modal closes and no duplicates are found', fakeAsync(() => {
    const modalRef = {
      componentInstance: {selectedCandidate: null},
      result: Promise.resolve()
    } as NgbModalRef;
    modalServiceSpy.open.and.returnValue(modalRef);
    candidateServiceSpy.fetchPotentialDuplicates.and.returnValue(of([]));
    spyOn(component.refresh, 'emit');

    component.candidate.potentialDuplicate = true;
    authorizationServiceSpy.canViewCandidateName.and.returnValue(true);
    fixture.detectChanges();

    const iconElement = fixture.debugElement.query(By.css('i.fa-person-circle-exclamation'));
    iconElement.triggerEventHandler('click', null);

    tick();
    fixture.detectChanges();

    expect(candidateServiceSpy.fetchPotentialDuplicates).toHaveBeenCalledWith(mockCandidate.id);
    expect(component.refresh.emit).toHaveBeenCalled();
    expect(component.loading).toBeFalse();
  }));

  it('should set error and clear loading when modal closes and fetch fails', fakeAsync(() => {
    const modalRef = {
      componentInstance: {selectedCandidate: null},
      result: Promise.resolve()
    } as NgbModalRef;
    modalServiceSpy.open.and.returnValue(modalRef);
    const errorMessage = 'Failed to fetch duplicates';
    candidateServiceSpy.fetchPotentialDuplicates.and.returnValue(throwError(errorMessage));

    component.candidate.potentialDuplicate = true;
    authorizationServiceSpy.canViewCandidateName.and.returnValue(true);
    fixture.detectChanges();

    const iconElement = fixture.debugElement.query(By.css('i.fa-person-circle-exclamation'));
    iconElement.triggerEventHandler('click', null);

    tick();
    fixture.detectChanges();

    expect(candidateServiceSpy.fetchPotentialDuplicates).toHaveBeenCalledWith(mockCandidate.id);
    expect(component.error).toBe(errorMessage);
    expect(component.loading).toBeFalse();
  }));

  it('should emit refresh event when modal is dismissed and no duplicates are found', fakeAsync(() => {
    const modalRef = {
      componentInstance: {selectedCandidate: null},
      result: Promise.reject('dismissed')
    } as NgbModalRef;
    modalServiceSpy.open.and.returnValue(modalRef);
    candidateServiceSpy.fetchPotentialDuplicates.and.returnValue(of([]));
    spyOn(component.refresh, 'emit');

    component.candidate.potentialDuplicate = true;
    authorizationServiceSpy.canViewCandidateName.and.returnValue(true);
    fixture.detectChanges();

    const iconElement = fixture.debugElement.query(By.css('i.fa-person-circle-exclamation'));
    iconElement.triggerEventHandler('click', null);

    tick();
    fixture.detectChanges();

    expect(candidateServiceSpy.fetchPotentialDuplicates).toHaveBeenCalledWith(mockCandidate.id);
    expect(component.refresh.emit).toHaveBeenCalled();
    expect(component.loading).toBeFalse();
  }));

  it('should set error and clear loading when modal is dismissed and fetch fails', fakeAsync(() => {
    const modalRef = {
      componentInstance: {selectedCandidate: null},
      result: Promise.reject('dismissed')
    } as NgbModalRef;
    modalServiceSpy.open.and.returnValue(modalRef);
    const errorMessage = 'Failed to fetch duplicates';
    candidateServiceSpy.fetchPotentialDuplicates.and.returnValue(throwError(errorMessage));

    component.candidate.potentialDuplicate = true;
    authorizationServiceSpy.canViewCandidateName.and.returnValue(true);
    fixture.detectChanges();

    const iconElement = fixture.debugElement.query(By.css('i.fa-person-circle-exclamation'));
    iconElement.triggerEventHandler('click', null);

    tick();
    fixture.detectChanges();

    expect(candidateServiceSpy.fetchPotentialDuplicates).toHaveBeenCalledWith(mockCandidate.id);
    expect(component.error).toBe(errorMessage);
    expect(component.loading).toBeFalse();
  }));

  it('should display loading spinner when loading is true', () => {
    component.candidate.potentialDuplicate = true;
    authorizationServiceSpy.canViewCandidateName.and.returnValue(true);
    component.loading = true;
    fixture.detectChanges();

    const spinnerElement = fixture.debugElement.query(By.css('i.fa-spinner'));
    expect(spinnerElement).toBeTruthy();
    const iconElement = fixture.debugElement.query(By.css('i.fa-person-circle-exclamation'));
    expect(iconElement).toBeNull();
  });

  it('should display error message when error is set', () => {
    component.candidate.potentialDuplicate = true;
    authorizationServiceSpy.canViewCandidateName.and.returnValue(true);
    component.error = 'Test error';
    fixture.detectChanges();

    const errorElement = fixture.debugElement.query(By.css('.alert.alert-danger'));
    expect(errorElement).toBeTruthy();
    expect(errorElement.nativeElement.textContent).toContain('Test error');
  });

});
