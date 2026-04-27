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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EventEmitter } from '@angular/core';
import { JobUploadTabComponent } from './job-upload-tab.component';
import { Job } from '../../../../../model/job';
import { JobPrepItem } from '../../../../../model/job-prep-item';
import {MockJob} from "../../../../../MockData/MockJob";
import {ViewJobUploadsComponent} from "../../uploads/view-job-uploads/view-job-uploads.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('JobUploadTabComponent', () => {
  let component: JobUploadTabComponent;
  let fixture: ComponentFixture<JobUploadTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports:[HttpClientTestingModule],
      declarations: [ JobUploadTabComponent ,ViewJobUploadsComponent]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobUploadTabComponent);
    component = fixture.componentInstance;
    component.job = MockJob;
    fixture.detectChanges();
  });

  it('should emit jobUpdated event when onJobUpdated is called', () => {
    const job: Job = {...MockJob};
    spyOn(component.jobUpdated, 'emit');

    component.onJobUpdated(job);

    expect(component.jobUpdated.emit).toHaveBeenCalledWith(job);
  });

});
