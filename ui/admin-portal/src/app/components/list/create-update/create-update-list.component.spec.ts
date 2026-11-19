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
import {CreateUpdateListComponent} from "./create-update-list.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {SavedListService} from "../../../services/saved-list.service";
import {NgbActiveModal, NgbTypeaheadModule} from "@ng-bootstrap/ng-bootstrap";
import {SalesforceService} from "../../../services/salesforce.service";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {MockSavedList} from "../../../MockData/MockSavedList";
import {of, throwError} from "rxjs";
import {JoblinkComponent} from "../../util/joblink/joblink.component";
import {JobNameAndId} from "../../../model/job";
import {expect, vi} from 'vitest';

describe('CreateUpdateListComponent', () => {
    let component: CreateUpdateListComponent;
    let fixture: ComponentFixture<CreateUpdateListComponent>;
    let savedListServiceSpy: MockedObject<SavedListService>;
    let activeModalSpy: MockedObject<NgbActiveModal>;
    let salesforceServiceSpy: MockedObject<SalesforceService>;

    beforeEach(async () => {
        const savedListServiceSpyObj = {
            create: vi.fn().mockName("SavedListService.create"),
            update: vi.fn().mockName("SavedListService.update")
        };
        const activeModalSpyObj = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };
        const salesforceServiceSpyObj = {
            fetchJob: vi.fn().mockName("SalesforceService.fetchJob")
        };

        await TestBed.configureTestingModule({
            declarations: [CreateUpdateListComponent, JoblinkComponent],
            imports: [FormsModule, ReactiveFormsModule, NgbTypeaheadModule],
            providers: [
                { provide: SavedListService, useValue: savedListServiceSpyObj },
                { provide: NgbActiveModal, useValue: activeModalSpyObj },
                { provide: SalesforceService, useValue: salesforceServiceSpyObj },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        savedListServiceSpy = TestBed.inject(SavedListService) as MockedObject<SavedListService>;
        activeModalSpy = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
        salesforceServiceSpy = TestBed.inject(SalesforceService) as MockedObject<SalesforceService>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CreateUpdateListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize form with savedList data', () => {
        component.savedList = MockSavedList;
        component.ngOnInit();
        expect(component.form.value.name).toBe(MockSavedList.name);
        expect(component.form.value.fixed).toBe(MockSavedList.fixed);
    });

    it('should close modal when save is successful', () => {
        savedListServiceSpy.create.mockReturnValue(of(MockSavedList));
        component.save();
        expect(savedListServiceSpy.create).toHaveBeenCalled();
        expect(activeModalSpy.close).toHaveBeenCalled();
    });

    it('should save fails when error throw', () => {
        savedListServiceSpy.create.mockReturnValue(throwError('Error saving list'));
        component.save();
        expect(savedListServiceSpy.create).toHaveBeenCalled();
        expect(component.saving).toBeFalsy();
    });

    it('should close modal when closeModal is called', () => {
        component.closeModal(MockSavedList);
        expect(activeModalSpy.close).toHaveBeenCalled();
    });

    it('should dismiss modal when dismiss is called', () => {
        component.dismiss();
        expect(activeModalSpy.dismiss).toHaveBeenCalled();
    });

    it('should update nameControl value when job is selected', () => {
        const job: JobNameAndId = { id: 1, name: 'Test Job' };
        component.onJobSelection(job);
        expect(component.nameControl.value).toBe('Test Job');
    });

    it('should set jobId to -1 when null job is selected', () => {
        component.onJobSelection(null);
        expect(component.jobId).toBe(-1);
    });

});
