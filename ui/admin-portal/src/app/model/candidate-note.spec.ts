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
import {CandidateNote} from "./candidate-note";

describe('CandidateNote Model', () => {
  let user1: User;
  let user2: User;
  let candidateNote: CandidateNote;

  beforeEach(() => {
    user1 = { id: 1, name: 'John Doe', email: 'john.doe@example.com' } as User;
    user2 = { id: 2, name: 'Jane Smith', email: 'jane.smith@example.com' } as User;

    candidateNote = {
      id: 1,
      title: 'Sample Title',
      comment: 'This is a sample comment.',
      noteType: 'General',
      createdBy: user1,
      createdDate: Date.now(),
      updatedBy: user2,
      updatedDate: Date.now()
    };
  });

  it('should create an instance of CandidateNote', () => {
    expect(candidateNote).toBeTruthy();
  });

  it('should have the correct id', () => {
    expect(candidateNote.id).toBe(1);
  });

  it('should have the correct title', () => {
    expect(candidateNote.title).toBe('Sample Title');
  });

  it('should have the correct comment', () => {
    expect(candidateNote.comment).toBe('This is a sample comment.');
  });

  it('should have the correct noteType', () => {
    expect(candidateNote.noteType).toBe('General');
  });

  it('should have the correct createdBy user', () => {
    expect(candidateNote.createdBy).toEqual(user1);
  });

  it('should have the correct createdDate', () => {
    expect(candidateNote.createdDate).toBeTruthy();
  });

  it('should have the correct updatedBy user', () => {
    expect(candidateNote.updatedBy).toEqual(user2);
  });

  it('should have the correct updatedDate', () => {
    expect(candidateNote.updatedDate).toBeTruthy();
  });
});
