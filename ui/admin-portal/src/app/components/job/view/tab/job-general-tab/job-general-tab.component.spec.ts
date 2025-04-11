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
import {JobGeneralTabComponent} from './job-general-tab.component';
import {MockJob} from "../../../../../MockData/MockJob";
import {ViewJobInfoComponent} from "../../info/view-job-info/view-job-info.component";
import {RouterLinkStubDirective} from "../../../../login/login.component.spec";
import {Job} from "../../../../../model/job";
import {AuthorizationService} from "../../../../../services/authorization.service";

describe('JobGeneralTabComponent', () => {
  let component: JobGeneralTabComponent;
  let fixture: ComponentFixture<JobGeneralTabComponent>;

  beforeEach(async () => {
    let authServiceSpy =
      jasmine.createSpyObj('AuthorizationService', ['canSeeJobDetails']);
    authServiceSpy.canSeeJobDetails.and.returnValue(true);

    await TestBed.configureTestingModule({
      declarations: [JobGeneralTabComponent,ViewJobInfoComponent,RouterLinkStubDirective],
      providers: [
        { provide: AuthorizationService, useValue: authServiceSpy },
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobGeneralTabComponent);
    component = fixture.componentInstance;
    fixture.componentInstance.job = MockJob;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit jobUpdated event when job is updated', () => {
    // Create a shallow copy of MockJob
    const updatedJob: Job = { ...MockJob };

    // Modify the properties of updatedJob as needed
    updatedJob.name = 'Updated Name';
    updatedJob.jobSummary = 'Updated Job Summary';

    spyOn(component.jobUpdated, 'emit');

    component.onJobUpdated(updatedJob);
    // Check if the jobUpdated event is emitted with the correct argument
    expect(component.jobUpdated.emit).toHaveBeenCalledWith(updatedJob);
  });
});
