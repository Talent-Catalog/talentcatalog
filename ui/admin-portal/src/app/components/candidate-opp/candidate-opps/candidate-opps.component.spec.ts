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
import {fakeAsync, tick} from '@angular/core/testing';
import {SimpleChange} from '@angular/core';
import {UntypedFormBuilder} from '@angular/forms';
import {of, throwError} from 'rxjs';

import {CandidateOppsComponent} from './candidate-opps.component';
import {CandidateOpportunity, CandidateOpportunityStage, SearchOpportunityRequest} from '../../../model/candidate-opportunity';
import {CandidateOpportunityService} from '../../../services/candidate-opportunity.service';
import {AuthorizationService} from '../../../services/authorization.service';
import {ChatService} from '../../../services/chat.service';
import {CountryService} from '../../../services/country.service';
import {LocalStorageService} from '../../../services/local-storage.service';
import {PartnerService} from '../../../services/partner.service';
import {SalesforceService} from '../../../services/salesforce.service';
import {JobChat, JobChatType} from '../../../model/chat';
import {SearchResults} from '../../../model/search-results';
import {enumOptions} from '../../../util/enum';
import {expect, vi} from 'vitest';

class TestCandidateOppsComponent extends CandidateOppsComponent {
    createRequest(): SearchOpportunityRequest {
        return this.createSearchRequest();
    }

    stageOptions() {
        return this.loadStages();
    }

    processResults(results: SearchResults<CandidateOpportunity>): void {
        this.processSearchResults(results);
    }
}

describe('CandidateOppsComponent', () => {
    let component: TestCandidateOppsComponent;
    let chatService: MockedObject<ChatService>;
    let authorizationService: MockedObject<AuthorizationService>;
    let localStorageService: MockedObject<LocalStorageService>;

    const prospectChat = { id: 101 } as JobChat;
    const recruitingChat = { id: 102 } as JobChat;
    const opportunity = {
        id: 1,
        candidate: { id: 11 },
        jobOpp: { id: 22 }
    } as CandidateOpportunity;

    beforeEach(() => {
        chatService = ({
            getOrCreate: vi.fn().mockName("ChatService.getOrCreate"),
            combineChatReadStatuses: vi.fn().mockName("ChatService.combineChatReadStatuses")
        } as unknown as typeof chatService);
        authorizationService = ({
            canViewChats: vi.fn().mockName("AuthorizationService.canViewChats")
        } as unknown as typeof authorizationService);
        localStorageService = ({
            get: vi.fn().mockName("LocalStorageService.get"),
            set: vi.fn().mockName("LocalStorageService.set")
        } as unknown as typeof localStorageService);

        component = new TestCandidateOppsComponent(chatService, new UntypedFormBuilder(), authorizationService, localStorageService, ({
            searchPaged: vi.fn().mockName("CandidateOpportunityService.searchPaged"),
            checkUnreadChats: vi.fn().mockName("CandidateOpportunityService.checkUnreadChats")
        } as unknown as ConstructorParameters<typeof TestCandidateOppsComponent>[4]), {
            sfOppToLink: vi.fn().mockName("SalesforceService.sfOppToLink")
        } as unknown as SalesforceService, {
            listCountries: vi.fn().mockName("CountryService.listCountries")
        } as unknown as CountryService, {
            listSourcePartners: vi.fn().mockName("PartnerService.listSourcePartners")
        } as unknown as PartnerService, 'en-US');

        component.opps = [];
        component.searchFilter = { focus: vi.fn().mockName('focus') } as any;
    });

    it('should create the component', () => {
        expect(component).toBeTruthy();
    });

    it('should use a candidate opportunity search request', () => {
        expect(component.createRequest()).toEqual(expect.any(SearchOpportunityRequest));
    });

    it('should load every candidate opportunity stage', () => {
        expect(component.stageOptions()).toEqual(enumOptions(CandidateOpportunityStage));
    });

    it('should update supplied opportunities and fetch their chats', () => {
        const fetchChats = vi.spyOn((component as any), 'fetchChats').mockReturnValue(undefined);
        component.candidateOpps = [opportunity];

        component.ngOnChanges({
            candidateOpps: new SimpleChange(undefined, component.candidateOpps, true)
        });

        expect(component.opps).toEqual([opportunity]);
        expect(fetchChats).toHaveBeenCalled();
    });

    it('should return false when an opportunity has no next-step due date', () => {
        expect(component.isOverdue({} as CandidateOpportunity)).toBe(false);
    });

    it('should identify past dates as overdue and future dates as not overdue', () => {
        const past = {
            nextStepDueDate: new Date(Date.now() - 60000).toISOString()
        } as unknown as CandidateOpportunity;
        const future = {
            nextStepDueDate: new Date(Date.now() + 60000).toISOString()
        } as unknown as CandidateOpportunity;

        expect(component.isOverdue(past)).toBe(true);
        expect(component.isOverdue(future)).toBe(false);
    });

    it('should process results without fetching chats when chat access is denied', fakeAsync(() => {
        authorizationService.canViewChats.mockReturnValue(false);
        const fetchChats = vi.spyOn((component as any), 'fetchChats').mockReturnValue(undefined);

        component.processResults({ content: [] } as SearchResults<CandidateOpportunity>);
        tick();

        expect(component.opps).toEqual([]);
        expect(fetchChats).not.toHaveBeenCalled();
        expect(component.searchFilter.focus).toHaveBeenCalled();
    }));

    it('should fetch chats after processing results when chat access is allowed', fakeAsync(() => {
        authorizationService.canViewChats.mockReturnValue(true);
        const fetchChats = vi.spyOn((component as any), 'fetchChats').mockReturnValue(undefined);

        component.processResults({ content: [] } as SearchResults<CandidateOpportunity>);
        tick();

        expect(fetchChats).toHaveBeenCalled();
    }));

    it('should build both candidate chat requests and process successful responses', () => {
        component.opps = [opportunity];
        component.error = 'old error';
        chatService.getOrCreate.mockReturnValueOnce(of(prospectChat)).mockReturnValueOnce(of(recruitingChat));
        const processOppChats = vi.spyOn((component as any), 'processOppChats').mockReturnValue(undefined);

        (component as any).fetchChats();

        expect(component.error).toBeNull();
        expect(vi.mocked(chatService.getOrCreate).mock.calls).toEqual([
            [{
                    type: JobChatType.CandidateProspect,
                    candidateId: 11
                }],
            [{
                    type: JobChatType.CandidateRecruiting,
                    candidateId: 11,
                    jobId: 22
                }]
        ]);
        expect(processOppChats).toHaveBeenCalledWith([[prospectChat, recruitingChat]]);
    });

    it('should expose an error when fetching a candidate chat fails', () => {
        const error = 'Unable to load chats';
        component.opps = [opportunity];
        chatService.getOrCreate.mockReturnValueOnce(throwError(error)).mockReturnValueOnce(of(recruitingChat));

        (component as any).fetchChats();

        expect(component.error).toBe(error);
    });
});
