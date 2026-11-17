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
import {JoblinkComponent} from "./joblink.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {JobService} from "../../../services/job.service";
import {FormsModule} from "@angular/forms";
import {NgbTypeaheadModule, NgbTypeaheadSelectItemEvent} from "@ng-bootstrap/ng-bootstrap";
import {of} from "rxjs";
import {MockJob} from "../../../MockData/MockJob";
import {SearchResults} from "../../../model/search-results";
import {Job} from "../../../model/job";
import {expect, vi} from 'vitest';

describe('JoblinkComponent', () => {
    let component: JoblinkComponent;
    let fixture: ComponentFixture<JoblinkComponent>;
    let jobService: MockedObject<JobService>;
    const searchResults: SearchResults<Job> = {
        first: false,
        last: false,
        number: 0,
        size: 0,
        totalPages: 0,
        totalElements: 1, content: [MockJob]
    };
    beforeEach(async () => {
        const jobServiceSpy = {
            searchPaged: vi.fn().mockName("JobService.searchPaged"),
            get: vi.fn().mockName("JobService.get")
        };

        await TestBed.configureTestingModule({
            imports: [FormsModule, NgbTypeaheadModule],
            declarations: [JoblinkComponent],
            providers: [
                { provide: JobService, useValue: jobServiceSpy }
            ]
        })
            .compileComponents();

        jobService = TestBed.inject(JobService) as MockedObject<JobService>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(JoblinkComponent);
        component = fixture.componentInstance;
        jobService.get.mockReturnValue(of(MockJob));
        jobService.searchPaged.mockReturnValue(of(searchResults));
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize correctly', () => {
        component.jobId = 1;
        component.currentJobName = 'XYZ';

        component.ngOnChanges({});

        expect(component.searchHeading).toBe('Change job association');
    });

    it('should emit job selection correctly', async () => {
        const job = { name: 'Developer', id: 1 };
        component.jobSelection.subscribe(selectedJob => {
            expect(selectedJob).toEqual(job);
            ;
        });

        component.selectSearchResult({ item: job } as NgbTypeaheadSelectItemEvent);
    });
});
