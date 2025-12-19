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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';
import {of} from 'rxjs';
import {ViewJobSummaryComponent} from './view-job-summary.component';
import {JobService} from '../../../../../services/job.service';
import {Job} from '../../../../../model/job';
import {MockJob} from "../../../../../MockData/MockJob";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";

describe('ViewJobSummaryComponent', () => {
  let component: ViewJobSummaryComponent;
  let fixture: ComponentFixture<ViewJobSummaryComponent>;
  let jobServiceSpy: jasmine.SpyObj<JobService>;

  beforeEach(async () => {
    const jobServiceSpyObj = jasmine.createSpyObj('JobService', ['updateSummary']);

    await TestBed.configureTestingModule({
      declarations: [ViewJobSummaryComponent, AutosaveStatusComponent],
      imports: [ReactiveFormsModule, HttpClientModule],
      providers: [{ provide: JobService, useValue: jobServiceSpyObj }]
    }).compileComponents();

    jobServiceSpy = TestBed.inject(JobService) as jasmine.SpyObj<JobService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobSummaryComponent);
    component = fixture.componentInstance;
    component.job = { id: 1, jobSummary: 'Initial summary' } as Job;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should save changes to job summary correctly', () => {
    const newSummary = 'Updated summary';
    component.form.get('jobSummary').setValue(newSummary);
    jobServiceSpy.updateSummary.and.returnValue(of(MockJob));

    component.doSave(component.form.get('jobSummary'));

    expect(jobServiceSpy.updateSummary).toHaveBeenCalledWith(component.job.id, newSummary);
    expect(component.error).toBeNull();
    expect(component.saving).toBeTruthy();
    expect(component.job.jobSummary).toEqual(newSummary);
    expect(component.jobSummaryControl.pristine).toBeTrue();
  });


  it('should cancel changes to job summary correctly', () => {
    const originalSummary = component.job.jobSummary;
    component.form.get('jobSummary').setValue('Updated summary');
    component.form.get('jobSummary').markAsDirty();

    component.cancelChanges();

    expect(component.jobSummaryControl.value).toEqual(originalSummary);
    expect(component.jobSummaryControl.pristine).toBeTrue();
  });
});
