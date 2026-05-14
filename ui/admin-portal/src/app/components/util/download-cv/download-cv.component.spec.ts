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

import {DownloadCvComponent} from "./download-cv.component";
import {CandidateService, DownloadCVRequest} from "../../../services/candidate.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {of, throwError} from "rxjs";
import {By} from "@angular/platform-browser";

describe('DownloadCvComponent', () => {
  let component: DownloadCvComponent;
  let fixture: ComponentFixture<DownloadCvComponent>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let windowOpenSpy: jasmine.Spy;

  beforeEach(async () => {
    const candidateService = jasmine.createSpyObj('CandidateService', ['downloadCv', 'downloadCvDocx']);
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
    candidateServiceSpy = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    windowOpenSpy = spyOn(window, 'open').and.returnValue({ location: { href: '' } } as Window);

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
      format: 'pdf'
    });
  });

  it('should call downloadCv and close modal on success for pdf', () => {
    const request: DownloadCVRequest = {
      candidateId: component.candidateId,
      showName: false,
      showContact: false,
    };

    const fakeBlob = new Blob();
    candidateServiceSpy.downloadCv.and.returnValue(of(fakeBlob));

    component.form.patchValue({ format: 'pdf' });
    component.onSave();

    expect(candidateServiceSpy.downloadCv).toHaveBeenCalledWith(request);
    expect(candidateServiceSpy.downloadCvDocx).not.toHaveBeenCalled();
    expect(activeModalSpy.close).toHaveBeenCalled();
    expect(windowOpenSpy).toHaveBeenCalled();
  });

  it('should call downloadCvDocx and close modal on success for docx', () => {
    const request: DownloadCVRequest = {
      candidateId: component.candidateId,
      showName: false,
      showContact: false,
    };

    const fakeBlob = new Blob();
    candidateServiceSpy.downloadCvDocx.and.returnValue(of(fakeBlob));

    component.form.patchValue({ format: 'docx' });
    component.onSave();

    expect(candidateServiceSpy.downloadCvDocx).toHaveBeenCalledWith(request);
    expect(candidateServiceSpy.downloadCv).not.toHaveBeenCalled();
    expect(activeModalSpy.close).toHaveBeenCalled();
    expect(windowOpenSpy).toHaveBeenCalled();
  });

  it('should set error message on failure', () => {
    const errorResponse = 'Error downloading CV';
    candidateServiceSpy.downloadCv.and.returnValue(throwError(errorResponse));
    component.form.patchValue({ format: 'pdf' });
    
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

    const saveButton = fixture.debugElement.nativeElement.querySelector('.modal-footer .btn-primary');
    expect(saveButton.disabled).toBeTruthy();
  });

  it('should disable save button if saving', () => {
    component.loading = false;
    component.saving = true;
    fixture.detectChanges();

    const saveButton = fixture.debugElement.nativeElement.querySelector('.modal-footer .btn-primary');
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

    component.form.setValue({ name: true, contact: true, format: 'pdf' });
    component.loading = false;
    component.saving = false;
    fixture.detectChanges();

    const saveButton = fixture.debugElement.nativeElement.querySelector('.modal-footer .btn-primary');
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
