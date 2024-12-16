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

import {FileUploadComponent} from "./file-upload.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateAttachmentService} from "../../../services/candidate-attachment.service";
import {FormsModule} from "@angular/forms";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('FileUploadComponent', () => {
  let component: FileUploadComponent;
  let fixture: ComponentFixture<FileUploadComponent>;
  let candidateAttachmentService: jasmine.SpyObj<CandidateAttachmentService>;

  beforeEach(async () => {
    const candidateAttachmentServiceSpy = jasmine.createSpyObj('CandidateAttachmentService', ['getMaxUploadFileSize']);

    await TestBed.configureTestingModule({
      declarations: [FileUploadComponent],
      imports: [HttpClientTestingModule,FormsModule, NgbModule, NgSelectModule],
      providers: [
        { provide: CandidateAttachmentService, useValue: candidateAttachmentServiceSpy }
      ]
    }).compileComponents();

    candidateAttachmentService = TestBed.inject(CandidateAttachmentService) as jasmine.SpyObj<CandidateAttachmentService>;
    candidateAttachmentService.getMaxUploadFileSize.and.returnValue(1024 * 1024); // Mocking return value

    fixture = TestBed.createComponent(FileUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should handle dragover event', () => {
    const event = new Event('dragover');
    const preventDefaultSpy = spyOn(event, 'preventDefault');
    const stopPropagationSpy = spyOn(event, 'stopPropagation');

    component.onDragOver(event);

    expect(preventDefaultSpy).toHaveBeenCalled();
    expect(stopPropagationSpy).toHaveBeenCalled();
    expect(component.hover).toBeTrue();
  });

  it('should handle dragleave event', () => {
    const event = new Event('dragleave');
    const preventDefaultSpy = spyOn(event, 'preventDefault');
    const stopPropagationSpy = spyOn(event, 'stopPropagation');

    component.onDragLeave(event);

    expect(preventDefaultSpy).toHaveBeenCalled();
    expect(stopPropagationSpy).toHaveBeenCalled();
    expect(component.hover).toBeFalse();
  });

  it('should handle file changed event with valid file', () => {
    const fileChangeEvent = {
      target: {
        files: [createMockFile('test.pdf')]
      }
    };
    const emitSpy = spyOn(component.newFiles, 'emit');

    component.handleFileChanged(fileChangeEvent);

    expect(emitSpy).toHaveBeenCalledWith([fileChangeEvent.target.files[0]]);
  });

  it('should handle file changed event with invalid file', () => {
    const fileChangeEvent = {
      target: {
        files: [createMockFile('test.exe')]
      }
    };
    const emitSpy = spyOn(component.error, 'emit');

    component.handleFileChanged(fileChangeEvent);

    expect(emitSpy).toHaveBeenCalled();
    expect(component.error.emit).toHaveBeenCalledWith(jasmine.stringMatching(/Unsupported file extension/));
  });

  it('should validate valid file', () => {
    const validFile = createMockFile('test.pdf');
    const isValid = component.validFile(validFile);

    expect(isValid).toBeTrue();
  });

  it('should validate invalid file', () => {
    const invalidFile = createMockFile('test.exe');
    const isValid = component.validFile(invalidFile);

    expect(isValid).toBeFalse();
  });

  // Helper function to create mock File object
  function createMockFile(name: string): File {
    return new File([''], name, { type: 'text/plain' });
  }
});
