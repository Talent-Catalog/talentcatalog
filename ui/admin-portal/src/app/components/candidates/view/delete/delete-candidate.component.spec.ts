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
import {DeleteCandidateComponent} from "./delete-candidate.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../services/candidate.service";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockCandidate} from "../../../../MockData/MockCandidate";

import {CandidatePipe} from "../../../../pipes/candidate.pipe";
import {of} from "rxjs";
import {expect, vi} from 'vitest';

describe('DeleteCandidateComponent', () => {
    let component: DeleteCandidateComponent;
    let fixture: ComponentFixture<DeleteCandidateComponent>;
    let mockActiveModal: MockedObject<NgbActiveModal>;
    let mockCandidateService: MockedObject<CandidateService>;

    const mockCandidate = new MockCandidate();
    beforeEach(async () => {
        const activeModalSpy = {
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss"),
            close: vi.fn().mockName("NgbActiveModal.close")
        };
        const candidateServiceSpy = {
            delete: vi.fn().mockName("CandidateService.delete")
        };

        await TestBed.configureTestingModule({
            declarations: [DeleteCandidateComponent, CandidatePipe],
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule],
            providers: [
                { provide: NgbActiveModal, useValue: activeModalSpy },
                { provide: CandidateService, useValue: candidateServiceSpy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        })
            .compileComponents();

        mockActiveModal = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
        mockCandidateService = TestBed.inject(CandidateService) as MockedObject<CandidateService>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(DeleteCandidateComponent);
        component = fixture.componentInstance;
        component.candidate = mockCandidate;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should dismiss the modal when cancel is called', () => {
        component.cancel();
        expect(mockActiveModal.dismiss).toHaveBeenCalled();
    });

    it('should set deleting to false and close the modal when confirm is called', () => {
        component.candidate = mockCandidate;
        mockCandidateService.delete.mockReturnValue(of(true));
        component.confirm();
        expect(component.deleting).toBe(false);
        expect(mockCandidateService.delete).toHaveBeenCalledWith(mockCandidate.id);
        expect(mockActiveModal.close).toHaveBeenCalled();
    });
});
