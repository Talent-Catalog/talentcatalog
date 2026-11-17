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
import {CreateEducationLevelComponent} from "./create-education-level.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {EducationLevelService} from "../../../../services/education-level.service";
import {NgSelectModule} from "@ng-select/ng-select";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {EducationLevel} from "../../../../model/education-level";
import {of, throwError} from "rxjs";
import {expect, vi} from 'vitest';

describe('CreateEducationLevelComponent', () => {
    let component: CreateEducationLevelComponent;
    let fixture: ComponentFixture<CreateEducationLevelComponent>;
    let educationLevelServiceSpy: MockedObject<EducationLevelService>;
    let ngbActiveModalSpy: MockedObject<NgbActiveModal>;
    let formBuilder: UntypedFormBuilder;

    beforeEach(async () => {
        const educationLevelServiceSpyObj = {
            create: vi.fn().mockName("EducationLevelService.create")
        };
        const ngbActiveModalSpyObj = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };

        await TestBed.configureTestingModule({
            declarations: [CreateEducationLevelComponent],
            imports: [ReactiveFormsModule, NgSelectModule],
            providers: [
                { provide: EducationLevelService, useValue: educationLevelServiceSpyObj },
                { provide: NgbActiveModal, useValue: ngbActiveModalSpyObj },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        educationLevelServiceSpy = TestBed.inject(EducationLevelService) as MockedObject<EducationLevelService>;
        ngbActiveModalSpy = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
        formBuilder = TestBed.inject(UntypedFormBuilder);
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CreateEducationLevelComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize form with required fields', () => {
        expect(component.educationLevelForm).toBeDefined();
        expect(component.educationLevelForm.get('level')).toBeTruthy();
        expect(component.educationLevelForm.get('name')).toBeTruthy();
        expect(component.educationLevelForm.get('status')).toBeTruthy();
    });

    it('should call onSave and close modal when education level is successfully created', () => {
        const educationLevel: EducationLevel = { id: 1, level: 1, name: 'Test', status: 'active' };

        educationLevelServiceSpy.create.mockReturnValue(of(educationLevel));

        component.educationLevelForm.patchValue({ level: 1, name: 'Test', status: 'active' });
        component.onSave();

        expect(educationLevelServiceSpy.create).toHaveBeenCalledWith({ level: 1, name: 'Test', status: 'active' });
        expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(educationLevel);
        expect(component.saving).toBe(false);
    });

    it('should handle error when education level creation fails', () => {
        const errorResponse = { status: 500, message: 'Internal Server Error' };
        educationLevelServiceSpy.create.mockReturnValue(throwError(errorResponse));

        component.onSave();

        expect(educationLevelServiceSpy.create).toHaveBeenCalled();
        expect(component.error).toEqual(errorResponse);
        expect(component.saving).toBe(false);
    });

    it('should dismiss modal when dismiss is called', () => {
        component.dismiss();
        expect(ngbActiveModalSpy.dismiss).toHaveBeenCalledWith(false);
    });
});
