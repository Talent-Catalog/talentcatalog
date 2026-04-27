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

import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from '@angular/core/testing';
import {ActivatedRoute, Router} from '@angular/router';
import {of, throwError} from 'rxjs';
import {ViewJobFromUrlComponent} from './view-job-from-url.component';
import {JobService} from '../../../../services/job.service';
import {MockJob} from "../../../../MockData/MockJob";
import {ViewJobComponent} from "../view-job/view-job.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgbNavModule, NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";
import {ReactiveFormsModule} from "@angular/forms";
import {CommonModule} from "@angular/common";
import {JobGeneralTabComponent} from "../tab/job-general-tab/job-general-tab.component";
import {ViewJobInfoComponent} from "../info/view-job-info/view-job-info.component";
import {ChatReadStatusComponent} from "../../../chat/chat-read-status/chat-read-status.component";
import {ViewJobSummaryComponent} from "../summary/view-job-summary/view-job-summary.component";
import {RouterLinkStubDirective} from "../../../login/login.component.spec";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

describe('ViewJobFromUrlComponent', () => {
  let component: ViewJobFromUrlComponent;
  let fixture: ComponentFixture<ViewJobFromUrlComponent>;
  let mockJobService: jasmine.SpyObj<JobService>;
  const mockActivatedRoute = {
    paramMap: of({
      get: (key: string) => '1' // Mock job ID
    })
  };

  beforeEach(waitForAsync(() => {
    mockJobService = jasmine.createSpyObj('JobService', ['get']);
    mockJobService.get.and.returnValue(of(MockJob));
    TestBed.configureTestingModule({
      imports:[HttpClientTestingModule,NgbNavModule,ReactiveFormsModule,CommonModule,NgbTooltipModule],
      declarations: [ViewJobFromUrlComponent,AutosaveStatusComponent,ViewJobComponent,JobGeneralTabComponent,ViewJobInfoComponent,ChatReadStatusComponent,ViewJobSummaryComponent,RouterLinkStubDirective],
      providers: [
        { provide: Router, useClass: class { navigate = jasmine.createSpy('navigate'); events = of(); } }, // Mock Router
        { provide: JobService, useValue: mockJobService },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewJobFromUrlComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load job based on ID from server', fakeAsync(() => {
    // Arrange
    const job = MockJob;
    // Act
    component.ngOnInit();
    tick();
    expect(component.error).toBeNull();
    expect(mockJobService.get).toHaveBeenCalledWith(1); // Expecting to call get method with job ID 1
    expect(component.job).toEqual(job); // Expecting loaded job to match the mock job
    expect(component.loading).toBeFalse();
  }));
  //
  it('should handle error when loading job', () => {
    // Arrange
    const errorMessage = 'Error occurred while loading job';
    mockJobService.get.and.returnValue(throwError(errorMessage));
    // Act
    component.ngOnInit();
    // Assert
    expect(mockJobService.get).toHaveBeenCalledWith(1); // Expecting to call get method with job ID 1
    expect(component.job).toBeNull(); // Expecting job to be null due to error
    expect(component.error).toEqual(errorMessage); // Expecting error message to be set
    expect(component.loading).toBeFalse();
  });
});
