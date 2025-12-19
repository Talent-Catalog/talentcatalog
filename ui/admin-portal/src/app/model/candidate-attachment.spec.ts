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


import {
  AttachmentType,
  CandidateAttachment,
  CandidateAttachmentRequest
} from "./candidate-attachment";
import {UploadType} from "./task";
import {User} from "./user";

describe('CandidateAttachmentRequest', () => {
  it('should create an instance of CandidateAttachmentRequest with required fields', () => {
    const request = new CandidateAttachmentRequest();
    request.candidateId = 1;
    request.type = AttachmentType.file;
    request.name = 'Test File';
    request.location = 'uploads/test-file.pdf';
    request.cv = true;
    request.uploadType = UploadType.cv;
    request.fileType = 'pdf';

    expect(request.candidateId).toBe(1);
    expect(request.type).toBe(AttachmentType.file);
    expect(request.name).toBe('Test File');
    expect(request.location).toBe('uploads/test-file.pdf');
    expect(request.cv).toBe(true);
    expect(request.uploadType).toBe(UploadType.cv);
    expect(request.fileType).toBe('pdf');
    expect(request.folder).toBeUndefined();
  });

  it('should create an instance of CandidateAttachmentRequest without optional fields', () => {
    const request = new CandidateAttachmentRequest();
    request.candidateId = 1;
    request.type = AttachmentType.link;
    request.name = 'Test Link';
    request.location = 'http://example.com';
    request.cv = false;
    request.uploadType = UploadType.degree;

    expect(request.candidateId).toBe(1);
    expect(request.type).toBe(AttachmentType.link);
    expect(request.name).toBe('Test Link');
    expect(request.location).toBe('http://example.com');
    expect(request.cv).toBe(false);
    expect(request.uploadType).toBe(UploadType.degree);
    expect(request.fileType).toBeUndefined();
    expect(request.folder).toBeUndefined();
  });

  it('should allow optional fields to be set', () => {
    const request = new CandidateAttachmentRequest();
    request.candidateId = 1;
    request.type = AttachmentType.googlefile;
    request.name = 'Google Drive';
    request.location = 'google-drive-link';
    request.cv = true;
    request.uploadType = UploadType.englishExam;
    request.fileType = 'docx';
    request.folder = 'my-folder';

    expect(request.candidateId).toBe(1);
    expect(request.type).toBe(AttachmentType.googlefile);
    expect(request.name).toBe('Google Drive');
    expect(request.location).toBe('google-drive-link');
    expect(request.cv).toBe(true);
    expect(request.uploadType).toBe(UploadType.englishExam);
    expect(request.fileType).toBe('docx');
    expect(request.folder).toBe('my-folder');
  });
});

describe('CandidateAttachment', () => {
  it('should create an instance of CandidateAttachment with required fields', () => {
    const createdBy: User = { id: 1, name: 'Creator' } as User;
    const updatedBy: User = { id: 2, name: 'Updater' } as User;

    const attachment: CandidateAttachment = {
      id: 1,
      type: AttachmentType.file,
      name: 'Test File',
      location: 'uploads/test-file.pdf',
      url: 'http://example.com/uploads/test-file.pdf',
      createdBy,
      createdDate: Date.now(),
      updatedBy,
      updatedDate: Date.now(),
      migrated: false,
      cv: true,
      uploadType: UploadType.degree,
      fileType: 'pdf'
    };

    expect(attachment.id).toBe(1);
    expect(attachment.type).toBe(AttachmentType.file);
    expect(attachment.name).toBe('Test File');
    expect(attachment.location).toBe('uploads/test-file.pdf');
    expect(attachment.url).toBe('http://example.com/uploads/test-file.pdf');
    expect(attachment.createdBy).toBe(createdBy);
    expect(attachment.createdDate).toBeTruthy();
    expect(attachment.updatedBy).toBe(updatedBy);
    expect(attachment.updatedDate).toBeTruthy();
    expect(attachment.migrated).toBe(false);
    expect(attachment.cv).toBe(true);
    expect(attachment.uploadType).toBe(UploadType.degree);
    expect(attachment.fileType).toBe('pdf');
  });
});
