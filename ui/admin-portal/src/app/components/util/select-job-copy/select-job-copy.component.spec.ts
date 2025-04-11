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

import {SelectJobCopyComponent} from "./select-job-copy.component";
import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from "@angular/core/testing";
import {JobService} from "../../../services/job.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {MockJob} from "../../../MockData/MockJob";
import {of, throwError} from "rxjs";
import {Job} from "../../../model/job";
import {By} from "@angular/platform-browser";

describe('SelectJobCopyComponent', () => {
  let component: SelectJobCopyComponent;
  let fixture: ComponentFixture<SelectJobCopyComponent>;
  let jobServiceSpy: jasmine.SpyObj<JobService>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    const jobService = jasmine.createSpyObj('JobService', ['search']);
    const activeModal = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [SelectJobCopyComponent],
      imports: [ReactiveFormsModule,FormsModule,NgSelectModule,HttpClientTestingModule],
      providers: [
        { provide: JobService, useValue: jobService },
        { provide: NgbActiveModal, useValue: activeModal },
        UntypedFormBuilder,
      ],
    }).compileComponents();

    jobServiceSpy = TestBed.inject(JobService) as jasmine.SpyObj<JobService>;
    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectJobCopyComponent);
    component = fixture.componentInstance;
    jobServiceSpy.search.and.returnValue(of());
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize component with loading state', () => {
    expect(component.loading).toBeTrue(); // Initially, loading should be true
  });

  it('should fetch jobs on component initialization', () => {
    const mockJobs: Job[] = [MockJob];
    jobServiceSpy.search.and.returnValue(of(mockJobs));

    component.ngOnInit();

    expect(component.loading).toBeFalse(); // Loading should be false after fetching
    expect(component.jobsToCopy).toEqual(mockJobs); // Jobs should be populated
  });

  it('should handle error when fetching jobs', () => {
    const errorMessage = 'Error fetching jobs';
    jobServiceSpy.search.and.returnValue(throwError(errorMessage));

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
