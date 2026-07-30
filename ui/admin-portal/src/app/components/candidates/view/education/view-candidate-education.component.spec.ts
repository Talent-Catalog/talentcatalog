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
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {of, throwError} from 'rxjs';

import {ViewCandidateEducationComponent} from './view-candidate-education.component';
import {CandidateEducationService} from '../../../../services/candidate-education.service';
import {CandidateService} from '../../../../services/candidate.service';
import {EditCandidateEducationComponent} from './edit/edit-candidate-education.component';
import {CreateCandidateEducationComponent} from './create/create-candidate-education.component';
import {
  EditMaxEducationLevelComponent
} from './edit-max-education-level/edit-max-education-level.component';
import {ConfirmationComponent} from '../../../util/confirm/confirmation.component';

describe('ViewCandidateEducationComponent', () => {
  let component: ViewCandidateEducationComponent;
  let fixture: ComponentFixture<ViewCandidateEducationComponent>;

  let candidateEducationService: jasmine.SpyObj<CandidateEducationService>;
  let candidateService: jasmine.SpyObj<CandidateService>;
  let modalService: jasmine.SpyObj<NgbModal>;

  const candidate = {
    id: 10,
    maxEducationLevel: {
      id: 3,
      name: 'Bachelor'
    },
    candidateEducations: []
  } as any;

  const education = {
    id: 25,
    educationType: 'University',
    courseName: 'Computer Science'
  } as any;

  function modalRef(
    result: Promise<any>,
    componentInstance: Record<string, any> = {}
  ): NgbModalRef {
    return {
      componentInstance,
      result,
      close: jasmine.createSpy('close'),
      dismiss: jasmine.createSpy('dismiss')
    } as unknown as NgbModalRef;
  }

  beforeEach(async () => {
    candidateEducationService = jasmine.createSpyObj<CandidateEducationService>(
      'CandidateEducationService',
      ['delete']
    );

    candidateService = jasmine.createSpyObj<CandidateService>(
      'CandidateService',
      ['updateMaxEducationLevel', 'updateCandidate']
    );

    modalService = jasmine.createSpyObj<NgbModal>(
      'NgbModal',
      ['open']
    );

    candidateService.updateMaxEducationLevel.and.returnValue(of({} as any));

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateEducationComponent],
      providers: [
        {
          provide: CandidateEducationService,
          useValue: candidateEducationService
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
    .overrideTemplate(ViewCandidateEducationComponent, '')
    .compileComponents();

    fixture = TestBed.createComponent(ViewCandidateEducationComponent);
    component = fixture.componentInstance;
    component.candidate = {...candidate};
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

  describe('editMaxEducationLevel', () => {
    it('should open the modal, update the level and refresh the candidate', fakeAsync(() => {
      const ref = modalRef(Promise.resolve(7));
      modalService.open.and.returnValue(ref);

      component.editMaxEducationLevel();
      tick();

      expect(modalService.open).toHaveBeenCalledWith(
        EditMaxEducationLevelComponent,
        {
          centered: true,
          backdrop: 'static'
        }
      );
      expect(ref.componentInstance.educationLevel)
      .toBe(component.candidate.maxEducationLevel);
      expect(candidateService.updateMaxEducationLevel)
      .toHaveBeenCalledWith(component.candidate.id, {
        maxEducationLevel: 7
      });
      expect(candidateService.updateCandidate).toHaveBeenCalled();
    }));

    it('should not update when the modal returns a falsy level id', fakeAsync(() => {
      modalService.open.and.returnValue(
        modalRef(Promise.resolve(null))
      );

      component.editMaxEducationLevel();
      tick();

      expect(candidateService.updateMaxEducationLevel).not.toHaveBeenCalled();
      expect(candidateService.updateCandidate).not.toHaveBeenCalled();
    }));

    it('should ignore modal dismissal', fakeAsync(() => {
      modalService.open.and.returnValue(
        modalRef(Promise.reject('dismissed'))
      );

      component.editMaxEducationLevel();
      tick();

      expect(candidateService.updateMaxEducationLevel).not.toHaveBeenCalled();
      expect(candidateService.updateCandidate).not.toHaveBeenCalled();
    }));
  });

  describe('editCandidateEducation', () => {
    it('should open the edit modal and refresh after success', fakeAsync(() => {
      const ref = modalRef(Promise.resolve(education));
      modalService.open.and.returnValue(ref);

      component.editCandidateEducation(education);
      tick();

      expect(modalService.open).toHaveBeenCalledWith(
        EditCandidateEducationComponent,
        {
          centered: true,
          backdrop: 'static'
        }
      );
      expect(ref.componentInstance.candidateEducation).toBe(education);
      expect(candidateService.updateCandidate).toHaveBeenCalled();
    }));

    it('should ignore edit modal dismissal', fakeAsync(() => {
      modalService.open.and.returnValue(
        modalRef(Promise.reject('dismissed'))
      );

      component.editCandidateEducation(education);
      tick();

      expect(candidateService.updateCandidate).not.toHaveBeenCalled();
    }));
  });

  describe('createCandidateEducation', () => {
    it('should open the create modal and refresh after success', fakeAsync(() => {
      const ref = modalRef(Promise.resolve(education));
      modalService.open.and.returnValue(ref);

      component.createCandidateEducation();
      tick();

      expect(modalService.open).toHaveBeenCalledWith(
        CreateCandidateEducationComponent,
        {
          centered: true,
          backdrop: 'static'
        }
      );
      expect(ref.componentInstance.candidateId).toBe(component.candidate.id);
      expect(candidateService.updateCandidate).toHaveBeenCalled();
    }));

    it('should ignore create modal dismissal', fakeAsync(() => {
      modalService.open.and.returnValue(
        modalRef(Promise.reject('dismissed'))
      );

      component.createCandidateEducation();
      tick();

      expect(candidateService.updateCandidate).not.toHaveBeenCalled();
    }));
  });

  describe('deleteCandidateEducation', () => {
    it('should delete after confirmation and refresh the candidate', fakeAsync(() => {
      const ref = modalRef(Promise.resolve(true));
      modalService.open.and.returnValue(ref);
      candidateEducationService.delete.and.returnValue(of({} as any));
      component.loading = true;

      component.deleteCandidateEducation(education);
      tick();

      expect(modalService.open).toHaveBeenCalledWith(
        ConfirmationComponent,
        {
          centered: true,
          backdrop: 'static'
        }
      );
      expect(ref.componentInstance.message)
      .toBe('Are you sure you want to delete this education?');
      expect(candidateEducationService.delete)
      .toHaveBeenCalledWith(education.id);
      expect(component.loading).toBeFalse();
      expect(candidateService.updateCandidate).toHaveBeenCalled();
    }));

    it('should expose delete errors and clear loading', fakeAsync(() => {
      const error = new Error('delete failed');
      modalService.open.and.returnValue(
        modalRef(Promise.resolve(true))
      );
      candidateEducationService.delete.and.returnValue(
        throwError(error)
      );
      component.loading = true;

      component.deleteCandidateEducation(education);
      tick();

      expect(candidateEducationService.delete)
      .toHaveBeenCalledWith(education.id);
      expect(component.error).toBe(error);
      expect(component.loading).toBeFalse();
      expect(candidateService.updateCandidate).not.toHaveBeenCalled();
    }));

    it('should not delete when confirmation result is false', fakeAsync(() => {
      modalService.open.and.returnValue(
        modalRef(Promise.resolve(false))
      );

      component.deleteCandidateEducation(education);
      tick();

      expect(candidateEducationService.delete).not.toHaveBeenCalled();
      expect(candidateService.updateCandidate).not.toHaveBeenCalled();
    }));

    it('should ignore delete modal dismissal', fakeAsync(() => {
      modalService.open.and.returnValue(
        modalRef(Promise.reject('dismissed'))
      );

      component.deleteCandidateEducation(education);
      tick();

      expect(candidateEducationService.delete).not.toHaveBeenCalled();
      expect(candidateService.updateCandidate).not.toHaveBeenCalled();
    }));
  });
});
