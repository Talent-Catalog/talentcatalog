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

import {FileSelectorComponent} from "./file-selector.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {FileUploadComponent} from "../file-upload/file-upload.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('FileSelectorComponent', () => {
  let component: FileSelectorComponent;
  let fixture: ComponentFixture<FileSelectorComponent>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;
  let modalMock: Partial<NgbActiveModal>;
  beforeEach(async () => {
    modalMock = {
      close: jasmine.createSpy('close')
    };
    const modalSpy = jasmine.createSpyObj('NgbModal', ['close']);
    await TestBed.configureTestingModule({
      imports:[HttpClientTestingModule],
      declarations: [FileSelectorComponent,FileUploadComponent],
      providers: [
        { provide: NgbActiveModal, useValue: modalMock },
        { provide: NgbModal, useValue: modalSpy }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FileSelectorComponent);
    component = fixture.componentInstance;
    modalServiceSpy = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.closeButtonLabel).toEqual('Close');
    expect(component.error).toBeUndefined();
    expect(component.instructions).toBeUndefined();
    expect(component.maxFiles).toEqual(0);
    expect(component.title).toEqual('Select files');
    expect(component.validExtensions).toEqual(['jpg', 'png', 'pdf', 'doc', 'docx', 'txt']);
    expect(component.selectedFiles).toEqual([]);
  });

  it('should add files and handle maximum file limit', () => {
    const files = [createMockFile('test.jpg'), createMockFile('document.pdf')];
    component.maxFiles = 1;

    component.addFiles(files);

    expect(component.error).toEqual('Only 1 file(s) can be selected.');
    expect(component.selectedFiles.length).toBe(0);
  });

  it('should close modal with selected files', () => {

    component.selectedFiles = [createMockFile('test.jpg')];
    component.close();

    expect(modalMock.close).toHaveBeenCalledWith([createMockFile('test.jpg')]);
  });

  it('should validate if a single file is selected', () => {
    component.selectedFiles = [createMockFile('test.jpg')];

    const isValid = component.isValid();

    expect(isValid).toBeTrue();
  });

  it('should handle file upload error', () => {
    const errorMessage = 'Invalid file format';
    component.onError(errorMessage);

    expect(component.error).toEqual(errorMessage);
  });

  // Helper function to create mock File object
  function createMockFile(name: string): File {
    return new File([''], name, { type: 'text/plain' });
  }
});
