/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
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
import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {of, throwError} from 'rxjs';

import {JobIntakeComponentTabBase} from './JobIntakeComponentTabBase';
import {AuthenticationService} from '../../../services/authentication.service';
import {AuthorizationService} from '../../../services/authorization.service';
import {JobService} from '../../../services/job.service';
import {expect, vi} from 'vitest';

class TestJobIntakeTabComponent extends JobIntakeComponentTabBase {
    dataLoaded = vi.fn().mockName('dataLoaded');

    protected onDataLoaded(init: boolean): void {
        this.dataLoaded(init);
    }
}

describe('JobIntakeComponentTabBase', () => {
    let component: TestJobIntakeTabComponent;
    let authenticationService: MockedObject<AuthenticationService>;
    let authorizationService: MockedObject<AuthorizationService>;
    let jobService: MockedObject<JobService>;
    const loggedInUser = { id: 8, firstName: 'Test', lastName: 'User' } as any;

    beforeEach(() => {
        authenticationService = ({
            getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser")
        } as unknown as typeof authenticationService);
        authorizationService = ({
            isEmployerPartner: vi.fn().mockName("AuthorizationService.isEmployerPartner")
        } as unknown as typeof authorizationService);
        jobService = ({
            get: vi.fn().mockName("JobService.get")
        } as unknown as typeof jobService);

        authenticationService.getLoggedInUser.mockReturnValue(loggedInUser);
        authorizationService.isEmployerPartner.mockReturnValue(false);

        TestBed.configureTestingModule({
            providers: [
                { provide: AuthenticationService, useValue: authenticationService },
                { provide: AuthorizationService, useValue: authorizationService },
                { provide: JobService, useValue: jobService }
            ]
        });

        component = new TestJobIntakeTabComponent(authenticationService, authorizationService, jobService);
        component.job = { id: 11 } as any;
    });

    it('should initialize logged-in user in constructor', () => {
        expect(component.loggedInUser).toBe(loggedInUser);
        expect(authenticationService.getLoggedInUser).toHaveBeenCalled();
    });

    it('should run ngOnInit without side effects', () => {
        expect(() => component.ngOnInit()).not.toThrow();
    });

    it('should show employer details to non-employer partners', () => {
        authorizationService.isEmployerPartner.mockReturnValue(false);

        expect(component.canViewEmployerDetails()).toBe(true);
    });

    it('should hide employer details from employer partners', () => {
        authorizationService.isEmployerPartner.mockReturnValue(true);

        expect(component.canViewEmployerDetails()).toBe(false);
    });

    it('should load intake data on input changes', fakeAsync(() => {
        const intake = { salary: '100' } as any;
        jobService.get.mockReturnValue(of({ id: 11, jobOppIntake: intake } as any));

        component.ngOnChanges({ job: {} as any });
        tick();

        expect(jobService.get).toHaveBeenCalledWith(11);
        expect(component.jobIntakeData).toBe(intake);
        expect(component.loading).toBe(false);
        expect(component.error).toBeNull();
        expect(component.dataLoaded).toHaveBeenCalledWith(true);
    }));

    it('should load intake data during manual refresh', fakeAsync(() => {
        const intake = { salary: '200' } as any;
        jobService.get.mockReturnValue(of({ id: 11, jobOppIntake: intake } as any));

        component.refreshIntakeData();
        tick();

        expect(component.jobIntakeData).toBe(intake);
        expect(component.dataLoaded).toHaveBeenCalledWith(false);
    }));

    it('should create empty intake data when the job has none', fakeAsync(() => {
        jobService.get.mockReturnValue(of({ id: 11, jobOppIntake: null } as any));

        component.refreshIntakeData();
        tick();

        expect(component.jobIntakeData).toEqual({});
        expect(component.dataLoaded).toHaveBeenCalledWith(false);
    }));

    it('should expose loading errors', fakeAsync(() => {
        jobService.get.mockReturnValue(throwError('load failed'));

        component.refreshIntakeData();
        tick();

        expect(component.loading).toBe(false);
        expect(component.error).toBe('load failed');
        expect(component.dataLoaded).not.toHaveBeenCalled();
    }));
});
