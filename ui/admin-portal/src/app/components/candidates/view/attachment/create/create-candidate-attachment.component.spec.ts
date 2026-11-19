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

import type {MockedObject} from "vitest";
import {CreateCandidateAttachmentComponent} from "./create-candidate-attachment.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateAttachmentService} from "../../../../../services/candidate-attachment.service";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {AttachmentType, CandidateAttachmentRequest} from "../../../../../model/candidate-attachment";
import {UploadType} from "../../../../../model/task";
import {of, throwError} from "rxjs";
import {expect, vi} from 'vitest';

describe('CreateCandidateAttachmentComponent', () => {
    let component: CreateCandidateAttachmentComponent;
    let fixture: ComponentFixture<CreateCandidateAttachmentComponent>;
    let candidateAttachmentServiceSpy: MockedObject<CandidateAttachmentService>;
    let modalSpy: MockedObject<NgbActiveModal>;

    beforeEach(async () => {
        const candidateAttachmentServiceMock = {
            createAttachment: vi.fn().mockName("CandidateAttachmentService.createAttachment"),
            uploadAttachment: vi.fn().mockName("CandidateAttachmentService.uploadAttachment")
        };
        const modalMock = {
            close: vi.fn().mockName("NgbActiveModal.close")
        };

        await TestBed.configureTestingModule({
            declarations: [CreateCandidateAttachmentComponent],
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule],
            providers: [
                UntypedFormBuilder,
                { provide: CandidateAttachmentService, useValue: candidateAttachmentServiceMock },
                { provide: NgbActiveModal, useValue: modalMock },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        })
            .compileComponents();

        candidateAttachmentServiceSpy = TestBed.inject(CandidateAttachmentService) as MockedObject<CandidateAttachmentService>;
        modalSpy = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CreateCandidateAttachmentComponent);
        component = fixture.componentInstance;
        component.candidateId = 1;
        component.type = 'link';
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should save a link attachment', () => {
        // Arrange
        const mockAttachment: CandidateAttachmentRequest = {
            candidateId: 1,
            type: AttachmentType.link,
            name: 'Test Attachment',
            url: 'https://example.com',
            uploadType: UploadType.other
        };

        // @ts-expect-error
        candidateAttachmentServiceSpy.createAttachment.mockReturnValue(of(mockAttachment));
        // Fill the form
        component.form.controls['name'].setValue('Test Attachment');
        component.form.controls['url'].setValue('https://example.com');

        // Act
        component.save();

        expect(candidateAttachmentServiceSpy.createAttachment).toHaveBeenCalledWith(expect.objectContaining(mockAttachment));
        expect(modalSpy.close).toHaveBeenCalled();
        expect(component.error).toBeFalsy();
    });

    it('should handle save link attachment error', () => {
        // Arrange
        candidateAttachmentServiceSpy.createAttachment.mockReturnValue(throwError('Error saving attachment'));

        // Fill the form
        component.form.controls['name'].setValue('Test Attachment');
        component.form.controls['url'].setValue('https://example.com');

        // Act
        component.save();

        // Assert
        expect(candidateAttachmentServiceSpy.createAttachment).toHaveBeenCalled();
        expect(component.error).toBe('Error saving attachment');
        expect(modalSpy.close).not.toHaveBeenCalled();
    });

    it('should initialize attachments and form values', () => {
        component.candidateId = 99;
        component.type = 'file';

        component.ngOnInit();

        expect(component.attachments).toEqual([]);
        expect(component.form.value).toEqual({
            candidateId: 99,
            type: 'file',
            url: '',
            name: '',
            cv: false
        });
    });

    it('should close modal when cancel is called', () => {
        component.cancel();

        expect(modalSpy.close).toHaveBeenCalled();
    });

    it('should close modal when close is called', () => {
        component.close();

        expect(modalSpy.close).toHaveBeenCalled();
    });

    it('should upload multiple files successfully', () => {
        const firstFile = new File(['one'], 'one.txt');
        const secondFile = new File(['two'], 'two.txt');

        const firstAttachment = {
            id: 1,
            name: 'one.txt'
        } as any;

        const secondAttachment = {
            id: 2,
            name: 'two.txt'
        } as any;

        component.form.controls['cv'].setValue(true);

        candidateAttachmentServiceSpy.uploadAttachment.mockReturnValueOnce(of(firstAttachment)).mockReturnValueOnce(of(secondAttachment));

        component.startServerUpload([firstFile, secondFile]);

        expect(component.error).toBeNull();
        expect(component.uploading).toBe(false);
        expect(component.attachments).toEqual([
            firstAttachment,
            secondAttachment
        ]);

        expect(vi.mocked(candidateAttachmentServiceSpy.uploadAttachment).mock.calls.length).toBe(2);

        expect(vi.mocked(candidateAttachmentServiceSpy.uploadAttachment).mock.calls[0][0]).toBe(component.candidateId);
        expect(vi.mocked(candidateAttachmentServiceSpy.uploadAttachment).mock.calls[0][1]).toBe(true);
        expect(vi.mocked(candidateAttachmentServiceSpy.uploadAttachment).mock.calls[0][2]).toEqual(expect.any(FormData));

        expect(vi.mocked(candidateAttachmentServiceSpy.uploadAttachment).mock.calls[1][0]).toBe(component.candidateId);
        expect(vi.mocked(candidateAttachmentServiceSpy.uploadAttachment).mock.calls[1][1]).toBe(true);
        expect(vi.mocked(candidateAttachmentServiceSpy.uploadAttachment).mock.calls[1][2]).toEqual(expect.any(FormData));
    });

    it('should handle upload failure', () => {
        const file = new File(['one'], 'one.txt');
        const error = 'upload failed';

        component.form.controls['cv'].setValue(false);

        candidateAttachmentServiceSpy.uploadAttachment.mockReturnValue(throwError(error));

        component.startServerUpload([file]);

        expect(component.error).toBe(error);
        expect(component.uploading).toBe(false);
        expect(component.attachments).toEqual([]);
    });

    it('should clear existing attachments before uploading', () => {
        const file = new File(['one'], 'one.txt');
        const uploadedAttachment = {
            id: 1,
            name: 'one.txt'
        } as any;

        component.attachments = [
            {
                id: 999,
                name: 'old.txt'
            } as any
        ];

        candidateAttachmentServiceSpy.uploadAttachment.mockReturnValue(of(uploadedAttachment));

        component.startServerUpload([file]);

        expect(component.attachments).toEqual([
            uploadedAttachment
        ]);
    });

    it('should set an error from the file upload component', () => {
        component.onError('Invalid file');

        expect(component.error).toBe('Invalid file');
    });

});
