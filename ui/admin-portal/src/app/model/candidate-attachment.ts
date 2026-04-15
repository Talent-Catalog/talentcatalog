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

import {User} from './user';
import {UploadType} from "./task";

export enum AttachmentType {
  googlefile = 'googlefile',
  file = 'file',
  link = 'link'
}

export interface CandidateAttachment {
  id: number;
  createdBy: User;
  createdDate: number;
  fileType: string;
  migrated: boolean; // A flag determining is the file was migrated from the previous system
  name: string;
  type: AttachmentType;
  updatedBy: User
  updatedDate: number;
  uploadType: UploadType;
  url: string;
}

export class CandidateAttachmentRequest {
  candidateId: number;
  type: AttachmentType;
  name: string;
  url: string;
  uploadType: UploadType;
  fileType?: string; //Not needed for links
  folder?: string; //Only used by S3. Not needed for links or Google
}
