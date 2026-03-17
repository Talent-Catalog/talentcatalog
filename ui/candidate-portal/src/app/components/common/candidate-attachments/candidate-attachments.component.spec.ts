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

import {Component, EventEmitter, forwardRef, Input, NO_ERRORS_SCHEMA, Output} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {CandidateAttachmentsComponent} from './candidate-attachments.component';
import {AttachmentType, CandidateAttachment, UploadType} from '../../../model/candidate-attachment';
import {CandidateAttachmentService} from '../../../services/candidate-attachment.service';
import {CandidateService} from '../../../services/candidate.service';
import {UserService} from '../../../services/user.service';
import {User} from '../../../model/user';

@Component({selector: 'app-file-upload', template: ''})
class FileUploadStubComponent {
  @Input() uploading?: boolean;
  @Output() uploadStarted = new EventEmitter<{files: File[]; type: string}>();
}

@Component({selector: 'app-error', template: ''})
class ErrorStubComponent {
  @Input() error?: unknown;
}

@Component({selector: 'tc-button', template: '<ng-content></ng-content>'})
class TcButtonStubComponent {
  @Input() color?: string;
  @Input() disabled?: boolean;
  @Input() class?: string;
  @Output() onClick = new EventEmitter<void>();
}

@Component({
  selector: 'tc-input',
  template: '',
  providers: [{provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => TcInputStubComponent), multi: true}]
})
class TcInputStubComponent implements ControlValueAccessor {
  @Input() id?: string;
  @Input() type?: string;
  @Input() placeholder?: string;
  @Input() ngModel?: string;
  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

@Component({selector: 'tc-label', template: '<ng-content></ng-content>'})
class TcLabelStubComponent {
  @Input() for?: string;
}

function makeUser(id: number, firstName: string, lastName: string): User {
  return {id, firstName, lastName} as User;
}

function makeAttachment(id: number, name: string, overrides: Partial<CandidateAttachment> = {}): CandidateAttachment {
  return {
    id,
    name,
    url: `https://files.example/${name}`,
    fileType: 'pdf',
    type: AttachmentType.file,
    migrated: false,
    cv: false,
    createdBy: makeUser(1, 'Jane', 'Admin'),
    createdDate: '2024-01-01T00:00:00Z',
    uploadType: UploadType.other,
    ...overrides
  };
}

describe('CandidateAttachmentsComponent', () => {
  let component: CandidateAttachmentsComponent;
  let fixture: ComponentFixture<CandidateAttachmentsComponent>;

  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let candidateAttachmentServiceSpy: jasmine.SpyObj<CandidateAttachmentService>;
  let userServiceSpy: jasmine.SpyObj<UserService>;

  async function configureAndCreate(options?: {
    attachments?: CandidateAttachment[];
    preview?: boolean;
    cv?: boolean;
    candidateError?: unknown;
    userError?: unknown;
    attachmentsError?: unknown;
    deleteError?: unknown;
    updateError?: unknown;
    uploadError?: unknown;
    downloadError?: unknown;
  }) {
    candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['getCandidatePersonal']);
    candidateAttachmentServiceSpy = jasmine.createSpyObj('CandidateAttachmentService', [
      'listCandidateAttachments',
      'deleteAttachment',
      'uploadAttachment',
      'downloadAttachment',
      'updateAttachment'
    ]);
    userServiceSpy = jasmine.createSpyObj('UserService', ['getMyUser']);

    // Default: one CV file, one non-CV file, one Google Drive file (non-CV)
    const attachments = options?.attachments ?? [
      makeAttachment(1, 'resume.pdf', {cv: true}),
      makeAttachment(2, 'certificate.pdf', {cv: false}),
      makeAttachment(3, 'drive-file', {type: AttachmentType.googlefile, cv: false})
    ];

    candidateServiceSpy.getCandidatePersonal.and.returnValue(
      options?.candidateError
        ? throwError(options.candidateError)
        : of({candidateNumber: 'C123'} as any)
    );

    userServiceSpy.getMyUser.and.returnValue(
      options?.userError
        ? throwError(options.userError)
        : of(makeUser(1, 'Jane', 'Admin'))
    );

    candidateAttachmentServiceSpy.listCandidateAttachments.and.returnValue(
      options?.attachmentsError
        ? throwError(options.attachmentsError)
        : of(attachments)
    );

    candidateAttachmentServiceSpy.deleteAttachment.and.returnValue(
      options?.deleteError ? throwError(options.deleteError) : of({} as any)
    );

    candidateAttachmentServiceSpy.updateAttachment.and.returnValue(
      options?.updateError ? throwError(options.updateError) : of({} as any)
    );

    candidateAttachmentServiceSpy.uploadAttachment.and.returnValue(
      options?.uploadError ? throwError(options.uploadError) : of(attachments[0])
    );

    candidateAttachmentServiceSpy.downloadAttachment.and.returnValue(
      options?.downloadError ? throwError(options.downloadError) : of(void 0)
    );

    await TestBed.configureTestingModule({
      declarations: [
        CandidateAttachmentsComponent,
        FileUploadStubComponent,
        ErrorStubComponent,
        TcButtonStubComponent,
        TcInputStubComponent,
        TcLabelStubComponent
      ],
      imports: [FormsModule, TranslateModule.forRoot()],
      providers: [
        {provide: CandidateService, useValue: candidateServiceSpy},
        {provide: CandidateAttachmentService, useValue: candidateAttachmentServiceSpy},
        {provide: UserService, useValue: userServiceSpy}
      ],
      // NO_ERRORS_SCHEMA suppresses the custom 'customDateTime' pipe format used in the
      // template, which is not a standard Angular built-in format string.
      schemas: [NO_ERRORS_SCHEMA]
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

  describe('ngOnInit', () => {
    it('should load candidate number, user, and filter out CV attachments when cv=false', async () => {
      await configureAndCreate({cv: false});

      expect(candidateServiceSpy.getCandidatePersonal).toHaveBeenCalled();
      expect(userServiceSpy.getMyUser).toHaveBeenCalled();
      expect(candidateAttachmentServiceSpy.listCandidateAttachments).toHaveBeenCalled();
      expect(component.candidateNumber).toBe('C123');
      // The default fixture has 1 CV attachment which is filtered out; 2 non-CV remain.
      expect(component.attachments.length).toBe(2);
      expect(component.attachments.every(a => !a.cv)).toBeTrue();
      expect(component.loading).toBeFalse();
    });

    it('should show only CV attachments when cv=true', async () => {
      await configureAndCreate({cv: true});

      expect(component.attachments.length).toBe(1);
      expect(component.attachments[0].cv).toBeTrue();
    });

    it('should keep all attachments unfiltered in preview mode', async () => {
      await configureAndCreate({preview: true, cv: false});

      expect(component.attachments.length).toBe(3);
    });

    it('should set editTarget to null on init', async () => {
      await configureAndCreate();
      expect(component.editTarget).toBeNull();
    });
  });

  describe('template', () => {
    beforeEach(async () => configureAndCreate({cv: false}));

    it('should render the file upload component when not in preview mode', () => {
      expect(fixture.debugElement.query(By.directive(FileUploadStubComponent))).toBeTruthy();
    });

    it('should not render the file upload component in preview mode', async () => {
      // This test needs a different input setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      await configureAndCreate({preview: true});

      expect(fixture.debugElement.query(By.directive(FileUploadStubComponent))).toBeNull();
    });

    it('should render edit (info) and delete (error) tc-buttons for attachments owned by the user', () => {
      const colors = fixture.debugElement
      .queryAll(By.directive(TcButtonStubComponent))
      .map(el => el.componentInstance.color);

      expect(colors).toContain('info');
      expect(colors).toContain('error');
    });

    it('should render tc-label and tc-input when an attachment is being edited', () => {
      component.editTarget = component.attachments[0];
      fixture.detectChanges();

      const labelFors = fixture.debugElement
      .queryAll(By.directive(TcLabelStubComponent))
      .map(el => el.componentInstance.for);
      const inputIds = fixture.debugElement
      .queryAll(By.directive(TcInputStubComponent))
      .map(el => el.componentInstance.id);

      expect(labelFors).toContain('attachmentName');
      expect(inputIds).toContain('attachmentName');
    });

    it('should render the empty state message when there are no attachments', async () => {
      // This test needs a different data setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      await configureAndCreate({attachments: [], cv: false});

      const text = (fixture.nativeElement as HTMLElement).textContent || '';
      expect(text).toContain('REGISTRATION.ATTACHMENTS.EMPTYSTATE');
    });
  });

  describe('getAttachmentUrl', () => {
    beforeEach(async () => configureAndCreate({cv: false}));

    it('should return the url property of the attachment', () => {
      // attachments[0] after cv=false filter is certificate.pdf (id 2)
      expect(component.getAttachmentUrl(component.attachments[0]))
      .toBe('https://files.example/certificate.pdf');
    });
  });

  describe('editCandidateAttachment', () => {
    beforeEach(async () => configureAndCreate({cv: false}));

    it('should set editTarget to the attachment on first call', () => {
      const attachment = component.attachments[0];
      component.editCandidateAttachment(attachment);
      expect(component.editTarget).toBe(attachment);
    });

    it('should clear editTarget to null on second call (toggle)', () => {
      // Note: the component clears editTarget whenever it is already set, regardless of
      // which attachment is passed in — the toggle is not per-attachment.
      const attachment = component.attachments[0];
      component.editCandidateAttachment(attachment);
      component.editCandidateAttachment(attachment);
      expect(component.editTarget).toBeNull();
    });
  });

  describe('deleteAttachment', () => {
    beforeEach(async () => configureAndCreate({cv: false}));

    it('should call deleteAttachment with the attachment id', () => {
      const attachment = component.attachments[0];
      component.deleteAttachment(attachment);
      expect(candidateAttachmentServiceSpy.deleteAttachment).toHaveBeenCalledWith(attachment.id);
    });

    it('should remove the deleted attachment from the list by name on success', () => {
      const attachment = component.attachments[0];
      const removedName = attachment.name;
      component.deleteAttachment(attachment);
      // The component filters by name, not id
      expect(component.attachments.find(a => a.name === removedName)).toBeUndefined();
    });

    it('should clear deleting on success', () => {
      component.deleteAttachment(component.attachments[0]);
      expect(component.deleting).toBeFalse();
    });

    it('should set error and clear deleting on failure', async () => {
      // This test needs a different service setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      const serverError = {status: 500};
      await configureAndCreate({deleteError: serverError, cv: false});

      component.deleteAttachment(component.attachments[0]);

      expect(component.error).toEqual(serverError);
      expect(component.deleting).toBeFalse();
    });
  });

  describe('updateAttachmentName', () => {
    beforeEach(async () => configureAndCreate({cv: false}));

    it('should call updateAttachment with the attachment id and updated name', () => {
      const attachment = component.attachments[0];
      attachment.name = 'renamed.pdf';
      component.updateAttachmentName(attachment, 0);
      expect(candidateAttachmentServiceSpy.updateAttachment).toHaveBeenCalledWith(attachment.id, {
        name: 'renamed.pdf'
      });
    });

    it('should clear editTarget and saving on success', () => {
      const attachment = component.attachments[0];
      component.editTarget = attachment;
      component.updateAttachmentName(attachment, 0);
      expect(component.editTarget).toBeNull();
      expect(component.saving).toBeFalse();
    });

    it('should set error on failure', async () => {
      // This test needs a different service setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      // Note: the component does not clear saving on error — this is a known gap in the component.
      TestBed.resetTestingModule();
      const serverError = {status: 503};
      await configureAndCreate({updateError: serverError, cv: false});

      component.updateAttachmentName(component.attachments[0], 0);

      expect(component.error).toEqual(serverError);
    });
  });

  describe('startServerUpload', () => {
    beforeEach(async () => configureAndCreate({cv: false}));

    it('should call uploadAttachment and refresh attachments on success', () => {
      const refreshSpy = spyOn<any>(component, 'refreshAttachments').and.callThrough();
      component.startServerUpload({files: [new File(['test'], 'doc.pdf')], type: 'file'});

      expect(candidateAttachmentServiceSpy.uploadAttachment).toHaveBeenCalled();
      expect(refreshSpy).toHaveBeenCalled();
      expect(component.uploading).toBeFalse();
    });

    it('should set error and clear uploading on failure', async () => {
      // This test needs a different service setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      const serverError = {status: 502};
      await configureAndCreate({uploadError: serverError, cv: false});

      component.startServerUpload({files: [new File(['test'], 'doc.pdf')], type: 'file'});

      expect(component.error).toEqual(serverError);
      expect(component.uploading).toBeFalse();
    });
  });

  describe('downloadCandidateAttachment', () => {
    beforeEach(async () => configureAndCreate({cv: false}));

    it('should call downloadAttachment with id and name, then clear downloading', () => {
      const attachment = component.attachments.find(a => a.type === AttachmentType.googlefile) as CandidateAttachment;
      component.downloadCandidateAttachment(attachment);

      expect(candidateAttachmentServiceSpy.downloadAttachment)
      .toHaveBeenCalledWith(attachment.id, attachment.name);
      expect(component.downloading).toBeFalse();
    });

    it('should set error and clear downloading on failure', async () => {
      // This test needs a different service setup than the shared beforeEach fixture,
      // so reset the TestBed before creating a fresh component instance.
      TestBed.resetTestingModule();
      const serverError = {status: 504};
      await configureAndCreate({downloadError: serverError, cv: false});

      const attachment = component.attachments.find(a => a.type === AttachmentType.googlefile) as CandidateAttachment;
      component.downloadCandidateAttachment(attachment);

      expect(component.error).toEqual(serverError);
      expect(component.downloading).toBeFalse();
    });
  });

  describe('error paths', () => {
    it('should set error and stop candidate loading when getCandidatePersonal fails', async () => {
      const serverError = {status: 500};
      await configureAndCreate({candidateError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component._loading.candidate).toBeFalse();
    });

    it('should set error and stop attachment loading when listCandidateAttachments fails', async () => {
      const serverError = {status: 503};
      await configureAndCreate({attachmentsError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component._loading.attachments).toBeFalse();
    });

    it('should set error and stop user loading when getMyUser fails', async () => {
      const serverError = {status: 401};
      await configureAndCreate({userError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component._loading.user).toBeFalse();
    });
  });
});
