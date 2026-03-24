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

import {Component, EventEmitter, Input, Output, forwardRef} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR, UntypedFormBuilder} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {CandidateAttachmentsComponent} from './candidate-attachments.component';
import {AttachmentType, CandidateAttachment} from '../../../model/candidate-attachment';
import {CandidateAttachmentService} from '../../../services/candidate-attachment.service';
import {CandidateService} from '../../../services/candidate.service';
import {UserService} from '../../../services/user.service';

@Component({
  selector: 'tc-button',
  template: '<ng-content></ng-content>'
})
class TcButtonStubComponent {
  @Input() color?: string;
  @Input() disabled?: boolean;
}

@Component({
  selector: 'tc-label',
  template: '<ng-content></ng-content>'
})
class TcLabelStubComponent {
  @Input() for?: string;
}

@Component({
  selector: 'tc-description-list',
  template: '<ng-content></ng-content>'
})
class TcDescriptionListStubComponent {
  @Input() direction?: string;
  @Input() compact?: boolean;
  @Input() size?: string;
}

@Component({
  selector: 'tc-description-item',
  template: '<ng-content></ng-content>'
})
class TcDescriptionItemStubComponent {
  @Input() label?: string;
}

@Component({
  selector: 'tc-input',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => TcInputStubComponent),
    multi: true
  }]
})
class TcInputStubComponent implements ControlValueAccessor {
  @Input() type?: string;
  @Input() id?: string;
  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

@Component({
  selector: 'app-file-upload',
  template: ''
})
class FileUploadStubComponent {
  @Input() uploading?: boolean;
  @Output() uploadStarted = new EventEmitter<any>();
}

@Component({
  selector: 'app-error',
  template: ''
})
class ErrorStubComponent {
  @Input() error: unknown;
}

function makeAttachment(overrides: Partial<CandidateAttachment> = {}): CandidateAttachment {
  return {
    id: 1,
    name: 'Resume.pdf',
    url: 'https://example.com/resume.pdf',
    fileType: 'pdf',
    type: AttachmentType.file,
    migrated: false,
    cv: false,
    createdBy: {
      id: 10,
      firstName: 'Amina',
      lastName: 'Saleh'
    } as any,
    createdDate: '2024-01-01T00:00:00Z',
    uploadType: null as any,
    ...overrides
  };
}

describe('FileUploadsComponent', () => {
  let component: CandidateAttachmentsComponent;
  let fixture: ComponentFixture<CandidateAttachmentsComponent>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let candidateAttachmentServiceSpy: jasmine.SpyObj<CandidateAttachmentService>;
  let userServiceSpy: jasmine.SpyObj<UserService>;

  async function configureAndCreate(options?: {
    preview?: boolean;
    cv?: boolean;
    attachments?: CandidateAttachment[];
    attachmentsError?: unknown;
  }) {
    candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['getCandidatePersonal']);
    candidateAttachmentServiceSpy = jasmine.createSpyObj('CandidateAttachmentService', [
      'listCandidateAttachments',
      'deleteAttachment',
      'downloadAttachment',
      'uploadAttachment',
      'updateAttachment'
    ]);
    userServiceSpy = jasmine.createSpyObj('UserService', ['getMyUser']);

    candidateServiceSpy.getCandidatePersonal.and.returnValue(of({candidateNumber: 'TC-123'} as any));
    userServiceSpy.getMyUser.and.returnValue(of({id: 10, firstName: 'Amina', lastName: 'Saleh'} as any));
    candidateAttachmentServiceSpy.listCandidateAttachments.and.returnValue(
      options?.attachmentsError ? throwError(options.attachmentsError) : of(options?.attachments ?? [makeAttachment()])
    );
    candidateAttachmentServiceSpy.deleteAttachment.and.returnValue(of({} as any));
    candidateAttachmentServiceSpy.downloadAttachment.and.returnValue(of(void 0));
    candidateAttachmentServiceSpy.uploadAttachment.and.returnValue(of(makeAttachment()));
    candidateAttachmentServiceSpy.updateAttachment.and.returnValue(of(makeAttachment()));

    await TestBed.configureTestingModule({
      declarations: [
        CandidateAttachmentsComponent,
        TcButtonStubComponent,
        TcLabelStubComponent,
        TcDescriptionListStubComponent,
        TcDescriptionItemStubComponent,
        TcInputStubComponent,
        FileUploadStubComponent,
        ErrorStubComponent
      ],
      imports: [FormsModule, TranslateModule.forRoot()],
      providers: [
        UntypedFormBuilder,
        {provide: CandidateService, useValue: candidateServiceSpy},
        {provide: CandidateAttachmentService, useValue: candidateAttachmentServiceSpy},
        {provide: UserService, useValue: userServiceSpy}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateAttachmentsComponent);
    component = fixture.componentInstance;
    component.preview = options?.preview ?? false;
    component.cv = options?.cv ?? false;

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();
    expect(component).toBeTruthy();
  });

  describe('template tc components', () => {
    it('should render the file upload and attachment action buttons in edit mode', async () => {
      await configureAndCreate();

      expect(fixture.debugElement.query(By.directive(FileUploadStubComponent))).toBeTruthy();

      const buttons = fixture.debugElement.queryAll(By.directive(TcButtonStubComponent));
      expect(buttons.length).toBeGreaterThan(1);
      expect(buttons[0].componentInstance.color).toBe('info');
      expect(buttons[1].componentInstance.color).toBe('error');
    });

    it('should render description items for attachment metadata', async () => {
      await configureAndCreate();

      const labels = fixture.debugElement
        .queryAll(By.directive(TcDescriptionItemStubComponent))
        .map(debugEl => debugEl.componentInstance.label);

      expect(labels).toContain('REGISTRATION.ATTACHMENTS.LABEL.NAME');
      expect(labels).toContain('REGISTRATION.ATTACHMENTS.LABEL.CREATEDBY');
      expect(labels).toContain('REGISTRATION.ATTACHMENTS.LABEL.CREATEDDATE');
    });

    it('should render tc-label and tc-input when editing an attachment name', async () => {
      await configureAndCreate();

      component.editTarget = component.attachments[0];
      fixture.detectChanges();

      expect(fixture.debugElement.query(By.directive(TcLabelStubComponent))).toBeTruthy();
      expect(fixture.debugElement.query(By.directive(TcInputStubComponent))).toBeTruthy();
    });

    it('should hide the file upload in preview mode', async () => {
      await configureAndCreate({preview: true});

      expect(fixture.debugElement.query(By.directive(FileUploadStubComponent))).toBeNull();
    });
  });

  describe('behaviour', () => {
    beforeEach(async () => configureAndCreate());

    it('should toggle editTarget when editing an attachment', () => {
      const attachment = component.attachments[0];

      component.editCandidateAttachment(attachment);
      expect(component.editTarget).toBe(attachment);

      component.editCandidateAttachment(attachment);
      expect(component.editTarget).toBeNull();
    });

    it('should delete an attachment on success', () => {
      component.deleteAttachment(component.attachments[0]);

      expect(candidateAttachmentServiceSpy.deleteAttachment).toHaveBeenCalledWith(1);
      expect(component.attachments.length).toBe(0);
    });

    it('should update an attachment name on success', () => {
      component.editTarget = component.attachments[0];
      component.attachments[0].name = 'Updated.pdf';

      component.updateAttachmentName(component.attachments[0], 0);

      expect(candidateAttachmentServiceSpy.updateAttachment).toHaveBeenCalledWith(1, {name: 'Updated.pdf'});
      expect(component.editTarget).toBeNull();
    });
  });

  describe('error paths', () => {
    it('should set error when attachments fail to load', async () => {
      const serverError = {status: 500};
      await configureAndCreate({attachmentsError: serverError});

      expect(component.error).toEqual(serverError);
    });
  });
});
