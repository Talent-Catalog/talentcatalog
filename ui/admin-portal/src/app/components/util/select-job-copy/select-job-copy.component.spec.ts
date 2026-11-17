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
import {SelectJobCopyComponent} from "./select-job-copy.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {JobService} from "../../../services/job.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {MockJob} from "../../../MockData/MockJob";
import {of, throwError} from "rxjs";
import {Job} from "../../../model/job";
import {expect, vi} from 'vitest';

describe('SelectJobCopyComponent', () => {
    let component: SelectJobCopyComponent;
    let fixture: ComponentFixture<SelectJobCopyComponent>;
    let jobServiceSpy: MockedObject<JobService>;
    let activeModalSpy: MockedObject<NgbActiveModal>;

    beforeEach(async () => {
        const jobService = {
            search: vi.fn().mockName("JobService.search")
        };
        const activeModal = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };

        await TestBed.configureTestingModule({
            declarations: [SelectJobCopyComponent],
            imports: [ReactiveFormsModule, FormsModule, NgSelectModule],
            providers: [
                { provide: JobService, useValue: jobService },
                { provide: NgbActiveModal, useValue: activeModal },
                UntypedFormBuilder,
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting(),
            ],
        }).compileComponents();

        jobServiceSpy = TestBed.inject(JobService) as MockedObject<JobService>;
        activeModalSpy = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SelectJobCopyComponent);
        component = fixture.componentInstance;
        jobServiceSpy.search.mockReturnValue(of());
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize component with loading state', () => {
        expect(component.loading).toBe(true); // Initially, loading should be true
    });

    it('should fetch jobs on component initialization', () => {
        const mockJobs: Job[] = [MockJob];
        jobServiceSpy.search.mockReturnValue(of(mockJobs));

        component.ngOnInit();

        expect(component.loading).toBe(false); // Loading should be false after fetching
        expect(component.jobsToCopy).toEqual(mockJobs); // Jobs should be populated
    });

    it('should handle error when fetching jobs', () => {
        const errorMessage = 'Error fetching jobs';
        jobServiceSpy.search.mockReturnValue(throwError(errorMessage));

        component.ngOnInit();

        expect(component.error).toEqual(errorMessage); // Error message should be set
    });

    it('should close modal with selected job id on select', () => {
        const selectedJobId = 1;
        component.form.patchValue({ jobToCopyId: selectedJobId });

        component.onSelect();

        expect(activeModalSpy.close).toHaveBeenCalledWith(selectedJobId); // Modal should be closed with selected job id
    });

    it('should dismiss modal on cancel', () => {
        component.onCancel();

        expect(activeModalSpy.dismiss).toHaveBeenCalled(); // Modal should be dismissed
    });

});
