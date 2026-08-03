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
import {SimpleChange} from '@angular/core';
import {UntypedFormBuilder} from '@angular/forms';
import {of, throwError} from 'rxjs';

import {CandidateOppsComponent} from './candidate-opps.component';
import {
  CandidateOpportunity,
  CandidateOpportunityStage,
  SearchOpportunityRequest
} from '../../../model/candidate-opportunity';
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
  let chatService: jasmine.SpyObj<ChatService>;
  let authorizationService: jasmine.SpyObj<AuthorizationService>;
  let localStorageService: jasmine.SpyObj<LocalStorageService>;

  const prospectChat = {id: 101} as JobChat;
  const recruitingChat = {id: 102} as JobChat;
  const opportunity = {
    id: 1,
    candidate: {id: 11},
    jobOpp: {id: 22}
  } as CandidateOpportunity;

  beforeEach(() => {
    chatService = jasmine.createSpyObj<ChatService>(
      'ChatService',
      ['getOrCreate', 'combineChatReadStatuses']
    );
    authorizationService = jasmine.createSpyObj<AuthorizationService>(
      'AuthorizationService',
      ['canViewChats']
    );
    localStorageService = jasmine.createSpyObj<LocalStorageService>(
      'LocalStorageService',
      ['get', 'set']
    );

    component = new TestCandidateOppsComponent(
      chatService,
      new UntypedFormBuilder(),
      authorizationService,
      localStorageService,
      jasmine.createSpyObj<CandidateOpportunityService>(
        'CandidateOpportunityService',
        ['searchPaged', 'checkUnreadChats']
      ),
      jasmine.createSpyObj<SalesforceService>('SalesforceService', ['sfOppToLink']),
      jasmine.createSpyObj<CountryService>('CountryService', ['listCountries']),
      jasmine.createSpyObj<PartnerService>('PartnerService', ['listSourcePartners']),
      'en-US'
    );

    component.opps = [];
    component.searchFilter = {focus: jasmine.createSpy('focus')} as any;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should use a candidate opportunity search request', () => {
    expect(component.createRequest()).toEqual(jasmine.any(SearchOpportunityRequest));
  });

  it('should load every candidate opportunity stage', () => {
    expect(component.stageOptions()).toEqual(enumOptions(CandidateOpportunityStage));
  });

  it('should update supplied opportunities and fetch their chats', () => {
    const fetchChats = spyOn<any>(component, 'fetchChats');
    component.candidateOpps = [opportunity];

    component.ngOnChanges({
      candidateOpps: new SimpleChange(undefined, component.candidateOpps, true)
    });

    expect(component.opps).toEqual([opportunity]);
    expect(fetchChats).toHaveBeenCalled();
  });

  it('should return false when an opportunity has no next-step due date', () => {
    expect(component.isOverdue({} as CandidateOpportunity)).toBeFalse();
  });

  it('should identify past dates as overdue and future dates as not overdue', () => {
    const past = {
      nextStepDueDate: new Date(Date.now() - 60000).toISOString()
    } as unknown as CandidateOpportunity;
    const future = {
      nextStepDueDate: new Date(Date.now() + 60000).toISOString()
    } as unknown as CandidateOpportunity;

    expect(component.isOverdue(past)).toBeTrue();
    expect(component.isOverdue(future)).toBeFalse();
  });

  it('should process results without fetching chats when chat access is denied', fakeAsync(() => {
    authorizationService.canViewChats.and.returnValue(false);
    const fetchChats = spyOn<any>(component, 'fetchChats');

    component.processResults({content: []} as SearchResults<CandidateOpportunity>);
    tick();

    expect(component.opps).toEqual([]);
    expect(fetchChats).not.toHaveBeenCalled();
    expect(component.searchFilter.focus).toHaveBeenCalled();
  }));

  it('should fetch chats after processing results when chat access is allowed', fakeAsync(() => {
    authorizationService.canViewChats.and.returnValue(true);
    const fetchChats = spyOn<any>(component, 'fetchChats');

    component.processResults({content: []} as SearchResults<CandidateOpportunity>);
    tick();

    expect(fetchChats).toHaveBeenCalled();
  }));

  it('should build both candidate chat requests and process successful responses', () => {
    component.opps = [opportunity];
    component.error = 'old error';
    chatService.getOrCreate.and.returnValues(of(prospectChat), of(recruitingChat));
    const processOppChats = spyOn<any>(component, 'processOppChats');

    (component as any).fetchChats();

    expect(component.error).toBeNull();
    expect(chatService.getOrCreate.calls.allArgs()).toEqual([
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
    chatService.getOrCreate.and.returnValues(
      throwError(error),
      of(recruitingChat)
    );

    (component as any).fetchChats();

    expect(component.error).toBe(error);
  });
});
