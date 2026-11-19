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

import type {Mock, MockedObject} from "vitest";
import {fakeAsync, TestBed, tick} from '@angular/core/testing';
import {of, Subject, throwError} from 'rxjs';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';

import {IntakeComponentTabBase} from './IntakeComponentTabBase';
import {CandidateService} from '../../../services/candidate.service';
import {CountryService} from '../../../services/country.service';
import {EducationLevelService} from '../../../services/education-level.service';
import {OccupationService} from '../../../services/occupation.service';
import {LanguageLevelService} from '../../../services/language-level.service';
import {CandidateNoteService} from '../../../services/candidate-note.service';
import {AuthenticationService} from '../../../services/authentication.service';
import {CrossTabSyncService} from '../../../services/cross-tab-sync.service';
import {Candidate} from "../../../model/candidate";
import {expect, vi} from 'vitest';

class TestIntakeTabComponent extends IntakeComponentTabBase {
    dataLoaded = vi.fn().mockName('dataLoaded');

    protected getTabId(): string {
        return 'test-tab';
    }

    protected onDataLoaded(init: boolean): void {
        this.dataLoaded(init);
    }
}

describe('IntakeComponentTabBase', () => {
    let component: TestIntakeTabComponent;
    let candidateService: MockedObject<CandidateService>;
    let countryService: MockedObject<CountryService>;
    let educationLevelService: MockedObject<EducationLevelService>;
    let occupationService: MockedObject<OccupationService>;
    let languageLevelService: MockedObject<LanguageLevelService>;
    let noteService: MockedObject<CandidateNoteService>;
    let authenticationService: MockedObject<AuthenticationService>;
    let modalService: MockedObject<NgbModal>;
    let crossTabUpdates: Subject<{
        id: number;
    }>;
    let internalUpdates: Subject<Candidate>;

    const candidate: any = {
        id: 10,
        user: { firstName: 'John', lastName: 'Doe' }
    };
    const loggedInUser: any = {
        id: 9,
        firstName: 'Jane',
        lastName: 'Smith'
    };

    const modalRef = (result: Promise<any>) => ({
        componentInstance: {},
        result
    } as any);

    beforeEach(() => {
        internalUpdates = new Subject<Candidate>();
        crossTabUpdates = new Subject<{
            id: number;
        }>();

        candidateService = ({
            candidateUpdated: vi.fn().mockName("CandidateService.candidateUpdated"),
            getIntakeData: vi.fn().mockName("CandidateService.getIntakeData"),
            get: vi.fn().mockName("CandidateService.get"),
            updateCandidate: vi.fn().mockName("CandidateService.updateCandidate"),
            completeIntake: vi.fn().mockName("CandidateService.completeIntake")
        } as unknown as typeof candidateService);
        countryService = ({
            listCountries: vi.fn().mockName("CountryService.listCountries"),
            listTCDestinations: vi.fn().mockName("CountryService.listTCDestinations")
        } as unknown as typeof countryService);
        educationLevelService = ({
            listEducationLevels: vi.fn().mockName("EducationLevelService.listEducationLevels")
        } as unknown as typeof educationLevelService);
        occupationService = ({
            listOccupations: vi.fn().mockName("OccupationService.listOccupations")
        } as unknown as typeof occupationService);
        languageLevelService = ({
            listLanguageLevels: vi.fn().mockName("LanguageLevelService.listLanguageLevels")
        } as unknown as typeof languageLevelService);
        noteService = ({
            create: vi.fn().mockName("CandidateNoteService.create")
        } as unknown as typeof noteService);
        authenticationService = ({
            getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser")
        } as unknown as typeof authenticationService);
        modalService = ({
            open: vi.fn().mockName("NgbModal.open")
        } as unknown as typeof modalService);

        candidateService.candidateUpdated.mockReturnValue(internalUpdates.asObservable());
        countryService.listCountries.mockReturnValue(of([{ id: 1, name: 'A' }] as any));
        countryService.listTCDestinations.mockReturnValue(of([{ id: 2, name: 'B' }] as any));
        educationLevelService.listEducationLevels.mockReturnValue(of([{ id: 3 }] as any));
        occupationService.listOccupations.mockReturnValue(of([{ id: 4 }] as any));
        languageLevelService.listLanguageLevels.mockReturnValue(of([{ id: 5 }] as any));
        candidateService.getIntakeData.mockReturnValue(of({ candidateExams: [] } as any));
        candidateService.get.mockReturnValue(of(candidate));
        authenticationService.getLoggedInUser.mockReturnValue(loggedInUser);
        noteService.create.mockReturnValue(of({} as any));

        TestBed.configureTestingModule({
            providers: [
                { provide: CandidateService, useValue: candidateService },
                { provide: CountryService, useValue: countryService },
                { provide: EducationLevelService, useValue: educationLevelService },
                { provide: OccupationService, useValue: occupationService },
                { provide: LanguageLevelService, useValue: languageLevelService },
                { provide: CandidateNoteService, useValue: noteService },
                { provide: AuthenticationService, useValue: authenticationService },
                { provide: NgbModal, useValue: modalService },
                {
                    provide: CrossTabSyncService,
                    useValue: { candidateUpdated$: crossTabUpdates.asObservable() }
                }
            ]
        });

        TestBed.runInInjectionContext(() => {
            component = new TestIntakeTabComponent(candidateService, countryService, educationLevelService, occupationService, languageLevelService, noteService, authenticationService, modalService);
        });

        component.candidate = { ...candidate, user: { ...candidate.user } };
    });

    it('should refresh after an internal candidate update', () => {
        vi.spyOn(component, 'refreshIntakeData').mockReturnValue(undefined);
        component.hasPendingRemoteUpdate = true;
        component.ngOnInit();

        internalUpdates.next(undefined);

        expect(component.hasPendingRemoteUpdate).toBe(false);
        expect(component.refreshIntakeData).toHaveBeenCalled();
    });

    it('should ignore cross-tab updates for another candidate', () => {
        component.ngOnInit();

        crossTabUpdates.next({ id: 999 });

        expect(component.hasPendingRemoteUpdate).toBe(false);
    });

    it('should mark matching cross-tab updates as pending', () => {
        component.ngOnInit();

        crossTabUpdates.next({ id: 10 });

        expect(component.hasPendingRemoteUpdate).toBe(true);
    });

    it('should not process another remote update while one is pending', () => {
        component.hasPendingRemoteUpdate = true;
        component.ngOnInit();

        crossTabUpdates.next({ id: 10 });

        expect(component.hasPendingRemoteUpdate).toBe(true);
    });

    it('should scroll to top for a matching update when page is scrolled', () => {
        vi.spyOn(window, 'scrollY', 'get').mockReturnValue(250);
        vi.spyOn(window, 'scrollTo').mockReturnValue(undefined);
        component.ngOnInit();

        crossTabUpdates.next({ id: 10 });

        expect(window.scrollTo as Mock).toHaveBeenCalledWith(expect.objectContaining({
            top: 0,
            behavior: 'smooth'
        }));
    });

    it('should not scroll when page is near the top', () => {
        vi.spyOn(window, 'scrollY', 'get').mockReturnValue(100);
        vi.spyOn(window, 'scrollTo').mockReturnValue(undefined);
        component.ngOnInit();

        crossTabUpdates.next({ id: 10 });

        expect(window.scrollTo).not.toHaveBeenCalled();
    });

    it('should stop subscriptions on destroy', () => {
        vi.spyOn(component, 'refreshIntakeData').mockReturnValue(undefined);
        component.ngOnInit();
        component.ngOnDestroy();

        internalUpdates.next(undefined);
        crossTabUpdates.next({ id: 10 });

        expect(component.refreshIntakeData).not.toHaveBeenCalled();
        expect(component.hasPendingRemoteUpdate).toBe(false);
    });

    it('should clear pending state and refresh on request', () => {
        component.hasPendingRemoteUpdate = true;
        vi.spyOn(component, 'refreshIntakeData').mockReturnValue(undefined);

        component.onRefreshRequested();

        expect(component.hasPendingRemoteUpdate).toBe(false);
        expect(component.refreshIntakeData).toHaveBeenCalled();
    });

    it('should load data when tab becomes active', fakeAsync(() => {
        component.tabIsActive = true;
        vi.spyOn((component as any), 'refreshIntakeDataInternal').mockReturnValue(undefined);

        component.ngOnChanges({ tabIsActive: {} as any });
        tick();

        expect((component as any).refreshIntakeDataInternal).toHaveBeenCalledWith(true);
    }));

    it('should not load data when tab is inactive', fakeAsync(() => {
        component.tabIsActive = false;
        vi.spyOn((component as any), 'refreshIntakeDataInternal').mockReturnValue(undefined);

        component.ngOnChanges({ tabIsActive: {} as any });
        tick();

        expect((component as any).refreshIntakeDataInternal).not.toHaveBeenCalled();
    }));

    it('should ignore unrelated input changes', fakeAsync(() => {
        vi.spyOn((component as any), 'refreshIntakeDataInternal').mockReturnValue(undefined);

        component.ngOnChanges({ candidate: {} as any });
        tick();

        expect((component as any).refreshIntakeDataInternal).not.toHaveBeenCalled();
    }));

    it('should load all intake data and emit initial loading state', fakeAsync(() => {
        vi.spyOn(component.loadingChange, 'emit').mockReturnValue(undefined);

        component.tabIsActive = true;
        component.ngOnChanges({ tabIsActive: {} as any });
        tick();

        expect(component.loadingChange.emit).toHaveBeenCalledWith({ loading: true, tabId: 'test-tab' });
        expect(component.loadingChange.emit).toHaveBeenCalledWith({ loading: false, tabId: 'test-tab' });
        expect(component.countries).toEqual([{ id: 1, name: 'A' }] as any);
        expect(component.tcDestinations).toEqual([{ id: 2, name: 'B' }] as any);
        expect(component.nationalities).toEqual([{ id: 1, name: 'A' }] as any);
        expect(component.educationLevels).toEqual([{ id: 3 }] as any);
        expect(component.occupations).toEqual([{ id: 4 }] as any);
        expect(component.languageLevels).toEqual([{ id: 5 }] as any);
        expect(component.candidateIntakeData).toEqual({ candidateExams: [] } as any);
        expect(component.candidate).toBe(candidate);
        expect(component.dataLoaded).toHaveBeenCalledWith(true);
        expect(component.loading).toBe(false);
    }));

    it('should manually refresh without tab-level loading events', fakeAsync(() => {
        vi.spyOn(component.loadingChange, 'emit').mockReturnValue(undefined);

        component.refreshIntakeData();
        tick();

        expect(component.loadingChange.emit).not.toHaveBeenCalled();
        expect(component.dataLoaded).toHaveBeenCalledWith(false);
    }));

    it('should expose initial-load errors and finish loading', fakeAsync(() => {
        vi.spyOn(component.loadingChange, 'emit').mockReturnValue(undefined);
        countryService.listCountries.mockReturnValue(throwError('load failed'));

        component.tabIsActive = true;
        component.ngOnChanges({ tabIsActive: {} as any });
        tick();

        expect(component.error).toBe('load failed');
        expect(component.loading).toBe(false);
        expect(component.loadingChange.emit).toHaveBeenCalledWith({ loading: false, tabId: 'test-tab' });
    }));

    it('should expose manual refresh errors without loading event', fakeAsync(() => {
        vi.spyOn(component.loadingChange, 'emit').mockReturnValue(undefined);
        countryService.listCountries.mockReturnValue(throwError('manual failed'));

        component.refreshIntakeData();
        tick();

        expect(component.error).toBe('manual failed');
        expect(component.loading).toBe(false);
        expect(component.loadingChange.emit).not.toHaveBeenCalled();
    }));

    it('should find the most recent verified DET official exam', () => {
        const older: any = { exam: 'DETOfficial', notes: 'Verification Date: 2025-01-01' };
        const newer: any = { exam: 'DETOfficial', notes: 'Verification Date: 2026-01-01' };
        const invalid: any = { exam: 'DETOfficial', notes: 'No date' };
        const other: any = { exam: 'IELTS', notes: 'Verification Date: 2027-01-01' };

        expect(component.getMostRecentDetOfficialExam([older, invalid, other, newer])).toBe(newer);
        expect(component.getMostRecentDetOfficialExam([])).toBeNull();
    });

    it('should find the highest numeric DET official score', () => {
        const low: any = { exam: 'DETOfficial', score: '80' };
        const high: any = { exam: 'DETOfficial', score: '120' };
        const invalid: any = { exam: 'DETOfficial', score: 'bad' };
        const missing: any = { exam: 'DETOfficial', score: null };
        const other: any = { exam: 'IELTS', score: '150' };

        expect(component.getHighestScoreDetOfficialExam([low, invalid, missing, other, high])).toBe(high);
        expect(component.getHighestScoreDetOfficialExam([])).toBeNull();
    });

    [
        { score: null, className: 'text-mute', tooltip: 'Pending' },
        { score: undefined, className: 'text-mute', tooltip: 'Pending' },
        { score: 'bad', className: 'text-mute', tooltip: 'Pending' },
        { score: '59', className: 'text-danger', tooltip: 'Below requirement' },
        { score: '60', className: 'text-warning', tooltip: 'Needs verification' },
        { score: '89', className: 'text-warning', tooltip: 'Needs verification' },
        { score: '90', className: 'text-success', tooltip: 'Meets language requirements' }
    ].forEach(testCase => {
        it(`should classify exam score ${testCase.score}`, () => {
            const result = component.getExamInfo(testCase.score as any);

            expect(result.className).toBe(testCase.className);
            expect(result.tooltip).toContain(testCase.tooltip);
        });
    });

    it('should label an exam that is both newest and best', () => {
        const exam: any = {
            id: 1,
            exam: 'DETOfficial',
            score: '120',
            notes: 'Verification Date: 2026-01-01'
        };
        component.candidateIntakeData = { candidateExams: [exam] } as any;

        expect(component.getExamLabel(exam)).toBe('DET Official');
    });

    it('should label the newest-only exam', () => {
        const best: any = { id: 1, exam: 'DETOfficial', score: '130', notes: 'Verification Date: 2025-01-01' };
        const newest: any = { id: 2, exam: 'DETOfficial', score: '100', notes: 'Verification Date: 2026-01-01' };
        component.candidateIntakeData = { candidateExams: [best, newest] } as any;

        expect(component.getExamLabel(newest)).toBe('DET Official Newest');
    });

    it('should label the best-only exam', () => {
        const best: any = { id: 1, exam: 'DETOfficial', score: '130', notes: 'Verification Date: 2025-01-01' };
        const newest: any = { id: 2, exam: 'DETOfficial', score: '100', notes: 'Verification Date: 2026-01-01' };
        component.candidateIntakeData = { candidateExams: [best, newest] } as any;

        expect(component.getExamLabel(best)).toBe('DET Official Best');
    });

    it('should label another DET exam', () => {
        const best: any = { id: 1, exam: 'DETOfficial', score: '130', notes: 'Verification Date: 2025-01-01' };
        const newest: any = { id: 2, exam: 'DETOfficial', score: '100', notes: 'Verification Date: 2026-01-01' };
        const other: any = { id: 3, exam: 'DETOfficial', score: '90', notes: 'Verification Date: 2024-01-01' };
        component.candidateIntakeData = { candidateExams: [best, newest, other] } as any;

        expect(component.getExamLabel(other)).toBe('DET');
    });

    it('should create an update intake note', fakeAsync(() => {
        vi.spyOn(component, 'makeUserName' as any);

        component.createIntakeNote('Mini Intake', 'update');
        tick();

        expect(noteService.create).toHaveBeenCalled();
        expect(component.noteRequest.title).toContain('Mini Intake interview updated by Jane Smith');
        expect(candidateService.updateCandidate).toHaveBeenCalledWith(component.candidate);
        expect(component.saving).toBe(false);
    }));

    it('should create a complete intake note including partner abbreviation', fakeAsync(() => {
        authenticationService.getLoggedInUser.mockReturnValue({
            ...loggedInUser,
            partner: { abbreviation: 'ABC' }
        } as any);

        component.createIntakeNote('Full Intake', 'complete');
        tick();

        expect(component.noteRequest.title).toContain('Full Intake interview completed by Jane Smith(ABC)');
        expect(component.saving).toBe(false);
    }));

    it('should expose note creation failure', fakeAsync(() => {
        noteService.create.mockReturnValue(throwError('note failed'));

        component.createIntakeNote('Full Intake', 'update');
        tick();

        expect(component.error).toBe('note failed');
        expect(component.saving).toBe(false);
    }));

    it('should configure and complete a full intake', fakeAsync(() => {
        const updatedCandidate = { ...candidate, fullIntakeCompletedDate: '2026-01-01' };
        modalService.open.mockReturnValue(modalRef(Promise.resolve(true)));
        candidateService.completeIntake.mockReturnValue(of(updatedCandidate as any));
        vi.spyOn(component, 'refreshIntakeData').mockReturnValue(undefined);
        vi.spyOn(component, 'createIntakeNote').mockReturnValue(undefined);

        component.completeIntake(true);
        tick();

        // TODO: vitest-migration: Direct usage of mostRecent() is not supported. Please refactor to access .args directly or use vi.mocked(spy).mock.lastCall. See: https://vitest.dev/api/mocked.html#mock-lastcall
        const ref = modalService.open.mock.results.at(-1)?.value as any;
        expect(ref.componentInstance.title).toBe('Mark Full Intake Complete?');
        expect(candidateService.completeIntake).toHaveBeenCalledWith(10, {
            completedDate: null,
            fullIntake: true
        });
        expect(component.candidate).toBe(updatedCandidate as any);
        expect(component.refreshIntakeData).toHaveBeenCalled();
        expect(component.createIntakeNote).toHaveBeenCalledWith('Full Intake', 'complete');
        expect(component.saving).toBe(false);
    }));

    it('should configure and complete a mini intake', fakeAsync(() => {
        modalService.open.mockReturnValue(modalRef(Promise.resolve(true)));
        candidateService.completeIntake.mockReturnValue(of(candidate));
        vi.spyOn(component, 'refreshIntakeData').mockReturnValue(undefined);
        vi.spyOn(component, 'createIntakeNote').mockReturnValue(undefined);

        component.completeIntake(false);
        tick();

        // TODO: vitest-migration: Direct usage of mostRecent() is not supported. Please refactor to access .args directly or use vi.mocked(spy).mock.lastCall. See: https://vitest.dev/api/mocked.html#mock-lastcall
        const ref = modalService.open.mock.results.at(-1)?.value as any;
        expect(ref.componentInstance.title).toBe('Mark Mini Intake Complete?');
        expect(candidateService.completeIntake).toHaveBeenCalledWith(10, {
            completedDate: null,
            fullIntake: false
        });
        expect(component.createIntakeNote).toHaveBeenCalledWith('Mini Intake', 'complete');
    }));

    it('should do nothing when completion is not confirmed', fakeAsync(() => {
        modalService.open.mockReturnValue(modalRef(Promise.resolve(false)));

        component.completeIntake(true);
        tick();

        expect(candidateService.completeIntake).not.toHaveBeenCalled();
    }));

    it('should tolerate completion modal dismissal', fakeAsync(() => {
        modalService.open.mockReturnValue(modalRef(Promise.reject('dismissed')));

        component.completeIntake(true);
        tick();

        expect(candidateService.completeIntake).not.toHaveBeenCalled();
    }));

    it('should expose completion failure', fakeAsync(() => {
        modalService.open.mockReturnValue(modalRef(Promise.resolve(true)));
        candidateService.completeIntake.mockReturnValue(throwError('complete failed'));

        component.completeIntake(true);
        tick();

        expect(component.error).toBe('complete failed');
        expect(component.saving).toBe(false);
    }));

    it('should accept old intake modal result', fakeAsync(() => {
        const updatedCandidate = { ...candidate, id: 20 };
        modalService.open.mockReturnValue(modalRef(Promise.resolve(updatedCandidate)));
        vi.spyOn(component, 'refreshIntakeData').mockReturnValue(undefined);

        component.inputOldIntake(true);
        tick();

        // TODO: vitest-migration: Direct usage of mostRecent() is not supported. Please refactor to access .args directly or use vi.mocked(spy).mock.lastCall. See: https://vitest.dev/api/mocked.html#mock-lastcall
        const ref = modalService.open.mock.results.at(-1)?.value as any;
        expect(ref.componentInstance.fullIntake).toBe(true);
        expect(ref.componentInstance.candidate.id).toBe(10);
        expect(component.candidate).toBe(updatedCandidate as any);
        expect(candidateService.updateCandidate).toHaveBeenCalledWith(updatedCandidate as any);
        expect(component.refreshIntakeData).toHaveBeenCalled();
        expect(component.saving).toBe(false);
    }));

    it('should expose old intake modal rejection', fakeAsync(() => {
        modalService.open.mockReturnValue(modalRef(Promise.reject('old intake failed')));

        component.inputOldIntake(false);
        tick();

        expect(component.error).toBe('old intake failed');
        expect(component.saving).toBe(false);
    }));
});
