/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import type {MockedObject} from "vitest";
import {fakeAsync, flushMicrotasks, tick} from '@angular/core/testing';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {of, throwError} from 'rxjs';

import {MockCandidate} from '../../../../MockData/MockCandidate';
import {Status} from '../../../../model/base';
import {CandidateAttachment} from '../../../../model/candidate-attachment';
import {TaskAssignment} from '../../../../model/task-assignment';
import {TaskType} from '../../../../model/task';
import {AuthorizationService} from '../../../../services/authorization.service';
import {CandidateAttachmentService} from '../../../../services/candidate-attachment.service';
import {CandidateService} from '../../../../services/candidate.service';
import {TaskAssignmentService} from '../../../../services/task-assignment.service';
import {AssignTasksCandidateComponent} from '../../../tasks/assign-tasks-candidate/assign-tasks-candidate.component';
import {ConfirmationComponent} from '../../../util/confirm/confirmation.component';
import {EditTaskAssignmentComponent} from './edit/edit-task-assignment.component';
import {ViewResponseComponent} from './view-response/view-response.component';
import {ViewCandidateTasksComponent} from './view-candidate-tasks.component';
import {expect, vi} from 'vitest';

describe('ViewCandidateTasksComponent', () => {
    let component: ViewCandidateTasksComponent;
    let candidate: MockCandidate;
    let candidateService: MockedObject<CandidateService>;
    let candidateAttachmentService: MockedObject<CandidateAttachmentService>;
    let authorizationService: MockedObject<AuthorizationService>;
    let taskAssignmentService: MockedObject<TaskAssignmentService>;
    let modalService: MockedObject<NgbModal>;

    beforeEach(() => {
        candidateService = ({
            updateCandidate: vi.fn().mockName("CandidateService.updateCandidate")
        } as unknown as typeof candidateService);
        candidateAttachmentService = ({
            listByType: vi.fn().mockName("CandidateAttachmentService.listByType")
        } as unknown as typeof candidateAttachmentService);
        authorizationService = ({
            isDefaultSourcePartner: vi.fn().mockName("AuthorizationService.isDefaultSourcePartner")
        } as unknown as typeof authorizationService);
        taskAssignmentService = ({
            removeTaskAssignment: vi.fn().mockName("TaskAssignmentService.removeTaskAssignment")
        } as unknown as typeof taskAssignmentService);
        modalService = ({
            open: vi.fn().mockName("NgbModal.open")
        } as unknown as typeof modalService);

        candidate = new MockCandidate();
        candidate.user = {
            ...candidate.user,
            firstName: 'Jane',
            lastName: 'Doe'
        };

        component = new ViewCandidateTasksComponent(candidateService, candidateAttachmentService, authorizationService, taskAssignmentService, modalService);
        component.candidate = candidate;
    });

    it('should initialize today', () => {
        const before = Date.now();

        component.ngOnInit();

        expect(component.today).toBeDefined();
        expect(component.today.getTime()).toBeGreaterThanOrEqual(before);
        expect(component.today.getTime()).toBeLessThanOrEqual(Date.now());
    });

    it('should filter tasks when the candidate input changes', () => {
        const filterTasksSpy = vi.spyOn(component, 'filterTasks').mockReturnValue(undefined);

        component.ngOnChanges({
            candidate: {
                currentValue: candidate,
                previousValue: null,
                firstChange: true,
                isFirstChange: () => true
            }
        });

        expect(filterTasksSpy).toHaveBeenCalled();
    });

    it('should separate ongoing, completed, and inactive assignments', () => {
        const ongoing = createAssignment(1, Status.active);
        const completed = createAssignment(2, Status.active, {
            completedDate: new Date('2026-01-01')
        });
        const abandoned = createAssignment(3, Status.active, {
            abandonedDate: new Date('2026-01-02')
        });
        const inactive = createAssignment(4, Status.inactive);
        candidate.taskAssignments = [completed, inactive, ongoing, abandoned];

        component.filterTasks();

        expect(component.ongoingTasks).toEqual([ongoing]);
        expect(component.completedTasks).toEqual([completed, abandoned]);
        expect(component.inactiveTasks).toEqual([inactive]);
    });

    it('should use empty ongoing and completed arrays when assignments are absent', () => {
        candidate.taskAssignments = undefined;

        component.filterTasks();

        expect(component.ongoingTasks).toEqual([]);
        expect(component.completedTasks).toEqual([]);
    });

    it('should identify a required task whose due date has passed as overdue', () => {
        component.today = new Date('2026-07-27');
        const assignment = createAssignment(1, Status.active, {
            dueDate: new Date('2026-07-26')
        });
        assignment.task.optional = false;

        expect(component.isOverdue(assignment)).toBe(true);
    });

    it('should assign a task, refresh the candidate, and hide the notification', fakeAsync(() => {
        const assignment = createAssignment(1, Status.active);
        assignment.task.notifyOnAssignment = true;
        const modalRef = createModalRef(Promise.resolve(assignment));
        modalService.open.mockReturnValue(modalRef);

        component.assignTask();
        flushMicrotasks();

        expect(modalService.open).toHaveBeenCalledWith(AssignTasksCandidateComponent, { centered: true, backdrop: 'static' });
        expect(modalRef.componentInstance.candidateId).toBe(candidate.id);
        expect(component.showAssignmentEmailNotification).toBe(true);
        expect(candidateService.updateCandidate).toHaveBeenCalled();

        tick(5000);
        expect(component.showAssignmentEmailNotification).toBe(false);
    }));

    it('should store a handled error', () => {
        const error = new Error('Request failed');

        component.handleError(error);

        expect(component.error).toBe(error);
    });

    it('should edit an assignment and refresh the candidate', fakeAsync(() => {
        const assignment = createAssignment(1, Status.active);
        const modalRef = createModalRef(Promise.resolve(assignment));
        modalService.open.mockReturnValue(modalRef);

        component.editTaskAssignment(assignment);
        flushMicrotasks();

        expect(modalService.open).toHaveBeenCalledWith(EditTaskAssignmentComponent, { centered: true, backdrop: 'static' });
        expect(modalRef.componentInstance.taskAssignment).toBe(assignment);
        expect(candidateService.updateCandidate).toHaveBeenCalled();
    }));

    it('should delete an assignment and refresh the candidate', fakeAsync(() => {
        const assignment = createAssignment(7, Status.active);
        const modalRef = createModalRef(Promise.resolve(true));
        modalService.open.mockReturnValue(modalRef);
        taskAssignmentService.removeTaskAssignment.mockReturnValue(of(true));
        component.saving = true;

        component.deleteTaskAssignment(assignment);
        flushMicrotasks();

        expect(modalService.open).toHaveBeenCalledWith(ConfirmationComponent, { centered: true, backdrop: 'static' });
        expect(modalRef.componentInstance.message).toBe(`Are you sure you want to delete the task '${assignment.task.displayName}' ` +
            'from the tasks assigned to Jane Doe?');
        expect(taskAssignmentService.removeTaskAssignment).toHaveBeenCalledWith(7);
        expect(candidateService.updateCandidate).toHaveBeenCalled();
        expect(component.saving).toBe(false);
    }));

    it('should store an error when deleting an assignment fails', fakeAsync(() => {
        const assignment = createAssignment(8, Status.active);
        const error = new Error('Delete failed');

        modalService.open.mockReturnValue(createModalRef(Promise.resolve(true)));

        taskAssignmentService.removeTaskAssignment.mockReturnValue(throwError(error));

        component.saving = true;

        component.deleteTaskAssignment(assignment);
        flushMicrotasks();

        expect(component.error).toBe(error);
        expect(component.saving).toBe(false);
    }));

    it('should request and open uploaded response files', () => {
        const assignment = createAssignment(1, Status.active);
        assignment.task.taskType = TaskType.Upload;
        const attachments = [
            { url: 'https://example.com/one.pdf' } as CandidateAttachment
        ];
        candidateAttachmentService.listByType.mockReturnValue(of(attachments));
        const openFilesSpy = vi.spyOn(component, 'openFiles').mockReturnValue(undefined);

        component.viewResponse(assignment);

        expect(candidateAttachmentService.listByType).toHaveBeenCalledWith({
            candidateId: candidate.id,
            uploadType: assignment.task.uploadType
        });
        expect(openFilesSpy).toHaveBeenCalledWith(attachments);
    });

    it('should store an error when uploaded responses cannot be loaded', () => {
        const assignment = createAssignment(1, Status.active);
        assignment.task.taskType = TaskType.Upload;

        const error = new Error('Attachment request failed');

        candidateAttachmentService.listByType.mockReturnValue(throwError(error));

        component.viewResponse(assignment);

        expect(component.error).toBe(error);
    });

    it('should delegate question responses to the question modal', () => {
        const assignment = createAssignment(1, Status.active);
        assignment.task.taskType = TaskType.Question;
        const viewQuestionResponseSpy = vi.spyOn(component, 'viewQuestionResponse').mockReturnValue(undefined);

        component.viewResponse(assignment);

        expect(viewQuestionResponseSpy).toHaveBeenCalledWith(assignment);
    });

    it('should open every response attachment', () => {
        const attachments = [
            { url: 'https://example.com/one.pdf' } as CandidateAttachment,
            { url: 'https://example.com/two.pdf' } as CandidateAttachment
        ];
        const windowOpenSpy = vi.spyOn(window, 'open').mockReturnValue(undefined);

        component.openFiles(attachments);

        expect(windowOpenSpy).toHaveBeenCalledTimes(2);
        expect(vi.mocked(windowOpenSpy).mock.calls[0]).toEqual([attachments[0].url]);
        expect(vi.mocked(windowOpenSpy).mock.calls[1]).toEqual([attachments[1].url]);
        expect(component.loadingResponse).toBe(false);
    });

    it('should open the question-response modal with the assignment', () => {
        const assignment = createAssignment(1, Status.active);
        const modalRef = createModalRef(new Promise(() => undefined));
        modalService.open.mockReturnValue(modalRef);

        component.viewQuestionResponse(assignment);

        expect(modalService.open).toHaveBeenCalledWith(ViewResponseComponent, { centered: true, backdrop: 'static' });
        expect(modalRef.componentInstance.taskAssignment).toBe(assignment);
    });

    it('should return the authorization service source-partner result', () => {
        authorizationService.isDefaultSourcePartner.mockReturnValue(true);

        expect(component.isDefaultSourcePartner()).toBe(true);
        expect(authorizationService.isDefaultSourcePartner).toHaveBeenCalled();
    });

    function createAssignment(id: number, status: Status, overrides: Partial<TaskAssignment> = {}): TaskAssignment {
        const template = candidate.taskAssignments[0];
        return {
            ...template,
            id,
            status,
            abandonedDate: null,
            completedDate: null,
            dueDate: new Date('2026-08-01'),
            task: {
                ...template.task,
                displayName: `Task ${id}`
            },
            ...overrides
        };
    }

    function createModalRef(result: Promise<unknown>): NgbModalRef {
        return {
            componentInstance: {},
            result
        } as NgbModalRef;
    }
});
