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

import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {TranslateModule} from '@ngx-translate/core';

import {FileUploadComponent} from './file-upload.component';

@Component({
  selector: 'app-error',
  template: ''
})
class ErrorStubComponent {
  @Input() error?: unknown;
}

@Component({
  selector: 'tc-button',
  template: '<ng-content></ng-content>'
})
class TcButtonStubComponent {
  @Input() disabled?: boolean;
  @Input() class?: string;
  @Output() onClick = new EventEmitter<void>();
}

describe('FileUploadComponent', () => {
  let component: FileUploadComponent;
  let fixture: ComponentFixture<FileUploadComponent>;

  async function configureAndCreate(options?: {uploading?: boolean}) {
    await TestBed.configureTestingModule({
      declarations: [
        FileUploadComponent,
        ErrorStubComponent,
        TcButtonStubComponent
      ],
      imports: [TranslateModule.forRoot()],
      providers: [
        {provide: NgbModal, useValue: jasmine.createSpyObj('NgbModal', ['open'])}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FileUploadComponent);
    component = fixture.componentInstance;
    component.uploading = options?.uploading ?? false;

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();

    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should initialize the max upload size', async () => {
      await configureAndCreate();

      expect(component.maxUploadSize).toBe(10485790);
    });
  });

  describe('template tc components', () => {
    it('should render tc-button actions when not uploading', async () => {
      await configureAndCreate({uploading: false});
      const buttons = fixture.debugElement.queryAll(By.directive(TcButtonStubComponent));

      expect(buttons.length).toBe(3);
      buttons.forEach(debugEl => {
        expect(debugEl.componentInstance.disabled).toBeFalse();
      });
    });

    it('should render a single disabled uploading tc-button when uploading', async () => {
      await configureAndCreate({uploading: true});
      const buttons = fixture.debugElement.queryAll(By.directive(TcButtonStubComponent));

      expect(buttons.length).toBe(2);
      expect(buttons[0].componentInstance.disabled).toBeTrue();
      expect(buttons[1].componentInstance.disabled).toBeTrue();
      const text = (fixture.nativeElement as HTMLElement).textContent || '';
      expect(text).toContain('FORM.ATTACHMENT.LABEL.UPLOADING');
    });
  });

  describe('handleFileChanged', () => {
    beforeEach(async () => configureAndCreate());

    it('should emit uploadStarted when all files are valid', () => {
      const uploadStartedSpy = spyOn(component.uploadStarted, 'emit');
      const file = new File(['test'], 'resume.pdf', {type: 'application/pdf'});

      component.handleFileChanged({target: {files: [file]}}, 'file');

      expect(uploadStartedSpy).toHaveBeenCalledWith({
        files: [file],
        type: 'file'
      });
      expect(component.error).toBeNull();
    });

    it('should not emit uploadStarted when a file has no extension', () => {
      const uploadStartedSpy = spyOn(component.uploadStarted, 'emit');
      const file = new File(['test'], 'resume', {type: 'application/pdf'});

      component.handleFileChanged({target: {files: [file]}}, 'file');

      expect(uploadStartedSpy).not.toHaveBeenCalled();
      expect(component.error).toBe('No file extension found. Please rename and re-upload this file.');
    });

    it('should not emit uploadStarted when a file is too large', () => {
      const uploadStartedSpy = spyOn(component.uploadStarted, 'emit');
      const file = new File(['test'], 'resume.pdf', {type: 'application/pdf'});
      Object.defineProperty(file, 'size', {value: component.maxUploadSize + 1});

      component.handleFileChanged({target: {files: [file]}}, 'file');

      expect(uploadStartedSpy).not.toHaveBeenCalled();
      expect(component.error).toBe('Max file size exceeded, please reduce file size to under 10MB.');
    });
  });

  describe('validation helpers', () => {
    beforeEach(async () => configureAndCreate());

    it('should return true for a valid file name', () => {
      const file = new File(['test'], 'resume.pdf', {type: 'application/pdf'});

      expect(component.validFile(file)).toBeTrue();
    });

    it('should return false for a file name without an extension', () => {
      const file = new File(['test'], 'resume', {type: 'application/pdf'});

      expect(component.validFile(file)).toBeFalse();
    });

    it('should return true when file size is within the limit', () => {
      const file = new File(['test'], 'resume.pdf', {type: 'application/pdf'});

      expect(component.validFileSize(file)).toBeTrue();
    });

    it('should return false when file size exceeds the limit', () => {
      const file = new File(['test'], 'resume.pdf', {type: 'application/pdf'});
      Object.defineProperty(file, 'size', {value: component.maxUploadSize + 1});

      expect(component.validFileSize(file)).toBeFalse();
    });
  });

  describe('drag and drop', () => {
    beforeEach(async () => configureAndCreate());

    it('should set hover true on drag over', () => {
      const event = jasmine.createSpyObj('event', ['preventDefault', 'stopPropagation']);

      component.onDragOver(event);

      expect(component.hover).toBeTrue();
    });

    it('should set hover false on drag leave', () => {
      component.hover = true;
      const event = jasmine.createSpyObj('event', ['preventDefault', 'stopPropagation']);

      component.onDragLeave(event);

      expect(component.hover).toBeFalse();
    });

    it('should pass dropped files to handleFileChanged', () => {
      const file = new File(['test'], 'resume.pdf', {type: 'application/pdf'});
      const handleSpy = spyOn(component, 'handleFileChanged');
      const event = {
        preventDefault: jasmine.createSpy('preventDefault'),
        stopPropagation: jasmine.createSpy('stopPropagation'),
        dataTransfer: {files: [file]}
      };

      component.onDrop(event);

      expect(component.hover).toBeFalse();
      expect(handleSpy).toHaveBeenCalledWith({target: {files: [file]}}, 'file');
    });
  });
});
