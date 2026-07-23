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

import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

import {FileSelectorComponent} from './file-selector.component';

@Component({
  selector: 'app-file-upload',
  template: ''
})
class FileUploadStubComponent {
  @Input() validExtensions: string[];

  @Output() uploadStarted = new EventEmitter<{files: File[]}>();
  @Output() error = new EventEmitter<string>();
}

describe('FileSelectorComponent', () => {
  let component: FileSelectorComponent;
  let fixture: ComponentFixture<FileSelectorComponent>;
  let modalSpy: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    modalSpy = jasmine.createSpyObj<NgbActiveModal>(
      'NgbActiveModal',
      ['close']
    );

    await TestBed.configureTestingModule({
      declarations: [
        FileSelectorComponent,
        FileUploadStubComponent
      ],
      providers: [
        {
          provide: NgbActiveModal,
          useValue: modalSpy
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FileSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have the expected default values', () => {
    expect(component.title).toBe('Select files');
    expect(component.closeButtonLabel).toBe('Close');
    expect(component.maxFiles).toBe(0);
    expect(component.selectedFiles).toEqual([]);
    expect(component.validExtensions).toEqual([
      'jpg',
      'png',
      'pdf',
      'doc',
      'docx',
      'txt'
    ]);
  });

  it('should add selected files', () => {
    const files = [
      new File(['content'], 'document.pdf', {
        type: 'application/pdf'
      })
    ];

    component.error = 'Previous error';

    component.addFiles({files});

    expect(component.error).toBeNull();
    expect(component.selectedFiles).toEqual(files);
  });

  it('should show an error when selected files exceed maxFiles', () => {
    component.maxFiles = 1;

    const files = [
      new File(['one'], 'one.pdf'),
      new File(['two'], 'two.pdf')
    ];

    component.addFiles({files});

    expect(component.error).toBe('Only 1 file(s) can be selected.');
    expect(component.selectedFiles).toEqual([]);
  });

  it('should allow any number of files when maxFiles is zero', () => {
    component.maxFiles = 0;

    const files = [
      new File(['one'], 'one.pdf'),
      new File(['two'], 'two.pdf')
    ];

    component.addFiles({files});

    expect(component.error).toBeNull();
    expect(component.selectedFiles).toEqual(files);
  });

  it('should be valid when exactly one file is selected', () => {
    component.selectedFiles = [
      new File(['content'], 'document.pdf')
    ];

    expect(component.isValid()).toBeTrue();
  });

  it('should be invalid when no files are selected', () => {
    component.selectedFiles = [];

    expect(component.isValid()).toBeFalse();
  });

  it('should be invalid when more than one file is selected', () => {
    component.selectedFiles = [
      new File(['one'], 'one.pdf'),
      new File(['two'], 'two.pdf')
    ];

    expect(component.isValid()).toBeFalse();
  });

  it('should set an upload error', () => {
    component.onError('Invalid file type');

    expect(component.error).toBe('Invalid file type');
  });

  it('should close the modal without a result when cancelled', () => {
    component.cancel();

    expect(modalSpy.close).toHaveBeenCalledWith();
  });

  it('should close the modal with the selected files', () => {
    const files = [
      new File(['content'], 'document.pdf')
    ];

    component.selectedFiles = files;

    component.close();

    expect(modalSpy.close).toHaveBeenCalledWith(files);
  });
});
