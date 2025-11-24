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
  checkForAbandoned,
  checkForCompleted,
  checkForOngoing,
  checkForOverdue,
  TaskAssignment,
  taskAssignmentSort
} from './task-assignment';
import {Task, TaskType, UploadType} from './task';
import {Status} from "./base";

describe('Task Assignment Sort Function', () => {
  const mockTask1: Task = {
    id: 1,
    name: 'Task 1',
    daysToComplete: 5,
    description: 'Description for Task 1',
    displayName: 'Task 1',
    optional: false,
    docLink: '',
    taskType: TaskType.Question,
    uploadType: UploadType.cv,
    uploadSubfolderName: '',
    uploadableFileTypes: '',
    candidateAnswerField: '',
  };

  const mockTask2: Task = {
    id: 2,
    name: 'Task 2',
    daysToComplete: 3,
    description: 'Description for Task 2',
    displayName: 'Task 2',
    optional: true,
    docLink: '',
    taskType: TaskType.Simple,
    uploadType: UploadType.offer,
    uploadSubfolderName: '',
    uploadableFileTypes: '',
    candidateAnswerField: '',
  };

  const mockTaskAssignment1: TaskAssignment = {
    id: 1,
    abandonedDate: null,
    candidateNotes: '',
    completedDate: null,
    dueDate: new Date('2024-07-31'),
    status: Status.active,
    task: mockTask1,
    answer: '',
  };

  const mockTaskAssignment2: TaskAssignment = {
    id: 2,
    abandonedDate: null,
    candidateNotes: '',
    completedDate: null,
    dueDate: new Date('2024-08-01'),
    status: Status.active,
    task: mockTask2,
    answer: '',
  };

  const mockTaskAssignment3: TaskAssignment = {
    id: 3,
    abandonedDate: new Date('2024-07-29'),
    candidateNotes: '',
    completedDate: null,
    dueDate: new Date('2024-07-25'),
    status: Status.active,
    task: mockTask1,
    answer: '',
  };

  const mockTaskAssignment4: TaskAssignment = {
    id: 4,
    abandonedDate: null,
    candidateNotes: '',
    completedDate: new Date('2024-07-30'),
    dueDate: new Date('2024-07-30'),
    status: Status.inactive,
    task: mockTask2,
    answer: '',
  };

  it('should correctly sort ongoing task assignments by due date then by task name', () => {
    const sorted = [mockTaskAssignment1, mockTaskAssignment2].sort(taskAssignmentSort);
    expect(sorted[0]).toBe(mockTaskAssignment1);
    expect(sorted[1]).toBe(mockTaskAssignment2);
  });

});

describe('Check For Overdue Function', () => {
  it('should return true if there are overdue task assignments that are not abandoned, completed, or optional', () => {
    const mockTaskAssignment1: TaskAssignment = {
      id: 1,
      abandonedDate: null,
      candidateNotes: '',
      completedDate: null,
      dueDate: new Date('2023-01-01'),
      status: Status.active,
      task: {} as Task,
      answer: '',
    };

    const mockTaskAssignment2: TaskAssignment = {
      id: 2,
      abandonedDate: null,
      candidateNotes: '',
      completedDate: null,
      dueDate: new Date('2024-01-01'),
      status: Status.active,
      task: { optional: true } as Task,
      answer: '',
    };

    const result = checkForOverdue([mockTaskAssignment1, mockTaskAssignment2]);
    expect(result).toBe(true);
  });

  it('should return false if all task assignments are either completed, abandoned, or optional', () => {
    const mockTaskAssignment1: TaskAssignment = {
      id: 1,
      abandonedDate: new Date('2023-01-01'),
      candidateNotes: '',
      completedDate: null,
      dueDate: new Date('2023-01-01'),
      status: Status.active,
      task: {} as Task,
      answer: '',
    };

    const mockTaskAssignment2: TaskAssignment = {
      id: 2,
      abandonedDate: null,
      candidateNotes: '',
      completedDate: new Date('2024-01-01'),
      dueDate: new Date('2024-01-01'),
      status: Status.inactive,
      task: {} as Task,
      answer: '',
    };

    const mockTaskAssignment3: TaskAssignment = {
      id: 3,
      abandonedDate: null,
      candidateNotes: '',
      completedDate: null,
      dueDate: new Date('2025-01-01'),
      status: Status.active,
      task: { optional: true } as Task,
      answer: '',
    };

    const result = checkForOverdue([mockTaskAssignment1, mockTaskAssignment2, mockTaskAssignment3]);
    expect(result).toBe(false);
  });
});

describe('Check For Abandoned Function', () => {
  it('should return true if there are abandoned task assignments that are not completed or optional', () => {
    const mockTaskAssignment1: TaskAssignment = {
      id: 1,
      abandonedDate: new Date('2023-01-01'),
      candidateNotes: '',
      completedDate: null,
      dueDate: new Date(),
      status: Status.active,
      task: {} as Task,
      answer: '',
    };

    const mockTaskAssignment2: TaskAssignment = {
      id: 2,
      abandonedDate: null,
      candidateNotes: '',
      completedDate: null,
      dueDate: new Date(),
      status: Status.active,
      task: { optional: true } as Task,
      answer: '',
    };

    const result = checkForAbandoned([mockTaskAssignment1, mockTaskAssignment2]);
    expect(result).toBe(true);
  });

  it('should return false if all abandoned task assignments are either completed or optional', () => {
    const mockTaskAssignment1: TaskAssignment = {
      id: 1,
      abandonedDate: new Date('2023-01-01'),
      candidateNotes: '',
      completedDate: null,
      dueDate: new Date(),
      status: Status.active,
      task: { optional: true } as Task,
      answer: '',
    };

    const mockTaskAssignment2: TaskAssignment = {
      id: 2,
      abandonedDate: null,
      candidateNotes: '',
      completedDate: new Date('2024-01-01'),
      dueDate: new Date(),
      status: Status.inactive,
      task: {} as Task,
      answer: '',
    };

    const result = checkForAbandoned([mockTaskAssignment1, mockTaskAssignment2]);
    expect(result).toBe(false);
  });
});

describe('Check For Completed Function', () => {
  it('should return true if there are completed task assignments', () => {
    const mockTaskAssignment1: TaskAssignment = {
      id: 1,
      abandonedDate: null,
      candidateNotes: '',
      completedDate: new Date('2023-01-01'),
      dueDate: new Date(),
      status: Status.inactive,
      task: {} as Task,
      answer: '',
    };

    const mockTaskAssignment2: TaskAssignment = {
      id: 2,
      abandonedDate: null,
      candidateNotes: '',
      completedDate: null,
      dueDate: new Date(),
      status: Status.active,
      task: { optional: true } as Task,
      answer: '',
    };

    const result = checkForCompleted([mockTaskAssignment1, mockTaskAssignment2]);
    expect(result).toBe(true);
  });

  it('should return false if there are no completed task assignments', () => {
    const mockTaskAssignment1: TaskAssignment = {
      id: 1,
      abandonedDate: new Date('2023-01-01'),
      candidateNotes: '',
      completedDate: null,
      dueDate: new Date(),
      status: Status.active,
      task: {} as Task,
      answer: '',
    };

    const mockTaskAssignment2: TaskAssignment = {
      id: 2,
      abandonedDate: null,
      candidateNotes: '',
      completedDate: null,
      dueDate: new Date(),
      status: Status.active,
      task: { optional: true } as Task,
      answer: '',
    };

    const result = checkForCompleted([mockTaskAssignment1, mockTaskAssignment2]);
    expect(result).toBe(false);
  });
});

describe('Check For Ongoing Function', () => {
  it('should return true if there are ongoing task assignments that are not abandoned or completed', () => {
    const mockTaskAssignment1: TaskAssignment = {
      id: 1,
      abandonedDate: null,
      candidateNotes: '',
      completedDate: null,
      dueDate: new Date(new Date().getTime() + 10 * 24 * 60 * 60 * 1000), // 10 days from now
      status: Status.active,
      task: {} as Task,
      answer: '',
    };

    const mockTaskAssignment2: TaskAssignment = {
      id: 2,
      abandonedDate: null,
      candidateNotes: '',
      completedDate: null,
      dueDate: new Date('2024-01-01'),
      status: Status.active,
      task: { optional: true } as Task,
      answer: '',
    };

    const result = checkForOngoing([mockTaskAssignment1, mockTaskAssignment2]);
    expect(result).toBe(true);
  });

  it('should return false if there are no ongoing task assignments that are not abandoned or completed', () => {
    const mockTaskAssignment1: TaskAssignment = {
      id: 1,
      abandonedDate: new Date('2023-01-01'),
      candidateNotes: '',
      completedDate: null,
      dueDate: new Date(),
      status: Status.active,
      task: {} as Task,
      answer: '',
    };

    const mockTaskAssignment2: TaskAssignment = {
      id: 2,
      abandonedDate: null,
      candidateNotes: '',
      completedDate: new Date('2024-01-01'),
      dueDate: new Date(),
      status: Status.inactive,
      task: {} as Task,
      answer: '',
    };

    const result = checkForOngoing([mockTaskAssignment1, mockTaskAssignment2]);
    expect(result).toBe(false);
  });
});
