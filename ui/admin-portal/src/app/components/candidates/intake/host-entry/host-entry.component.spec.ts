import type {MockedObject} from "vitest";
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {UntypedFormBuilder} from '@angular/forms';
import {HostEntryComponent} from './host-entry.component';
import {CandidateService} from '../../../../services/candidate.service';
import {generateYearArray} from '../../../../util/year-helper';
import {of} from 'rxjs';
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {mockCandidateIntakeData} from "../../view/tab/candidate-intake-tab/candidate-intake-tab.component.spec";
import {expect, vi} from 'vitest';

describe('HostEntryComponent', () => {
    let component: HostEntryComponent;
    let fixture: ComponentFixture<HostEntryComponent>;
    let candidateServiceMock: MockedObject<CandidateService>;


    const mockCandidate = {
        id: 1,
        country: { id: 2, name: 'CountryB' }
    };


    beforeEach(async () => {
        candidateServiceMock = ({
            updateIntakeData: vi.fn().mockName("CandidateService.updateIntakeData")
        } as unknown as typeof candidateServiceMock);
        candidateServiceMock.updateIntakeData.mockReturnValue(of());

        await TestBed.configureTestingModule({
            declarations: [HostEntryComponent],
            providers: [
                UntypedFormBuilder,
                { provide: CandidateService, useValue: candidateServiceMock }
            ]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(HostEntryComponent);
        component = fixture.componentInstance;
        component.entity = new MockCandidate();
        component.candidateIntakeData = mockCandidateIntakeData;
        component.entity = mockCandidate;
        component.countries = new MockCandidate().country;
        component.editable = true;
        fixture.detectChanges();
    });

    it('should create the component', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize years array from 1950 to current year', () => {
        const expectedYears = generateYearArray(1950, true);
        expect(component.years).toEqual(expectedYears);
    });

    it('should return countryIdAsNumber as a number', () => {
        component.form.controls['birthCountryId'].setValue('1');
        expect(component.countryIdAsNumber).toBe(1);
    });

    it('should return enterLegally value from form', () => {
        component.form.controls['hostEntryLegally'].setValue('Yes');
        expect(component.enterLegally).toBe('Yes');
    });

    it('should return returnedHome value from form', () => {
        component.form.controls['returnedHome'].setValue('No');
        expect(component.returnedHome).toBe('No');
    });

    it('should detect Other in leftHomeReasons array of strings', () => {
        component.form.controls['leftHomeReasons'].setValue(['Conflict', 'Other']);
        expect(component.hasOther).toBe(true);
    });

    it('should return false for hasOther when Other is not in leftHomeReasons', () => {
        component.form.controls['leftHomeReasons'].setValue(['Conflict', 'Persecution']);
        expect(component.hasOther).toBe(false);
    });

    it('should return returnHomeFuture value from form', () => {
        component.form.controls['returnHomeFuture'].setValue('Unsure');
        expect(component.returnHomeFuture).toBe('Unsure');
    });

    it('should return true for hasNotes when hostEntryYear is set', () => {
        component.form.controls['hostEntryYear'].setValue(2020);
        expect(component.hasNotes).toBe(true);
    });

    it('should return false for hasNotes when hostEntryYear is null or empty', () => {
        component.form.controls['hostEntryYear'].setValue(null);
        expect(component.hasNotes).toBe(false);

        component.form.controls['hostEntryYear'].setValue('');
        expect(component.hasNotes).toBe(false);
    });

    it('should return false for notHostBorn when birthCountryId is not set', () => {
        component.form.controls['birthCountryId'].setValue(null);
        expect(component.notHostBorn).toBe(false);
    });

    it('should call setNoResponse to set form control to NoResponse', () => {
        component.setNoResponse('hostEntryLegally');
        expect(component.form.controls['hostEntryLegally'].value).toBe('NoResponse');
    });

    it('should update candidateIntakeData on field change', () => {
        component.updateDataOnFieldChange('hostEntryYear');
        component.form.controls['hostEntryYear'].setValue(2021);
        fixture.detectChanges();

        expect(component.candidateIntakeData['hostEntryYear']).toBe(2021);
    });
});
