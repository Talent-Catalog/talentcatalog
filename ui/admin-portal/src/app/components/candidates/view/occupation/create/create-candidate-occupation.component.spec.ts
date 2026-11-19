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
import {CreateCandidateOccupationComponent} from "./create-candidate-occupation.component";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgxWigModule} from "ngx-wig";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateOccupationService} from "../../../../../services/candidate-occupation.service";
import {OccupationService} from "../../../../../services/occupation.service";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {of, throwError} from "rxjs";
import {expect, vi} from 'vitest';

describe('CreateCandidateOccupationComponent', () => {
    let component: CreateCandidateOccupationComponent;
    let fixture: ComponentFixture<CreateCandidateOccupationComponent>;
    let mockActiveModal: any;
    let mockCandidateOccupationService: any;
    let mockOccupationService: any;

    const mockOccupations = [
        { id: 1, name: 'Software Engineer', isco08Code: '1234', status: 'active' },
        { id: 2, name: 'Data Scientist', isco08Code: '5678', status: 'active' }
    ];
    beforeEach(waitForAsync(() => {
        mockActiveModal = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };
        mockCandidateOccupationService = {
            create: vi.fn().mockName("CandidateOccupationService.create")
        };
        mockOccupationService = {
            listOccupations: vi.fn().mockName("OccupationService.listOccupations")
        };

        TestBed.configureTestingModule({
            declarations: [CreateCandidateOccupationComponent],
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule, NgxWigModule],
            providers: [
                UntypedFormBuilder,
                { provide: NgbActiveModal, useValue: mockActiveModal },
                { provide: CandidateOccupationService, useValue: mockCandidateOccupationService },
                { provide: OccupationService, useValue: mockOccupationService },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(CreateCandidateOccupationComponent);
        component = fixture.componentInstance;
        mockOccupationService.listOccupations.mockReturnValue(of(mockOccupations));

        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize without errors and load the list of occupations', () => {

        mockOccupationService.listOccupations.mockReturnValue(of(mockOccupations));

        component.ngOnInit();

        expect(component.loading).toBe(false);
        expect(component.occupations).toEqual(mockOccupations);
        expect(component.form).toBeDefined();
        expect(component.form.controls.occupationId).toBeDefined();
        expect(component.form.controls.yearsExperience).toBeDefined();

    });

    it('should handle errors when loading occupations', () => {
        const error = 'Error loading occupations';
        mockOccupationService.listOccupations.mockReturnValue(throwError(error));

        component.ngOnInit();

        expect(component.loading).toBe(false);
        expect(component.error).toBe(error);
    });

    it('should save candidate occupation successfully', () => {
        const mockCandidateOccupation = { id: 1, occupation: { id: 1, name: 'Software Engineer', isco08Code: '1234', status: 'active' }, yearsExperience: 5 };
        mockCandidateOccupationService.create.mockReturnValue(of(mockCandidateOccupation));

        component.candidateId = 1;
        component.form.setValue({
            occupationId: 1,
            yearsExperience: 5
        });

        component.onSave();


        expect(component.saving).toBe(false);
        expect(mockActiveModal.close).toHaveBeenCalledWith(mockCandidateOccupation);
    });

    it('should handle errors when saving candidate occupation', () => {
        const error = 'Error saving candidate occupation';
        mockCandidateOccupationService.create.mockReturnValue(throwError(error));

        component.candidateId = 1;
        component.form.setValue({
            occupationId: 1,
            yearsExperience: 5
        });

        component.onSave();

        expect(component.saving).toBe(false);
        expect(component.error).toBe(error);
    });
});
