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
import {CandidateNameNumSearchComponent} from "./candidate-name-num-search.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateService} from "../../../services/candidate.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {Router} from "@angular/router";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {ReactiveFormsModule} from "@angular/forms";
import {By} from "@angular/platform-browser";
import {SearchResults} from "../../../model/search-results";
import {Candidate} from "../../../model/candidate";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {expect, vi} from 'vitest';

describe('CandidateNameNumSearchComponent', () => {
    let component: CandidateNameNumSearchComponent;
    let fixture: ComponentFixture<CandidateNameNumSearchComponent>;
    let candidateService: MockedObject<CandidateService>;
    let authService: MockedObject<AuthorizationService>;
    let router: MockedObject<Router>;

    const mockCandidate = new MockCandidate();
    const mockResults: SearchResults<Candidate> = {
        first: false, last: false, number: 0, size: 0,
        content: [mockCandidate],
        totalElements: 1,
        totalPages: 1
    };
    beforeEach(async () => {
        const candidateServiceSpy = {
            findByCandidateNumberOrName: vi.fn().mockName("CandidateService.findByCandidateNumberOrName")
        };
        const authServiceSpy = {
            canViewCandidateName: vi.fn().mockName("AuthorizationService.canViewCandidateName")
        };
        const routerSpy = {
            navigate: vi.fn().mockName("Router.navigate")
        };

        await TestBed.configureTestingModule({
            declarations: [CandidateNameNumSearchComponent],
            imports: [ReactiveFormsModule, NgbModule],
            providers: [
                { provide: CandidateService, useValue: candidateServiceSpy },
                { provide: AuthorizationService, useValue: authServiceSpy },
                { provide: Router, useValue: routerSpy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(CandidateNameNumSearchComponent);
        component = fixture.componentInstance;
        candidateService = TestBed.inject(CandidateService) as MockedObject<CandidateService>;
        authService = TestBed.inject(AuthorizationService) as MockedObject<AuthorizationService>;
        router = TestBed.inject(Router) as MockedObject<Router>;
    });

    beforeEach(() => {
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
    //
    it('should display the correct placeholder text when displayValue is null and user is limited', () => {
        authService.canViewCandidateName.mockReturnValue(false);
        component.displayValue = null;
        fixture.detectChanges();
        expect(component.placeholderText).toBe('Candidate number...');
    });
    //
    it('should display the correct placeholder text when displayValue is null and user is not limited', () => {
        authService.canViewCandidateName.mockReturnValue(true);
        component.displayValue = null;
        fixture.detectChanges();
        expect(component.placeholderText).toBe('Name or number...');
    });

    it('should render candidate row correctly when user is limited', () => {
        authService.canViewCandidateName.mockReturnValue(false);
        const row = component.renderCandidateRow(mockCandidate);
        expect(row).toBe(mockCandidate.candidateNumber);
    });

    it('should render candidate row correctly when user is not limited', () => {
        authService.canViewCandidateName.mockReturnValue(true);
        const row = component.renderCandidateRow(mockCandidate);
        expect(row).toBe(`${mockCandidate.candidateNumber}: ${mockCandidate.user.firstName} ${mockCandidate.user.lastName}`);
    });

    it('should handle select search result correctly when handleSelect is displayOnly', () => {
        component.handleSelect = 'displayOnly';
        vi.spyOn(component.candChange, 'emit').mockReturnValue(undefined);
        authService.canViewCandidateName.mockReturnValue(true);

        const input = fixture.debugElement.query(By.css('#quickNumberOrNameSearch')).nativeElement;
        component.selectSearchResult({ preventDefault: () => { }, item: mockCandidate }, input);

        expect(input.value).toBe(`${mockCandidate.candidateNumber}: ${mockCandidate.user.firstName} ${mockCandidate.user.lastName}`);
        expect(component.candChange.emit).toHaveBeenCalledWith(mockCandidate as unknown as string);
    });

    it('should handle select search result correctly when handleSelect is not displayOnly', () => {
        component.handleSelect = 'navigate';
        const input = fixture.debugElement.query(By.css('#quickNumberOrNameSearch')).nativeElement;
        component.selectSearchResult({ preventDefault: () => { }, item: mockCandidate }, input);
        expect(input.value).toBe('');
        expect(router.navigate).toHaveBeenCalledWith(['candidate', mockCandidate.candidateNumber]);
    });
});
