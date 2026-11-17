import type {MockedObject} from "vitest";
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {EditCandidateOccupationComponent} from './edit-candidate-occupation.component';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {CandidateOccupationService} from '../../../../../services/candidate-occupation.service';
import {OccupationService} from '../../../../../services/occupation.service';
import {ReactiveFormsModule} from '@angular/forms';
import {of, throwError} from 'rxjs';
import {CandidateOccupation} from '../../../../../model/candidate-occupation';
import {Occupation} from '../../../../../model/occupation';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {MockUser} from "../../../../../MockData/MockUser";
import {expect, vi} from 'vitest';

describe('EditCandidateOccupationComponent', () => {
    let component: EditCandidateOccupationComponent;
    let fixture: ComponentFixture<EditCandidateOccupationComponent>;
    let mockOccupationService: MockedObject<OccupationService>;
    let mockCandidateOccupationService: MockedObject<CandidateOccupationService>;
    let mockActiveModal: MockedObject<NgbActiveModal>;

    const mockOccupations: Occupation[] = [
        {
            id: 1,
            name: 'Software Engineer',
            isco08Code: '2512',
            status: 'ACTIVE'
        },
        {
            id: 2,
            name: 'Data Scientist',
            isco08Code: '2521',
            status: 'ACTIVE'
        }
    ];

    const mockCandidateOccupation: CandidateOccupation = {
        id: 123,
        occupation: {
            id: 1,
            name: 'Software Engineer',
            isco08Code: '2512',
            status: 'ACTIVE'
        },
        yearsExperience: 3,
        updatedDate: 2024,
        createdDate: 2004,
        createdBy: new MockUser(),
        updatedBy: new MockUser(),
        migrationOccupation: null,
        candidateJobExperiences: []
    };


    beforeEach(async () => {
        mockOccupationService = ({
            listOccupations: vi.fn().mockName("OccupationService.listOccupations")
        } as unknown as typeof mockOccupationService);
        mockCandidateOccupationService = ({
            update: vi.fn().mockName("CandidateOccupationService.update")
        } as unknown as typeof mockCandidateOccupationService);
        mockActiveModal = ({
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        } as unknown as typeof mockActiveModal);

        await TestBed.configureTestingModule({
            declarations: [EditCandidateOccupationComponent],
            imports: [ReactiveFormsModule],
            providers: [
                { provide: OccupationService, useValue: mockOccupationService },
                { provide: CandidateOccupationService, useValue: mockCandidateOccupationService },
                { provide: NgbActiveModal, useValue: mockActiveModal }
            ],
            schemas: [NO_ERRORS_SCHEMA] // Ignores unknown elements like <ng-select>
        }).compileComponents();

        fixture = TestBed.createComponent(EditCandidateOccupationComponent);
        component = fixture.componentInstance;
        // Setup mocks
        mockOccupationService.listOccupations.mockReturnValue(of([]));
        mockCandidateOccupationService.update.mockReturnValue(of(mockCandidateOccupation));

        component.candidateOccupation = mockCandidateOccupation;
    });

    it('should initialize the form with candidate occupation data', fakeAsync(() => {
        mockOccupationService.listOccupations.mockReturnValue(of(mockOccupations));

        component.ngOnInit();
        tick();

        expect(component.form.value).toEqual({
            occupationId: 1,
            yearsExperience: 3,
            markAsPrincipal: false
        });
        expect(component.occupations.length).toBe(2);
        expect(component.loading).toBe(false);
    }));

    it('should call update service with principal false and close modal on save success', fakeAsync(() => {
        component.candidateOccupation = mockCandidateOccupation;
        component.ngOnInit();

        component.form.setValue({ occupationId: 2, yearsExperience: 5, markAsPrincipal: false });

        component.onSave();
        tick();

        expect(mockCandidateOccupationService.update).toHaveBeenCalledWith(123, {
            occupationId: 2,
            yearsExperience: 5,
            principal: false
        });
        expect(mockActiveModal.close).toHaveBeenCalledWith(mockCandidateOccupation);
    }));

    it('should call update service with principal true when markAsPrincipal is checked', fakeAsync(() => {
        const updatedOccupation = { ...mockCandidateOccupation, yearsExperience: 5 };
        mockCandidateOccupationService.update.mockReturnValue(of(updatedOccupation));
        component.candidateOccupation = mockCandidateOccupation;
        component.ngOnInit();

        component.form.setValue({ occupationId: 1, yearsExperience: 5, markAsPrincipal: true });

        component.onSave();
        tick();

        expect(mockCandidateOccupationService.update).toHaveBeenCalledWith(123, {
            occupationId: 1,
            yearsExperience: 5,
            principal: true
        });
        expect(mockActiveModal.close).toHaveBeenCalledWith(updatedOccupation);
    }));

    it('should surface an error if the update fails', fakeAsync(() => {
        mockCandidateOccupationService.update.mockReturnValue(throwError('update error'));
        component.candidateOccupation = mockCandidateOccupation;
        component.ngOnInit();

        component.form.setValue({ occupationId: 1, yearsExperience: 5, markAsPrincipal: true });

        component.onSave();
        tick();

        expect(component.error).toBe('update error');
        expect(component.saving).toBe(false);
        expect(mockActiveModal.close).not.toHaveBeenCalled();
    }));

    it('should dismiss the modal when dismiss() is called', () => {
        component.dismiss();
        expect(mockActiveModal.dismiss).toHaveBeenCalledWith(false);
    });

});
