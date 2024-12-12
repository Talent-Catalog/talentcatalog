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

import {Task, TaskType, UploadType} from './task';

describe('Task Interface', () => {
  const mockTask: Task = {
    id: 1,
    name: 'Sample Task',
    daysToComplete: 5,
    description: 'This is a sample task.',
    displayName: 'Sample Task Display Name',
    optional: true,
    helpLink: 'https://example.com/help',
    taskType: TaskType.Question,
    uploadType: UploadType.cv,
    uploadSubfolderName: 'subfolder',
    uploadableFileTypes: 'pdf,jpg',
    candidateAnswerField: 'answerField',
    createdDate: new Date(),
  };

  it('should have the correct id property', () => {
    expect(mockTask.id).toBe(1);
  });

  it('should have the correct name property', () => {
    expect(mockTask.name).toBe('Sample Task');
  });

  it('should have the correct daysToComplete property', () => {
    expect(mockTask.daysToComplete).toBe(5);
  });

  it('should have the correct description property', () => {
    expect(mockTask.description).toBe('This is a sample task.');
  });

  it('should have the correct displayName property', () => {
    expect(mockTask.displayName).toBe('Sample Task Display Name');
  });

  it('should have the correct optional property', () => {
    expect(mockTask.optional).toBe(true);
  });

  it('should have the correct helpLink property', () => {
    expect(mockTask.helpLink).toBe('https://example.com/help');
  });

  it('should have the correct taskType property', () => {
    expect(mockTask.taskType).toBe(TaskType.Question);
  });

  it('should have the correct uploadType property', () => {
    expect(mockTask.uploadType).toBe(UploadType.cv);
  });

  it('should have the correct uploadSubfolderName property', () => {
    expect(mockTask.uploadSubfolderName).toBe('subfolder');
  });

  it('should have the correct uploadableFileTypes property', () => {
    expect(mockTask.uploadableFileTypes).toBe('pdf,jpg');
  });

  it('should have the correct candidateAnswerField property', () => {
    expect(mockTask.candidateAnswerField).toBe('answerField');
  });

  it('should have the correct createdDate property from Auditable', () => {
    expect(mockTask.createdDate instanceof Date).toBe(true);
  });
});
