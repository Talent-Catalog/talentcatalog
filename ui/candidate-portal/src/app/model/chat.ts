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
import {User} from "./user";
import {Auditable} from "./base";
import {Reaction} from "./reaction";
import {LinkPreview} from "./link-preview";

export interface Post extends Auditable {
  content: string;
  linkPreviews: LinkPreview[];
}

export interface JobChat {
  id: number;
  type: JobChatType;
  name?: string;
}

export interface JobChatUserInfo {
  numberUnreadChats?: number;
  lastReadPostId?: number;
  lastPostId?: number;
}

export interface ChatPost {
  content: string;
  createdBy: User;
  createdDate: Date;
  id: number;
  jobChat: JobChat;
  updatedBy: User;
  updatedDate: Date;
  reactions?: Reaction[];
  linkPreviews?: LinkPreview[];
}

export interface GroupedMessages {
  date: string;
  messages: ChatPost[];
}

export enum JobChatType {
  JobCreatorSourcePartner,
  JobCreatorAllSourcePartners,
  CandidateProspect,
  CandidateRecruiting,
  AllJobCandidates
}

export interface CreateChatRequest {
  type?: JobChatType;
  jobId?: number;
  sourcePartnerId?: number;
  candidateId?: number;
}
