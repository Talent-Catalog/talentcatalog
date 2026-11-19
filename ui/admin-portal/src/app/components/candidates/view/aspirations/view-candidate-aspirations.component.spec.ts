import type {MockedObject} from "vitest";
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {By} from '@angular/platform-browser';

import {ViewCandidateAspirationsComponent} from './view-candidate-aspirations.component';
import {CandidateService} from '../../../../services/candidate.service';
import {EditCandidateAspirationsComponent} from './edit/edit-candidate-aspirations.component';
import {expect, vi} from 'vitest';

describe('ViewCandidateAspirationsComponent', () => {
    let component: ViewCandidateAspirationsComponent;
    let fixture: ComponentFixture<ViewCandidateAspirationsComponent>;
    let modalService: MockedObject<NgbModal>;
    let candidateService: MockedObject<CandidateService>;

    const candidateMock: any = {
        id: 123,
        aspirations: 'I want to become a software engineer.'
    };

    beforeEach(async () => {
        modalService = ({
            open: vi.fn().mockName("NgbModal.open")
        } as unknown as typeof modalService);
        candidateService = ({
            updateCandidate: vi.fn().mockName("CandidateService.updateCandidate")
        } as unknown as typeof candidateService);

        await TestBed.configureTestingModule({
            declarations: [ViewCandidateAspirationsComponent],
            providers: [
                { provide: NgbModal, useValue: modalService },
                { provide: CandidateService, useValue: candidateService }
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();

        fixture = TestBed.createComponent(ViewCandidateAspirationsComponent);
        component = fixture.componentInstance;

        component.candidate = candidateMock;
        component.editable = true;

        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should display edit button when editable is true', () => {
        component.editable = true;
        fixture.detectChanges();

        const editButton = fixture.debugElement.query(By.css('tc-button'));

        expect(editButton).toBeTruthy();
    });

    it('should not display edit button when editable is false', () => {
        component.editable = false;
        fixture.detectChanges();

        const editButton = fixture.debugElement.query(By.css('tc-button'));

        expect(editButton).toBeFalsy();
    });

    it('should call editAspirations when edit button emits onClick', () => {
        vi.spyOn(component, 'editAspirations').mockReturnValue(undefined);

        const editButton = fixture.debugElement.query(By.css('tc-button'));
        editButton.triggerEventHandler('onClick', null);

        expect(component.editAspirations).toHaveBeenCalled();
    });

    it('should open edit aspirations modal with correct options', () => {
        const modalRefMock: any = {
            componentInstance: {},
            result: Promise.resolve(candidateMock)
        };

        modalService.open.mockReturnValue(modalRefMock);

        component.editAspirations();

        expect(modalService.open).toHaveBeenCalledWith(EditCandidateAspirationsComponent, {
            centered: true,
            backdrop: 'static'
        });
    });

    it('should pass candidate id to edit aspirations modal', () => {
        const modalRefMock: any = {
            componentInstance: {},
            result: Promise.resolve(candidateMock)
        };

        modalService.open.mockReturnValue(modalRefMock);

        component.editAspirations();

        expect(modalRefMock.componentInstance.candidateId).toBe(candidateMock.id);
    });

    it('should update candidate when modal closes successfully', fakeAsync(() => {
        const modalRefMock: any = {
            componentInstance: {},
            result: Promise.resolve(candidateMock)
        };

        modalService.open.mockReturnValue(modalRefMock);

        component.editAspirations();
        tick();

        expect(candidateService.updateCandidate).toHaveBeenCalled();
    }));

    it('should not update candidate when modal is dismissed', fakeAsync(() => {
        const modalRefMock: any = {
            componentInstance: {},
            result: Promise.reject()
        };

        modalService.open.mockReturnValue(modalRefMock);

        component.editAspirations();
        tick();

        expect(candidateService.updateCandidate).not.toHaveBeenCalled();
    }));
});
