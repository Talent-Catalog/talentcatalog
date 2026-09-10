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
  let chatService: jasmine.SpyObj<ChatService>;
  let authorizationService: jasmine.SpyObj<AuthorizationService>;

  const job = {id: 44} as Job;
  const chat = {id: 501} as JobChat;

  beforeEach(() => {
    chatService = jasmine.createSpyObj<ChatService>(
      'ChatService',
      ['getOrCreate', 'combineChatReadStatuses']
    );
    authorizationService = jasmine.createSpyObj<AuthorizationService>(
      'AuthorizationService',
      ['canViewChats', 'isEmployerPartner', 'canSeeJobDetails']
    );

    component = new TestJobsComponent(
      chatService,
      new UntypedFormBuilder(),
      authorizationService,
      jasmine.createSpyObj<LocalStorageService>('LocalStorageService', ['get', 'set']),
      jasmine.createSpyObj<JobService>('JobService', ['searchPaged', 'checkUnreadChats']),
      jasmine.createSpyObj<SalesforceService>('SalesforceService', ['sfOppToLink']),
      jasmine.createSpyObj<CountryService>('CountryService', ['listCountries']),
      jasmine.createSpyObj<PartnerService>('PartnerService', ['listSourcePartners']),
      'en-US'
    );

    component.opps = [];
    component.sourcePartners = [];
    component.searchFilter = {focus: jasmine.createSpy('focus')} as any;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should create an unrestricted request for other search types', () => {
    component.searchBy = undefined;

    const request = component.createRequest();

    expect(request).toEqual(jasmine.any(SearchJobRequest));
    expect(request.sfOppClosed).toBeUndefined();
    expect(request.activeStages).toBeUndefined();
    expect(request.starred).toBeUndefined();
  });

  it('should create a live-jobs request', () => {
    component.searchBy = SearchOppsBy.live;

    const request = component.createRequest();

    expect(request.sfOppClosed).toBeFalse();
    expect(request.activeStages).toBeTrue();
  });

  it('should create a starred-jobs request that includes closed jobs', () => {
    component.searchBy = SearchOppsBy.starredByMe;

    const request = component.createRequest();

    expect(request.starred).toBeTrue();
    expect(request.sfOppClosed).toBeTrue();
  });

  it('should load every job opportunity stage', () => {
    expect(component.stageOptions()).toEqual(enumOptions(JobOpportunityStage));
  });

  it('should process results without fetching chats when chat access is denied', fakeAsync(() => {
    authorizationService.canViewChats.and.returnValue(false);
    const fetchChats = spyOn<any>(component, 'fetchChats');

    component.processResults({content: []} as SearchResults<Job>);
    tick();

    expect(component.opps).toEqual([]);
    expect(fetchChats).not.toHaveBeenCalled();
    expect(component.searchFilter.focus).toHaveBeenCalled();
  }));

  it('should fetch chats after processing results when chat access is allowed', fakeAsync(() => {
    authorizationService.canViewChats.and.returnValue(true);
    const fetchChats = spyOn<any>(component, 'fetchChats');

    component.processResults({content: []} as SearchResults<Job>);
    tick();

    expect(fetchChats).toHaveBeenCalled();
  }));

  it('should construct the two common requests plus one request per source partner', () => {
    component.sourcePartners = [{id: 7}, {id: 8}] as any;

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
    component.sourcePartners = [{id: 7}] as any;
    component.error = 'old error';
    chatService.getOrCreate.and.callFake(() => of(chat));
    const processOppChats = spyOn<any>(component, 'processOppChats');

    (component as any).fetchChats();

    expect(component.error).toBeNull();
    expect(chatService.getOrCreate).toHaveBeenCalledTimes(3);
    expect(processOppChats).toHaveBeenCalledWith([[chat, chat, chat]]);
  });

  it('should expose an error when fetching a job chat fails', () => {
    const error = 'Unable to load job chats';
    component.opps = [job];
    component.sourcePartners = [];
    chatService.getOrCreate.and.callFake((request) =>
      request.type === JobChatType.AllJobCandidates
        ? throwError(error)
        : of(chat)
    );

    (component as any).fetchChats();

    expect(component.error).toBe(error);
  });

  it('should require destination filtering for non-employer partners', () => {
    authorizationService.isEmployerPartner.and.returnValue(false);
    expect(component.needsFilterByDestination()).toBeTrue();

    authorizationService.isEmployerPartner.and.returnValue(true);
    expect(component.needsFilterByDestination()).toBeFalse();
  });

  it('should delegate job-detail permission checks to AuthorizationService', () => {
    authorizationService.canSeeJobDetails.and.returnValue(true);
    expect(component.canSeeJobDetails()).toBeTrue();

    authorizationService.canSeeJobDetails.and.returnValue(false);
    expect(component.canSeeJobDetails()).toBeFalse();
  });

  it('should delegate chat permission checks to AuthorizationService', () => {
    authorizationService.canViewChats.and.returnValue(true);
    expect(component.canViewChats()).toBeTrue();

    authorizationService.canViewChats.and.returnValue(false);
    expect(component.canViewChats()).toBeFalse();
  });
});
