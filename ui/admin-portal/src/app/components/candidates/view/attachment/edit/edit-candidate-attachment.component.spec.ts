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
import {EditCandidateAttachmentComponent} from "./edit-candidate-attachment.component";
import {CandidateAttachmentService, UpdateCandidateAttachmentRequest} from "../../../../../services/candidate-attachment.service";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder, UntypedFormControl, Validators} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {of, throwError} from "rxjs";
import {AttachmentType} from "../../../../../model/candidate-attachment";
import {expect, vi} from 'vitest';

describe('EditCandidateAttachmentComponent', () => {
    let component: EditCandidateAttachmentComponent;
    let fixture: ComponentFixture<EditCandidateAttachmentComponent>;
    let candidateAttachmentServiceSpy: MockedObject<CandidateAttachmentService>;
    let fb: UntypedFormBuilder;
    const mockAttachment = new MockCandidate().candidateAttachments[0];
    beforeEach(async () => {

        const candidateAttachmentServiceMock = {
            updateAttachment: vi.fn().mockName("CandidateAttachmentService.updateAttachment")
        };

        await TestBed.configureTestingModule({
            declarations: [EditCandidateAttachmentComponent],
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule],
            providers: [
                UntypedFormBuilder,
                NgbActiveModal,
                { provide: CandidateAttachmentService, useValue: candidateAttachmentServiceMock },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(EditCandidateAttachmentComponent);
        component = fixture.componentInstance;
        fb = TestBed.inject(UntypedFormBuilder) as MockedObject<UntypedFormBuilder>;
        candidateAttachmentServiceSpy = TestBed.inject(CandidateAttachmentService) as MockedObject<CandidateAttachmentService>;
        // Initialize the form with mock data
        mockAttachment.type = AttachmentType.link;
        component.attachment = mockAttachment;
        component.form = fb.group({
            id: [mockAttachment.id],
            name: [mockAttachment.name, Validators.required],
            url: [mockAttachment.url]
        });

        if (mockAttachment.type === 'link') {
            component.form.addControl('url', new UntypedFormControl(mockAttachment.url, Validators.required));
        }
        fixture.detectChanges();
    });

    it('should ', () => {
        expect(component).toBeTruthy();
    });

    it('should create and initialize form with attachment data', () => {
        expect(component).toBeTruthy();
        expect(component.form).toBeDefined();
        expect(component.form.controls['name'].value).toBe(mockAttachment.name);
        expect(component.form.controls['url'].value).toBe(mockAttachment.url);
    });

    it('should save the attachment', () => {
        const mockResponse = { ...mockAttachment, name: 'Updated Attachment' };
        candidateAttachmentServiceSpy.updateAttachment.mockReturnValue(of(mockResponse));

        component.form.controls['name'].setValue('Updated Attachment');
        component.form.controls['url'].setValue('https://updated.com');

        component.save();

        expect(candidateAttachmentServiceSpy.updateAttachment).toHaveBeenCalledWith(mockAttachment.id, expect.objectContaining<UpdateCandidateAttachmentRequest>({
            name: 'Updated Attachment',
            url: 'https://updated.com'
        }));
        expect(component.loading).toBe(true);
    });

    it('should handle error during save', () => {
        const mockError = 'Update failed';
        candidateAttachmentServiceSpy.updateAttachment.mockReturnValue(throwError(mockError));

        component.form.controls['name'].setValue('Updated Attachment');
        component.form.controls['url'].setValue('https://updated.com');

        component.save();

        expect(candidateAttachmentServiceSpy.updateAttachment).toHaveBeenCalledWith(mockAttachment.id, expect.objectContaining<UpdateCandidateAttachmentRequest>({
            name: 'Updated Attachment',
            url: 'https://updated.com'
        }));
        expect(component.loading).toBe(true);
        expect(component.error).toBe(mockError);
    });
});
