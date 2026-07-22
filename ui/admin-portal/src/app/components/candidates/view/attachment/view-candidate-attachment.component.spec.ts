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
import {UntypedFormBuilder} from '@angular/forms';
import {of, throwError} from 'rxjs';

import {ViewCandidateAttachmentComponent} from './view-candidate-attachment.component';
import {CandidateAttachmentService} from '../../../../services/candidate-attachment.service';
import {CandidateService} from '../../../../services/candidate.service';
import {CreateCandidateAttachmentComponent} from './create/create-candidate-attachment.component';
import {EditCandidateAttachmentComponent} from './edit/edit-candidate-attachment.component';
import {ConfirmationComponent} from '../../../util/confirm/confirmation.component';
import {AttachmentType} from '../../../../model/candidate-attachment';
import {UploadType} from '../../../../model/task';

describe('ViewCandidateAttachmentComponent', () => {
  let component: ViewCandidateAttachmentComponent;
  let fixture: ComponentFixture<ViewCandidateAttachmentComponent>;

  let candidateAttachmentService:
    jasmine.SpyObj<CandidateAttachmentService>;
  let candidateService: jasmine.SpyObj<CandidateService>;
  let modalService: jasmine.SpyObj<NgbModal>;

  const candidate = {
    id: 10,
    candidateAttachments: []
  } as any;

  const attachment = {
    id: 20,
    name: 'passport.pdf',
    type: AttachmentType.file
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
    candidateAttachmentService =
      jasmine.createSpyObj<CandidateAttachmentService>(
        'CandidateAttachmentService',
        [
          'deleteAttachment',
          'downloadGoogleAttachment'
        ]
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
      declarations: [ViewCandidateAttachmentComponent],
      providers: [
        UntypedFormBuilder,
        {
          provide: CandidateAttachmentService,
          useValue: candidateAttachmentService
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
    .overrideTemplate(ViewCandidateAttachmentComponent, '')
    .compileComponents();

    fixture = TestBed.createComponent(
      ViewCandidateAttachmentComponent
    );
    component = fixture.componentInstance;
    component.candidate = candidate;
    component.editable = true;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should expose AttachmentType enum', () => {
    expect(component.AttachmentType).toBe(AttachmentType);
  });

  it('should expose UploadType enum', () => {
    expect(component.UploadType).toBe(UploadType);
  });

  it('should run ngOnInit', () => {
    expect(() => component.ngOnInit()).not.toThrow();
  });

  describe('editCandidateAttachment', () => {
    it('should open edit modal and refresh candidate after success', fakeAsync(() => {
      const ref = modalRef(Promise.resolve(attachment));
      modalService.open.and.returnValue(ref);

      component.editCandidateAttachment(attachment);
      tick();

      expect(modalService.open).toHaveBeenCalledWith(
        EditCandidateAttachmentComponent,
        {
          centered: true,
          backdrop: 'static'
        }
      );
      expect(ref.componentInstance.attachment).toBe(attachment);
      expect(candidateService.updateCandidate)
      .toHaveBeenCalledTimes(1);
    }));

    it('should ignore edit modal dismissal', fakeAsync(() => {
      const ref = modalRef(Promise.reject('dismissed'));
      modalService.open.and.returnValue(ref);

      component.editCandidateAttachment(attachment);
      tick();

      expect(ref.componentInstance.attachment).toBe(attachment);
      expect(candidateService.updateCandidate)
      .not.toHaveBeenCalled();
    }));
  });

  describe('addAttachment', () => {
    it('should open create modal with explicit type and refresh after success', fakeAsync(() => {
      const ref = modalRef(Promise.resolve(true));
      modalService.open.and.returnValue(ref);

      component.addAttachment('file');
      tick();

      expect(modalService.open).toHaveBeenCalledWith(
        CreateCandidateAttachmentComponent,
        {
          centered: true,
          backdrop: 'static'
        }
      );
      expect(ref.componentInstance.candidateId)
      .toBe(candidate.id);
      expect(ref.componentInstance.type)
      .toBe('file');
      expect(candidateService.updateCandidate)
      .toHaveBeenCalledTimes(1);
    }));

    it('should default type to link when type is falsy', fakeAsync(() => {
      const ref = modalRef(Promise.resolve(true));
      modalService.open.and.returnValue(ref);

      component.addAttachment('');
      tick();

      expect(ref.componentInstance.type)
      .toBe('link');
      expect(candidateService.updateCandidate)
      .toHaveBeenCalledTimes(1);
    }));

    it('should refresh candidate when create modal is dismissed', fakeAsync(() => {
      const ref = modalRef(Promise.reject('dismissed'));
      modalService.open.and.returnValue(ref);

      component.addAttachment('file');
      tick();

      expect(candidateService.updateCandidate)
      .toHaveBeenCalledTimes(1);
    }));
  });

  describe('deleteCandidateAttachment', () => {
    it('should delete after confirmation and refresh candidate', fakeAsync(() => {
      const ref = modalRef(Promise.resolve(true));
      modalService.open.and.returnValue(ref);
      candidateAttachmentService.deleteAttachment
      .and.returnValue(of(void 0));

      component.deleteCandidateAttachment(attachment);
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
        'Are you sure you want to delete passport.pdf?'
      );
      expect(candidateAttachmentService.deleteAttachment)
      .toHaveBeenCalledOnceWith(attachment.id);
      expect(candidateService.updateCandidate)
      .toHaveBeenCalledTimes(1);
    }));

    it('should expose delete error', fakeAsync(() => {
      const error = new Error('delete failed');
      const ref = modalRef(Promise.resolve(true));
      modalService.open.and.returnValue(ref);
      candidateAttachmentService.deleteAttachment
      .and.returnValue(throwError(error));

      component.deleteCandidateAttachment(attachment);
      tick();

      expect(component.error).toBe(error);
      expect(candidateService.updateCandidate)
      .not.toHaveBeenCalled();
    }));

    it('should not delete when confirmation is false', fakeAsync(() => {
      modalService.open.and.returnValue(
        modalRef(Promise.resolve(false))
      );

      component.deleteCandidateAttachment(attachment);
      tick();

      expect(candidateAttachmentService.deleteAttachment)
      .not.toHaveBeenCalled();
      expect(candidateService.updateCandidate)
      .not.toHaveBeenCalled();
    }));

    it('should ignore delete modal dismissal', fakeAsync(() => {
      modalService.open.and.returnValue(
        modalRef(Promise.reject('dismissed'))
      );

      component.deleteCandidateAttachment(attachment);
      tick();

      expect(candidateAttachmentService.deleteAttachment)
      .not.toHaveBeenCalled();
      expect(candidateService.updateCandidate)
      .not.toHaveBeenCalled();
    }));
  });

  describe('downloadGoogleCandidateAttachment', () => {
    it('should clear error, set loading and finish after success', () => {
      candidateAttachmentService.downloadGoogleAttachment
      .and.returnValue(of(void 0));

      component.error = new Error('old error');
      component.loading = false;

      component.downloadGoogleCandidateAttachment(attachment);

      expect(candidateAttachmentService.downloadGoogleAttachment)
      .toHaveBeenCalledOnceWith(
        attachment.id,
        attachment.name
      );
      expect(component.error).toBeNull();
      expect(component.loading).toBeFalse();
    });

    it('should expose download error and clear loading', () => {
      const error = new Error('download failed');

      candidateAttachmentService.downloadGoogleAttachment
      .and.returnValue(throwError(error));

      component.downloadGoogleCandidateAttachment(attachment);

      expect(component.error).toBe(error);
      expect(component.loading).toBeFalse();
    });
  });
});
