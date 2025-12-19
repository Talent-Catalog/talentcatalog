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

import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ViewJobUploadsComponent} from './view-job-uploads.component';
import {JobService} from '../../../../../services/job.service';
import {of, throwError} from 'rxjs';
import {MockJob} from "../../../../../MockData/MockJob";
import {FileSelectorComponent} from "../../../../util/file-selector/file-selector.component";

describe('ViewJobUploadsComponent', () => {
  let component: ViewJobUploadsComponent;
  let fixture: ComponentFixture<ViewJobUploadsComponent>;
  let mockJobService: jasmine.SpyObj<JobService>;
  let mockModalService: jasmine.SpyObj<NgbModal>;

  beforeEach(async () => {
    mockJobService = jasmine.createSpyObj('JobService', ['updateJobLink', 'uploadJobDoc']);
    mockModalService = jasmine.createSpyObj('NgbModal', ['open']);
    // Create a mock NgbModalRef object
    const mockModalRef = jasmine.createSpyObj('NgbModalRef', ['componentInstance', 'close', 'dismiss']);
    // Configure the result property of the mock to return a Promise that resolves to an array of files
    mockModalRef.result = Promise.resolve([new File(['test'], 'test.txt', { type: 'text/plain' })]);
    // Configure the mockModalService.open method to return the mockModalRef
    mockModalService.open.and.returnValue(mockModalRef);
    await TestBed.configureTestingModule({
      declarations: [ ViewJobUploadsComponent,FileSelectorComponent ],
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
    expect(component).toBeTruthy()
  });
  it('should call uploadJobDoc method with correct arguments when uploadJD is called', fakeAsync(() => {
    const file = new File(['test'], 'test.txt', { type: 'text/plain' });
    spyOn(window, 'File').and.returnValue(file);
    const jobDocType = 'jd';
    const expectedFormData = new FormData();
    expectedFormData.append('file', file);

    component.uploadJD(); // Calling the public method

    expect(mockModalService.open).toHaveBeenCalled();
    const fileSelectorModal = mockModalService.open.calls.mostRecent().returnValue;
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
    spyOn(window, 'File').and.returnValue(file);
    spyOn(component.jobUpdated, 'emit');
    mockJobService.uploadJobDoc.and.returnValue(of(component.job));

    component.uploadJD();

    const fileSelectorModal = mockModalService.open.calls.mostRecent().returnValue;
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
    spyOn(window, 'File').and.returnValue(file);
    spyOn(component.jobUpdated, 'emit');
    const errorMessage = 'Error uploading document';
    mockJobService.uploadJobDoc.and.returnValue(throwError(errorMessage));

    component.uploadJD();

    tick();

    expect(component.error).toBe(errorMessage);
    expect(component.jobUpdated.emit).not.toHaveBeenCalled();
  }));
});
