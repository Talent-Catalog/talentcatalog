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
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA, SimpleChange} from '@angular/core';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {of, throwError} from 'rxjs';

import {ViewCandidateCertificationComponent} from './view-candidate-certification.component';
import {CandidateCertificationService} from '../../../../services/candidate-certification.service';
import {CandidateService} from '../../../../services/candidate.service';
import {EditCandidateCertificationComponent} from './edit/edit-candidate-certification.component';
import {
  CreateCandidateCertificationComponent
} from './create/create-candidate-certification.component';
import {ConfirmationComponent} from '../../../util/confirm/confirmation.component';
import {MockCandidate} from "../../../../MockData/MockCandidate";

describe('ViewCandidateCertificationComponent', () => {
  let component: ViewCandidateCertificationComponent;
  let fixture: ComponentFixture<ViewCandidateCertificationComponent>;

  let candidateCertificationService:
    jasmine.SpyObj<CandidateCertificationService>;
  let candidateService: jasmine.SpyObj<CandidateService>;
  let modalService: jasmine.SpyObj<NgbModal>;

  const candidate = new MockCandidate();

  const certification = {
    id: 20,
    name: 'AWS Certified Developer',
    institution: 'Amazon'
  } as any;

  function modalRef(result: Promise<any>): NgbModalRef {
    return {
      componentInstance: {},
      result,
      close: jasmine.createSpy('close'),
      dismiss: jasmine.createSpy('dismiss')
    } as unknown as NgbModalRef;
  }

  beforeEach(async () => {
    candidateCertificationService =
      jasmine.createSpyObj<CandidateCertificationService>(
        'CandidateCertificationService',
        ['delete']
      );

    candidateService = jasmine.createSpyObj<CandidateService>(
      'CandidateService',
      ['updateCandidate']
    );

    modalService = jasmine.createSpyObj<NgbModal>(
      'NgbModal',
      ['open']
    );

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateCertificationComponent],
      providers: [
        {
          provide: CandidateCertificationService,
          useValue: candidateCertificationService
        },
        {
          provide: CandidateService,
          useValue: candidateService
        },
        {
          provide: NgbModal,
          useValue: modalService
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .overrideTemplate(ViewCandidateCertificationComponent, '')
    .compileComponents();

    fixture = TestBed.createComponent(
      ViewCandidateCertificationComponent
    );
    component = fixture.componentInstance;

    component.candidate = candidate;
    component.editable = true;
    component.adminUser = true;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should run ngOnInit', () => {
    expect(() => component.ngOnInit()).not.toThrow();
  });

  it('should run ngOnChanges', () => {
    expect(() =>
      component.ngOnChanges({
        candidate: new SimpleChange(null, candidate, true)
      })
    ).not.toThrow();
  });

  describe('editCandidateCertification', () => {
    it('should open edit modal and refresh candidate after success', fakeAsync(() => {
      const ref = modalRef(Promise.resolve(certification));
      modalService.open.and.returnValue(ref);

      component.editCandidateCertification(certification);
      tick();

      expect(modalService.open).toHaveBeenCalledWith(
        EditCandidateCertificationComponent,
        {
          centered: true,
          backdrop: 'static'
        }
      );

      expect(ref.componentInstance.candidateCertification)
      .toBe(certification);

      expect(candidateService.updateCandidate)
      .toHaveBeenCalledTimes(1);
    }));

    it('should ignore edit modal dismissal', fakeAsync(() => {
      const ref = modalRef(Promise.reject('dismissed'));
      modalService.open.and.returnValue(ref);

      component.editCandidateCertification(certification);
      tick();

      expect(ref.componentInstance.candidateCertification)
      .toBe(certification);

      expect(candidateService.updateCandidate)
      .not.toHaveBeenCalled();
    }));
  });

  describe('createCandidateCertification', () => {
    it('should open create modal and refresh candidate after success', fakeAsync(() => {
      const ref = modalRef(Promise.resolve(certification));
      modalService.open.and.returnValue(ref);

      component.createCandidateCertification();
      tick();

      expect(modalService.open).toHaveBeenCalledWith(
        CreateCandidateCertificationComponent,
        {
          centered: true,
          backdrop: 'static'
        }
      );

      expect(ref.componentInstance.candidateId)
      .toBe(candidate.id);

      expect(candidateService.updateCandidate)
      .toHaveBeenCalledTimes(1);
    }));

    it('should ignore create modal dismissal', fakeAsync(() => {
      const ref = modalRef(Promise.reject('dismissed'));
      modalService.open.and.returnValue(ref);

      component.createCandidateCertification();
      tick();

      expect(ref.componentInstance.candidateId)
      .toBe(candidate.id);

      expect(candidateService.updateCandidate)
      .not.toHaveBeenCalled();
    }));
  });

  describe('deleteCandidateCertification', () => {
    it('should delete after confirmation and refresh candidate', fakeAsync(() => {
      const ref = modalRef(Promise.resolve(true));
      modalService.open.and.returnValue(ref);

      candidateCertificationService.delete
      .and.returnValue(of({} as any));

      component.loading = true;

      component.deleteCandidateCertification(certification);
      tick();

      expect(modalService.open).toHaveBeenCalledWith(
        ConfirmationComponent,
        {
          centered: true,
          backdrop: 'static'
        }
      );

      expect(ref.componentInstance.message)
      .toBe(
        'Are you sure you want to delete this certification?'
      );

      expect(candidateCertificationService.delete)
      .toHaveBeenCalledOnceWith(certification.id);

      expect(component.loading).toBeFalse();

      expect(candidateService.updateCandidate)
      .toHaveBeenCalledTimes(1);
    }));

    it('should expose delete error and clear loading', fakeAsync(() => {
      const error = new Error('delete failed');
      const ref = modalRef(Promise.resolve(true));
      modalService.open.and.returnValue(ref);

      candidateCertificationService.delete
      .and.returnValue(throwError(error));

      component.loading = true;

      component.deleteCandidateCertification(certification);
      tick();

      expect(component.error).toBe(error);
      expect(component.loading).toBeFalse();

      expect(candidateService.updateCandidate)
      .not.toHaveBeenCalled();
    }));

    it('should not delete when confirmation is false', fakeAsync(() => {
      modalService.open.and.returnValue(
        modalRef(Promise.resolve(false))
      );

      component.deleteCandidateCertification(certification);
      tick();

      expect(candidateCertificationService.delete)
      .not.toHaveBeenCalled();

      expect(candidateService.updateCandidate)
      .not.toHaveBeenCalled();
    }));

    it('should ignore delete modal dismissal', fakeAsync(() => {
      modalService.open.and.returnValue(
        modalRef(Promise.reject('dismissed'))
      );

      component.deleteCandidateCertification(certification);
      tick();

      expect(candidateCertificationService.delete)
      .not.toHaveBeenCalled();

      expect(candidateService.updateCandidate)
      .not.toHaveBeenCalled();
    }));
  });
});
