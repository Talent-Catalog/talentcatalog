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
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of, throwError} from 'rxjs';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {DuolingoAssignmentComponent} from './duolingo-assignment.component';
import {CandidateService} from 'src/app/services/candidate.service';
import {DuolingoCouponService} from 'src/app/services/duolingo-coupon.service';
import {MockCandidate} from 'src/app/MockData/MockCandidate';
import {expect, vi} from 'vitest';


describe('DuolingoAssignmentComponent', () => {
    let component: DuolingoAssignmentComponent;
    let fixture: ComponentFixture<DuolingoAssignmentComponent>;
    let candidateServiceSpy: MockedObject<CandidateService>;
    let duolingoCouponServiceSpy: MockedObject<DuolingoCouponService>;

    beforeEach(async () => {
        candidateServiceSpy = ({
            updateCandidate: vi.fn().mockName("CandidateService.updateCandidate")
        } as unknown as typeof candidateServiceSpy);
        duolingoCouponServiceSpy = ({
            assignCouponToCandidate: vi.fn().mockName("DuolingoCouponService.assignCouponToCandidate")
        } as unknown as typeof duolingoCouponServiceSpy);

        await TestBed.configureTestingModule({
            declarations: [DuolingoAssignmentComponent],
            providers: [
                { provide: CandidateService, useValue: candidateServiceSpy },
                { provide: DuolingoCouponService, useValue: duolingoCouponServiceSpy },
            ],
            schemas: [NO_ERRORS_SCHEMA], // Ignore template errors for isolated testing
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(DuolingoAssignmentComponent);
        component = fixture.componentInstance;
        component.candidate = new MockCandidate(); // Mock candidate
        fixture.detectChanges();
    });

    it('should create the component', () => {
        expect(component).toBeTruthy();
    });

    it('should call assignCouponToCandidate and updateCandidate on success', () => {
        duolingoCouponServiceSpy.assignCouponToCandidate.mockReturnValue(of(null)); // Simulate success

        component.assignDuolingoCouponTask();

        expect(duolingoCouponServiceSpy.assignCouponToCandidate).toHaveBeenCalledWith(1);
        expect(candidateServiceSpy.updateCandidate).toHaveBeenCalled();
    });

    it('should emit errorOccurred event on failure', () => {
        const errorResponse = { message: 'Error assigning coupon' };
        vi.spyOn(component.errorOccurred, 'emit').mockReturnValue(undefined);
        duolingoCouponServiceSpy.assignCouponToCandidate.mockReturnValue(throwError(errorResponse));

        component.assignDuolingoCouponTask();

        expect(component.errorOccurred.emit).toHaveBeenCalledWith(errorResponse);
    });
});
