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
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ViewJobUploadsComponent} from './view-job-uploads.component';
import {JobService} from '../../../../../services/job.service';
import {of, throwError} from 'rxjs';
import {MockJob} from "../../../../../MockData/MockJob";
import {FileSelectorComponent} from "../../../../util/file-selector/file-selector.component";
import {expect, vi} from 'vitest';

describe('ViewJobUploadsComponent', () => {
    let component: ViewJobUploadsComponent;
    let fixture: ComponentFixture<ViewJobUploadsComponent>;
    let mockJobService: MockedObject<JobService>;
    let mockModalService: MockedObject<NgbModal>;

    beforeEach(async () => {
        mockJobService = ({
            updateJobLink: vi.fn().mockName("JobService.updateJobLink"),
            uploadJobDoc: vi.fn().mockName("JobService.uploadJobDoc")
        } as unknown as typeof mockJobService);
        mockModalService = ({
            open: vi.fn().mockName("NgbModal.open")
        } as unknown as typeof mockModalService);
        // Create a mock NgbModalRef object
        const mockModalRef = {
            componentInstance: vi.fn().mockName("NgbModalRef.componentInstance"),
            close: vi.fn().mockName("NgbModalRef.close"),
            dismiss: vi.fn().mockName("NgbModalRef.dismiss")
        };
        // Configure the result property of the mock to return a Promise that resolves to an array of files
        Object.assign(mockModalRef, {result: Promise.resolve([new File(['test'], 'test.txt', { type: 'text/plain' })])});
        // Configure the mockModalService.open method to return the mockModalRef
        mockModalService.open.mockReturnValue((mockModalRef as unknown as ReturnType<typeof mockModalService.open>));
        await TestBed.configureTestingModule({
            declarations: [ViewJobUploadsComponent, FileSelectorComponent],
            providers: [
                { provide: JobService, useValue: mockJobService },
                { provide: NgbModal, useValue: mockModalService }
            ]
        })
            .compileComponents();

    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ViewJobUploadsComponent);
        component = fixture.componentInstance;
        component.job = MockJob;

        fixture.detectChanges();
    });
    it('should ', () => {
        expect(component).toBeTruthy();
    });
    it('should call uploadJobDoc method with correct arguments when uploadJD is called', fakeAsync(() => {
        const file = new File(['test'], 'test.txt', { type: 'text/plain' });
        vi.spyOn(window, 'File').mockReturnValue(file);
        const jobDocType = 'jd';
        const expectedFormData = new FormData();
        expectedFormData.append('file', file);

        component.uploadJD(); // Calling the public method

        expect(mockModalService.open).toHaveBeenCalled();
        // TODO: vitest-migration: Direct usage of mostRecent() is not supported. Please refactor to access .args directly or use vi.mocked(spy).mock.lastCall. See: https://vitest.dev/api/mocked.html#mock-lastcall
        const fileSelectorModal = mockModalService.open.mock.results.at(-1)?.value;
        expect(fileSelectorModal.componentInstance.maxFiles).toBe(1);
        expect(fileSelectorModal.componentInstance.closeButtonLabel).toBe('Upload');
        expect(fileSelectorModal.componentInstance.title).toBe('Select file containing the ' + jobDocType);

        fileSelectorModal.result.then((files: File[]) => {
            expect(files[0].name).toEqual('test.txt');
        });
        tick();
        expect(mockJobService.uploadJobDoc).toHaveBeenCalledWith(component.job.id, jobDocType, expectedFormData);
    }));

    it('should emit jobUpdated event when uploading a job document is successful', fakeAsync(() => {
        const file = new File(['test'], 'test.txt', { type: 'text/plain' });
        const jobDocType = 'jd';
        vi.spyOn(window, 'File').mockReturnValue(file);
        vi.spyOn(component.jobUpdated, 'emit').mockReturnValue(undefined);
        mockJobService.uploadJobDoc.mockReturnValue(of(component.job));

        component.uploadJD();

        // TODO: vitest-migration: Direct usage of mostRecent() is not supported. Please refactor to access .args directly or use vi.mocked(spy).mock.lastCall. See: https://vitest.dev/api/mocked.html#mock-lastcall
        const fileSelectorModal = mockModalService.open.mock.results.at(-1)?.value;
        fileSelectorModal.result.then((files: File[]) => {
            // Assertion: Ensure that the correct number of files is received
            expect(files.length).toBeGreaterThan(0);
        });
        tick();

        expect(component.jobUpdated.emit).toHaveBeenCalledWith(component.job);
    }));

    it('should handle error when uploading a job document fails', fakeAsync(() => {
        const file = new File(['test'], 'test.txt', { type: 'text/plain' });
        const jobDocType = 'jd';
        vi.spyOn(window, 'File').mockReturnValue(file);
        vi.spyOn(component.jobUpdated, 'emit').mockReturnValue(undefined);
        const errorMessage = 'Error uploading document';
        mockJobService.uploadJobDoc.mockReturnValue(throwError(errorMessage));

        component.uploadJD();

        tick();

        expect(component.error).toBe(errorMessage);
        expect(component.jobUpdated.emit).not.toHaveBeenCalled();
    }));
});
