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
import {SelectListComponent, TargetListSelection} from "./select-list.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {SavedListService} from "../../../services/saved-list.service";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {MockSavedList} from "../../../MockData/MockSavedList";
import {of, throwError} from "rxjs";
import {CandidateStatus, UpdateCandidateStatusInfo} from "../../../model/candidate";
import {JobNameAndId} from "../../../model/job";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockJob} from "../../../MockData/MockJob";
import {SavedList} from "../../../model/saved-list";
import {DtoType} from "../../../model/base";
import {AuthorizationService} from "../../../services/authorization.service";
import {expect, vi} from 'vitest';

describe('SelectListComponent', () => {
    let component: SelectListComponent;
    let fixture: ComponentFixture<SelectListComponent>;
    let activeModalSpy: MockedObject<NgbActiveModal>;
    let savedListServiceSpy: MockedObject<SavedListService>;
    let formBuilder: UntypedFormBuilder;

    beforeEach(async () => {
        const activeModalSpyObj = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };
        const savedListServiceSpyObj = {
            search: vi.fn().mockName("SavedListService.search")
        };
        const authorizationServiceSpyObj = {
            isCandidateSourceMine: vi.fn().mockName("AuthorizationService.isCandidateSourceMine")
        };
        authorizationServiceSpyObj.isCandidateSourceMine.mockReturnValue(true);

        await TestBed.configureTestingModule({
            declarations: [SelectListComponent],
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule],
            providers: [
                UntypedFormBuilder,
                { provide: NgbActiveModal, useValue: activeModalSpyObj },
                { provide: SavedListService, useValue: savedListServiceSpyObj },
                { provide: AuthorizationService, useValue: authorizationServiceSpyObj },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        activeModalSpy = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
        savedListServiceSpy = TestBed.inject(SavedListService) as MockedObject<SavedListService>;
        formBuilder = TestBed.inject(UntypedFormBuilder);
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SelectListComponent);
        component = fixture.componentInstance;

        component.excludeList = { ...MockSavedList, id: 1 }; // Add an id to the excludeList
        savedListServiceSpy.search.mockReturnValue(of([MockSavedList]));
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize form with default values', () => {
        expect(component.form).toBeDefined();
        expect(component.newList).toBe(false);
        expect(component.replace).toBe(false);
        expect(component.changeStatuses).toBe(false);
    });

    it('should request minimal non-fixed list data when loading lists', () => {
        expect(savedListServiceSpy.search).toHaveBeenCalledWith(expect.objectContaining({
            dtoType: DtoType.MINIMAL,
            fixed: false,
            owned: true,
            shared: true,
            global: true
        }));
    });

    it('should handle error while loading lists', fakeAsync(() => {
        const errorMessage = 'Error loading lists';
        savedListServiceSpy.search.mockReturnValue(throwError(errorMessage));

        component.ngOnInit();
        tick(); // Wait for observable to resolve

        expect(savedListServiceSpy.search).toHaveBeenCalled();
        expect(component.error).toEqual(errorMessage);
        expect(component.loading).toBe(false);
    }));

    it('should dismiss modal', () => {
        component.dismiss();
        expect(activeModalSpy.dismiss).toHaveBeenCalled();
    });

    it('should select list and close modal', () => {
        component.jobId = 1;
        const selection: TargetListSelection = {
            savedListId: 0,
            newListName: null,
            replace: false,
            jobId: 1
        };
        component.form.patchValue({ newList: false }); // Simulate selecting existing list

        component.select();
        expect(activeModalSpy.close).toHaveBeenCalledWith(selection);
    });

    it('should handle job selection', () => {
        const job: JobNameAndId = { id: 1, name: 'Test Job' };
        component.onJobSelection(job);
        expect(component.jobName).toBe('Test Job');
        expect(component.jobId).toBe(1);
        expect(component.newListNameControl.value).toBe('Test Job');
    });

    it('should handle status info update', () => {
        const statusInfo: UpdateCandidateStatusInfo = { status: CandidateStatus.active };
        component.onStatusInfoUpdate(statusInfo);
        expect(component['statusUpdateInfo']).toEqual(statusInfo);
    });

    it('should disable new list', () => {
        component.disableNew();
        expect(component.form.get('newList').disabled).toBe(true);
    });

    it('should enable new list', () => {
        component.enableNew();
        expect(component.form.get('newList').enabled).toBe(true);
        expect(component.form.get('savedList').value).toBeNull();
    });

    it('should set replace to false if existing list is a submission list', () => {
        let selectedSubmissionList = {
            id: 2,
            name: 'Submission List',
            sfJobOpp: MockJob,
            fixed: true,
            global: true
        } as SavedList;
        component.form.controls['savedList'].patchValue(selectedSubmissionList);
        component.select();
        expect(component.form.get('replace').value).toBe(false);
    });

});
