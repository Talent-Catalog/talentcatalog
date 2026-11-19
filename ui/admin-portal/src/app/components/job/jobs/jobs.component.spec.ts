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
import {UntypedFormBuilder} from '@angular/forms';
import {of, throwError} from 'rxjs';

import {JobsComponent} from './jobs.component';
import {AuthorizationService} from '../../../services/authorization.service';
import {ChatService} from '../../../services/chat.service';
import {CountryService} from '../../../services/country.service';
import {JobService} from '../../../services/job.service';
import {LocalStorageService} from '../../../services/local-storage.service';
import {PartnerService} from '../../../services/partner.service';
import {SalesforceService} from '../../../services/salesforce.service';
import {SearchOppsBy} from '../../../model/base';
import {Job, JobOpportunityStage, SearchJobRequest} from '../../../model/job';
import {JobChat, JobChatType} from '../../../model/chat';
import {SearchResults} from '../../../model/search-results';
import {enumOptions} from '../../../util/enum';
import {expect, vi} from 'vitest';

class TestJobsComponent extends JobsComponent {
    createRequest(): SearchJobRequest {
        return this.createSearchRequest() as SearchJobRequest;
    }

    stageOptions() {
        return this.loadStages();
    }

    processResults(results: SearchResults<Job>): void {
        this.processSearchResults(results);
    }
}

describe('JobsComponent', () => {
    let component: TestJobsComponent;
    let chatService: MockedObject<ChatService>;
    let authorizationService: MockedObject<AuthorizationService>;

    const job = { id: 44 } as Job;
    const chat = { id: 501 } as JobChat;

    beforeEach(() => {
        chatService = ({
            getOrCreate: vi.fn().mockName("ChatService.getOrCreate"),
            combineChatReadStatuses: vi.fn().mockName("ChatService.combineChatReadStatuses")
        } as unknown as typeof chatService);
        authorizationService = ({
            canViewChats: vi.fn().mockName("AuthorizationService.canViewChats"),
            isEmployerPartner: vi.fn().mockName("AuthorizationService.isEmployerPartner"),
            canSeeJobDetails: vi.fn().mockName("AuthorizationService.canSeeJobDetails")
        } as unknown as typeof authorizationService);

        component = new TestJobsComponent(chatService, new UntypedFormBuilder(), authorizationService, ({
            get: vi.fn().mockName("LocalStorageService.get"),
            set: vi.fn().mockName("LocalStorageService.set")
        } as unknown as ConstructorParameters<typeof TestJobsComponent>[3]), {
            searchPaged: vi.fn().mockName("JobService.searchPaged"),
            checkUnreadChats: vi.fn().mockName("JobService.checkUnreadChats")
        } as unknown as JobService, {
            sfOppToLink: vi.fn().mockName("SalesforceService.sfOppToLink")
        } as unknown as SalesforceService, {
            listCountries: vi.fn().mockName("CountryService.listCountries")
        } as unknown as CountryService, {
            listSourcePartners: vi.fn().mockName("PartnerService.listSourcePartners")
        } as unknown as PartnerService, 'en-US');

        component.opps = [];
        component.sourcePartners = [];
        component.searchFilter = { focus: vi.fn().mockName('focus') } as any;
    });

    it('should create the component', () => {
        expect(component).toBeTruthy();
    });

    it('should create an unrestricted request for other search types', () => {
        component.searchBy = undefined;

        const request = component.createRequest();

        expect(request).toEqual(expect.any(SearchJobRequest));
        expect(request.sfOppClosed).toBeUndefined();
        expect(request.activeStages).toBeUndefined();
        expect(request.starred).toBeUndefined();
    });

    it('should create a live-jobs request', () => {
        component.searchBy = SearchOppsBy.live;

        const request = component.createRequest();

        expect(request.sfOppClosed).toBe(false);
        expect(request.activeStages).toBe(true);
    });

    it('should create a starred-jobs request that includes closed jobs', () => {
        component.searchBy = SearchOppsBy.starredByMe;

        const request = component.createRequest();

        expect(request.starred).toBe(true);
        expect(request.sfOppClosed).toBe(true);
    });

    it('should load every job opportunity stage', () => {
        expect(component.stageOptions()).toEqual(enumOptions(JobOpportunityStage));
    });

    it('should process results without fetching chats when chat access is denied', fakeAsync(() => {
        authorizationService.canViewChats.mockReturnValue(false);
        const fetchChats = vi.spyOn((component as any), 'fetchChats').mockReturnValue(undefined);

        component.processResults({ content: [] } as SearchResults<Job>);
        tick();

        expect(component.opps).toEqual([]);
        expect(fetchChats).not.toHaveBeenCalled();
        expect(component.searchFilter.focus).toHaveBeenCalled();
    }));

    it('should fetch chats after processing results when chat access is allowed', fakeAsync(() => {
        authorizationService.canViewChats.mockReturnValue(true);
        const fetchChats = vi.spyOn((component as any), 'fetchChats').mockReturnValue(undefined);

        component.processResults({ content: [] } as SearchResults<Job>);
        tick();

        expect(fetchChats).toHaveBeenCalled();
    }));

    it('should construct the two common requests plus one request per source partner', () => {
        component.sourcePartners = [{ id: 7 }, { id: 8 }] as any;

        const requests = (component as any).constructChatRequestsForOpp(job);

        expect(requests).toEqual([
            {
                type: JobChatType.AllJobCandidates,
                jobId: 44
            },
            {
                type: JobChatType.JobCreatorAllSourcePartners,
                jobId: 44
            },
            {
                type: JobChatType.JobCreatorSourcePartner,
                jobId: 44,
                sourcePartnerId: 7
            },
            {
                type: JobChatType.JobCreatorSourcePartner,
                jobId: 44,
                sourcePartnerId: 8
            }
        ]);
    });

    it('should fetch all job chats and process successful responses', () => {
        component.opps = [job];
        component.sourcePartners = [{ id: 7 }] as any;
        component.error = 'old error';
        chatService.getOrCreate.mockImplementation(() => of(chat));
        const processOppChats = vi.spyOn((component as any), 'processOppChats').mockReturnValue(undefined);

        (component as any).fetchChats();

        expect(component.error).toBeNull();
        expect(chatService.getOrCreate).toHaveBeenCalledTimes(3);
        expect(processOppChats).toHaveBeenCalledWith([[chat, chat, chat]]);
    });

    it('should expose an error when fetching a job chat fails', () => {
        const error = 'Unable to load job chats';
        component.opps = [job];
        component.sourcePartners = [];
        chatService.getOrCreate.mockImplementation((request) => request.type === JobChatType.AllJobCandidates
            ? throwError(error)
            : of(chat));

        (component as any).fetchChats();

        expect(component.error).toBe(error);
    });

    it('should require destination filtering for non-employer partners', () => {
        authorizationService.isEmployerPartner.mockReturnValue(false);
        expect(component.needsFilterByDestination()).toBe(true);

        authorizationService.isEmployerPartner.mockReturnValue(true);
        expect(component.needsFilterByDestination()).toBe(false);
    });

    it('should delegate job-detail permission checks to AuthorizationService', () => {
        authorizationService.canSeeJobDetails.mockReturnValue(true);
        expect(component.canSeeJobDetails()).toBe(true);

        authorizationService.canSeeJobDetails.mockReturnValue(false);
        expect(component.canSeeJobDetails()).toBe(false);
    });

    it('should delegate chat permission checks to AuthorizationService', () => {
        authorizationService.canViewChats.mockReturnValue(true);
        expect(component.canViewChats()).toBe(true);

        authorizationService.canViewChats.mockReturnValue(false);
        expect(component.canViewChats()).toBe(false);
    });
});
