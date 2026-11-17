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
import {FileUploadComponent} from "./file-upload.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateAttachmentService} from "../../../services/candidate-attachment.service";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {NgSelectModule} from "@ng-select/ng-select";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {expect, vi} from 'vitest';

describe('FileUploadComponent', () => {
    let component: FileUploadComponent;
    let fixture: ComponentFixture<FileUploadComponent>;
    let candidateAttachmentService: MockedObject<CandidateAttachmentService>;

    beforeEach(async () => {
        const candidateAttachmentServiceSpy = {
            getMaxUploadFileSize: vi.fn().mockName("CandidateAttachmentService.getMaxUploadFileSize")
        };

        await TestBed.configureTestingModule({
            declarations: [FileUploadComponent],
            imports: [FormsModule, NgbModule, NgSelectModule],
            providers: [
                { provide: CandidateAttachmentService, useValue: candidateAttachmentServiceSpy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        candidateAttachmentService = TestBed.inject(CandidateAttachmentService) as MockedObject<CandidateAttachmentService>;
        candidateAttachmentService.getMaxUploadFileSize.mockReturnValue(1024 * 1024); // Mocking return value

        fixture = TestBed.createComponent(FileUploadComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should handle dragover event', () => {
        const event = new Event('dragover');
        const preventDefaultSpy = vi.spyOn(event, 'preventDefault').mockReturnValue(undefined);
        const stopPropagationSpy = vi.spyOn(event, 'stopPropagation').mockReturnValue(undefined);

        component.onDragOver(event);

        expect(preventDefaultSpy).toHaveBeenCalled();
        expect(stopPropagationSpy).toHaveBeenCalled();
        expect(component.hover).toBe(true);
    });

    it('should handle dragleave event', () => {
        const event = new Event('dragleave');
        const preventDefaultSpy = vi.spyOn(event, 'preventDefault').mockReturnValue(undefined);
        const stopPropagationSpy = vi.spyOn(event, 'stopPropagation').mockReturnValue(undefined);

        component.onDragLeave(event);

        expect(preventDefaultSpy).toHaveBeenCalled();
        expect(stopPropagationSpy).toHaveBeenCalled();
        expect(component.hover).toBe(false);
    });

    it('should handle file changed event with valid file', () => {
        const fileChangeEvent = {
            target: {
                files: [createMockFile('test.pdf')]
            }
        };
        const emitSpy = vi.spyOn(component.newFiles, 'emit').mockReturnValue(undefined);

        component.handleFileChanged(fileChangeEvent);

        expect(emitSpy).toHaveBeenCalledWith([fileChangeEvent.target.files[0]]);
    });

    it('should handle file changed event with invalid file', () => {
        const fileChangeEvent = {
            target: {
                files: [createMockFile('test.exe')]
            }
        };
        const emitSpy = vi.spyOn(component.error, 'emit').mockReturnValue(undefined);

        component.handleFileChanged(fileChangeEvent);

        expect(emitSpy).toHaveBeenCalled();
        expect(component.error.emit).toHaveBeenCalledWith(expect.stringMatching(/Unsupported file extension/));
    });

    it('should validate valid file', () => {
        const validFile = createMockFile('test.pdf');
        const isValid = component.validFile(validFile);

        expect(isValid).toBe(true);
    });

    it('should validate invalid file', () => {
        const invalidFile = createMockFile('test.exe');
        const isValid = component.validFile(invalidFile);

        expect(isValid).toBe(false);
    });

    // Helper function to create mock File object
    function createMockFile(name: string): File {
        return new File([''], name, { type: 'text/plain' });
    }
});
