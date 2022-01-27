/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

import {Candidate} from './candidate';
import {User} from './user';

export enum AttachmentType {
  googlefile = 'googlefile',
  file = 'file',
  link = 'link'
}

export enum UploadType {
  cv = 'cv',
  other = 'other',
}


export interface CandidateAttachment {
  id?: number;
  name: string;
  location: string;
  fileType: string;
  type: AttachmentType;
  migrated: boolean;
  cv: boolean;
  candidate?: Candidate;
  createdBy: User;
  createdDate: string;
  uploadType: UploadType;

}
