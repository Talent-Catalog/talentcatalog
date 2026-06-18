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

import {DownloadCvComponent} from "./download-cv.component";
import {CandidateService, DownloadCVRequest} from "../../../services/candidate.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {of, throwError} from "rxjs";
import {By} from "@angular/platform-browser";

describe('DownloadCvComponent', () => {
  const waitForAsyncTasks = () => new Promise(resolve => setTimeout(resolve, 100));

  let component: DownloadCvComponent;
  let fixture: ComponentFixture<DownloadCvComponent>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let windowOpenSpy: jasmine.Spy;
  let createObjectUrlSpy: jasmine.Spy;

  beforeEach(async () => {
    const candidateService = jasmine.createSpyObj('CandidateService', ['downloadCv']);
    const activeModal = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [DownloadCvComponent],
      imports: [ReactiveFormsModule],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateService, useValue: candidateService },
        { provide: NgbActiveModal, useValue: activeModal }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DownloadCvComponent);
    component = fixture.componentInstance;
    component.candidateId = 99;

    candidateServiceSpy = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;

    windowOpenSpy = spyOn(window, 'open')
    .and.returnValue({location: {href: ''}} as Window);

    createObjectUrlSpy = spyOn(URL, 'createObjectURL')
    .and.returnValue('blob:mock-cv-url');

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default values', () => {
    expect(component.form).toBeDefined();
    expect(component.form.value).toEqual({
      name: false,
      contact: false,
      format: 'PDF'
    });
  });

  it('should call downloadCv and close modal on success for PDF', () => {
    const request: DownloadCVRequest = {
      candidateId: 99,
      showName: false,
      showContact: false,
      format: 'PDF'
    };

    const fakeBlob = new Blob(['pdf']);
    candidateServiceSpy.downloadCv.and.returnValue(of(fakeBlob));

    component.form.patchValue({format: 'PDF'});
    component.onSave();

    expect(candidateServiceSpy.downloadCv).toHaveBeenCalledWith(request);
    expect(windowOpenSpy).toHaveBeenCalled();
    expect(createObjectUrlSpy).toHaveBeenCalledWith(fakeBlob);
    expect(activeModalSpy.close).toHaveBeenCalled();
  });

  it('should call downloadCv and close modal on success for DOCX', () => {
    const request: DownloadCVRequest = {
      candidateId: 99,
      showName: false,
      showContact: false,
      format: 'DOCX'
    };

    const fakeBlob = new Blob(['docx']);
    candidateServiceSpy.downloadCv.and.returnValue(of(fakeBlob));

    component.form.patchValue({format: 'DOCX'});
    component.onSave();

    expect(candidateServiceSpy.downloadCv).toHaveBeenCalledWith(request);
    expect(windowOpenSpy).toHaveBeenCalled();
    expect(createObjectUrlSpy).toHaveBeenCalledWith(fakeBlob);
    expect(activeModalSpy.close).toHaveBeenCalled();
  });

  it('should pass name and contact values to downloadCv request', () => {
    const request: DownloadCVRequest = {
      candidateId: 99,
      showName: true,
      showContact: true,
      format: 'PDF'
    };

    const fakeBlob = new Blob(['pdf']);
    candidateServiceSpy.downloadCv.and.returnValue(of(fakeBlob));

    component.form.patchValue({
      name: true,
      contact: true,
      format: 'PDF'
    });

    component.onSave();

    expect(candidateServiceSpy.downloadCv).toHaveBeenCalledWith(request);
    expect(activeModalSpy.close).toHaveBeenCalled();
  });

  it('should call downloadCv and open Google Doc URL on success for GOOGLE_DOC', async () => {
    const googleDocUrl = 'https://docs.google.com/document/d/mock-google-doc-id/edit';

    const request: DownloadCVRequest = {
      candidateId: 99,
      showName: false,
      showContact: false,
      format: 'GOOGLE_DOC'
    };

    const fakeBlob = new Blob([googleDocUrl], {type: 'text/plain;charset=utf-8'});
    candidateServiceSpy.downloadCv.and.returnValue(of(fakeBlob));

    const mockGoogleDocWindow = {
      opener: null,
      document: {
        write: jasmine.createSpy('write')
      },
      location: {
        href: ''
      },
      close: jasmine.createSpy('close')
    } as unknown as Window;

    windowOpenSpy.and.returnValue(mockGoogleDocWindow);

    activeModalSpy.close.calls.reset();

    component.form.patchValue({format: 'GOOGLE_DOC'});
    component.onSave();

    await waitForAsyncTasks();

    expect(candidateServiceSpy.downloadCv).toHaveBeenCalledWith(request);
    expect(createObjectUrlSpy).not.toHaveBeenCalled();

    expect(windowOpenSpy).toHaveBeenCalledWith('', '_blank');
    expect(mockGoogleDocWindow.document.write).toHaveBeenCalledWith('Creating Google Doc...');
    expect(mockGoogleDocWindow.location.href).toBe(googleDocUrl);

    expect(component.googleDocUrl).toBe(googleDocUrl);
    expect(component.googleDocPopupBlocked).toBeFalse();
    expect(component.saving).toBeFalse();
  });

  it('should set error message on failure', () => {
    const errorResponse = 'Error downloading CV';
    candidateServiceSpy.downloadCv.and.returnValue(throwError(errorResponse));
    component.form.patchValue({format: 'PDF'});
    component.onSave();

    expect(component.error).toBe(errorResponse);
    expect(activeModalSpy.close).not.toHaveBeenCalled();
  });

  it('should close the modal', () => {
    component.closeModal();

    expect(activeModalSpy.close).toHaveBeenCalled();
  });

  it('should dismiss the modal', () => {
    component.dismiss();

    expect(activeModalSpy.dismiss).toHaveBeenCalledWith(false);
  });

  it('should disable save button if loading', () => {
    component.loading = true;
    fixture.detectChanges();

    const saveButton = fixture.debugElement.nativeElement.querySelector(
      '.modal-footer .btn-primary'
    );

    expect(saveButton.disabled).toBeTruthy();
  });

  it('should disable save button if saving', () => {
    component.loading = false;
    component.saving = true;
    fixture.detectChanges();

    const saveButton = fixture.debugElement.nativeElement.querySelector(
      '.modal-footer .btn-primary'
    );

    expect(saveButton.disabled).toBeTruthy();
  });

  it('should show error message if error is present', () => {
    component.error = 'Test error message';
    fixture.detectChanges();

    const errorMessage = fixture.debugElement.nativeElement.querySelector('tc-alert');

    expect(errorMessage).toBeTruthy();
    expect(errorMessage.textContent).toContain('Test error message');
  });

  it('should call onSave when save button is clicked', () => {
    spyOn(component, 'onSave');

    component.form.setValue({
      name: true,
      contact: true,
      format: 'PDF'
    });
    component.loading = false;
    component.saving = false;
    fixture.detectChanges();

    const saveButton = fixture.debugElement.nativeElement.querySelector(
      '.modal-footer .btn-primary'
    );

    saveButton.click();

    expect(component.onSave).toHaveBeenCalled();
  });

  it('should call dismiss when cancel button is clicked', () => {
    const dismissSpy = spyOn(component, 'dismiss');
    fixture.detectChanges();

    const tcModalDE = fixture.debugElement.query(By.css('tc-modal'));
    (tcModalDE.componentInstance as any).onCancel.subscribe(() => component.dismiss());

    const cancelBtn: HTMLButtonElement =
      fixture.debugElement.nativeElement.querySelector('.modal-footer .btn-secondary');

    expect(cancelBtn).toBeTruthy();

    cancelBtn.click();

    expect(dismissSpy).toHaveBeenCalled();
  });
});
