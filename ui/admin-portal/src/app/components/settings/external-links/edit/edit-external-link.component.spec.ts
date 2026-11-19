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
import {of, throwError} from "rxjs";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {SavedList, SearchSavedListRequest} from "../../../../model/saved-list";
import {EditExternalLinkComponent} from "./edit-external-link.component";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {SavedListService} from "../../../../services/saved-list.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {MockSavedList} from "../../../../MockData/MockSavedList";
import {expect, vi} from 'vitest';

describe('EditExternalLinkComponent', () => {
    let component: EditExternalLinkComponent;
    let fixture: ComponentFixture<EditExternalLinkComponent>;
    let savedListServiceSpy: MockedObject<SavedListService>;
    let ngbActiveModalSpy: MockedObject<NgbActiveModal>;
    let formBuilder: UntypedFormBuilder;
    const mockSavedList: SavedList = MockSavedList;
    beforeEach(async () => {
        const savedListServiceSpyObj = {
            search: vi.fn().mockName("SavedListService.search"),
            updateShortName: vi.fn().mockName("SavedListService.updateShortName")
        };
        const ngbActiveModalSpyObj = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };

        await TestBed.configureTestingModule({
            declarations: [EditExternalLinkComponent],
            imports: [ReactiveFormsModule, NgSelectModule],
            providers: [
                { provide: SavedListService, useValue: savedListServiceSpyObj },
                { provide: NgbActiveModal, useValue: ngbActiveModalSpyObj },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        savedListServiceSpy = TestBed.inject(SavedListService) as MockedObject<SavedListService>;
        ngbActiveModalSpy = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
        formBuilder = TestBed.inject(UntypedFormBuilder);
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(EditExternalLinkComponent);
        component = fixture.componentInstance;
        mockSavedList.id = 1;
        component.savedList = mockSavedList; // Mocking saved list
        savedListServiceSpy.search.mockReturnValue(of([mockSavedList]));
        savedListServiceSpy.updateShortName.mockReturnValue(of(mockSavedList));

        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize form with saved list details', () => {
        expect(component.form).toBeDefined();
        expect(component.form.get('tcShortName').value).toEqual('TC');
    });

    it('should load saved lists on initialization', fakeAsync(() => {
        const savedLists: SavedList[] = [mockSavedList];
        const request: SearchSavedListRequest = {
            owned: true,
            shared: true,
            global: true
        };

        component.ngOnInit();
        tick(); // Waiting for async operation to complete

        expect(savedListServiceSpy.search).toHaveBeenCalledWith(request);
        expect(component.savedLists).toEqual(savedLists);
    }));

    it('should call onSave and close modal when external link is successfully updated', fakeAsync(() => {
        const shortName = 'updated-link';
        const link: SavedList = mockSavedList;
        component.form.patchValue({ tcShortName: shortName });

        component.onSave();
        tick(); // Waiting for async operation to complete

        expect(savedListServiceSpy.updateShortName).toHaveBeenCalledWith({ savedListId: 1, tcShortName: shortName });
        expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(link);
        expect(component.saving).toBe(false);
    }));

    it('should handle error when updating external link fails', fakeAsync(() => {
        const errorResponse = { status: 500, message: 'Internal Server Error' };
        savedListServiceSpy.updateShortName.mockReturnValue(throwError(errorResponse));

        component.onSave();
        tick(); // Waiting for async operation to complete

        expect(component.error).toEqual(errorResponse);
        expect(component.saving).toBe(false);
    }));

    it('should dismiss modal when dismiss is called', () => {
        component.dismiss();
        expect(ngbActiveModalSpy.dismiss).toHaveBeenCalledWith(false);
    });
});
