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

import {CommonModule, Location} from '@angular/common';
import {NO_ERRORS_SCHEMA, Pipe, PipeTransform} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ActivatedRoute} from '@angular/router';
import {NgbNavChangeEvent, NgbNavModule} from '@ng-bootstrap/ng-bootstrap';
import {of} from 'rxjs';

import {ViewCandidateComponent} from './view-candidate.component';
import {AuthorizationService} from '../../../services/authorization.service';
import {CandidateService} from '../../../services/candidate.service';
import {ChatService} from '../../../services/chat.service';
import {LinkedinService} from '../../../services/linkedin.service';
import {CasiPortalService} from '../../../services/casi-portal.service';
import {LocalStorageService} from '../../../services/local-storage.service';
import {AuthenticationService} from '../../../services/authentication.service';

@Pipe({name: 'translate'})
class TranslatePipeStub implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

describe('ViewCandidateComponent', () => {
  let component: ViewCandidateComponent;
  let fixture: ComponentFixture<ViewCandidateComponent>;

  let authorizationService: jasmine.SpyObj<AuthorizationService>;
  let candidateService: jasmine.SpyObj<CandidateService>;
  let chatService: jasmine.SpyObj<ChatService>;
  let linkedinService: jasmine.SpyObj<LinkedinService>;
  let casiPortalService: jasmine.SpyObj<CasiPortalService>;
  let localStorageService: jasmine.SpyObj<LocalStorageService>;
  let location: jasmine.SpyObj<Location>;
  let authenticationService: jasmine.SpyObj<AuthenticationService>;

  const candidate = {
    id: 1,
    status: 'active',
    surveyType: {
      id: 1
    },
    taskAssignments: [],
    candidateOpportunities: []
  };

  beforeEach(async () => {
    authorizationService = jasmine.createSpyObj<AuthorizationService>(
      'AuthorizationService',
      ['canViewChats']
    );

    candidateService = jasmine.createSpyObj<CandidateService>(
      'CandidateService',
      ['getProfile']
    );

    chatService = jasmine.createSpyObj<ChatService>(
      'ChatService',
      [
        'getCandidateProspectChat',
        'getJobChatUserInfo',
        'getChatPosts$',
        'getOrCreate',
        'removeDuplicateChats',
        'markChatAsRead'
      ]
    );

    linkedinService = jasmine.createSpyObj<LinkedinService>(
      'LinkedinService',
      ['isEligible']
    );

    casiPortalService = jasmine.createSpyObj<CasiPortalService>(
      'CasiPortalService',
      ['checkEligibility']
    );

    localStorageService = jasmine.createSpyObj<LocalStorageService>(
      'LocalStorageService',
      ['get', 'set']
    );

    location = jasmine.createSpyObj<Location>(
      'Location',
      ['path', 'replaceState']
    );

    authenticationService = jasmine.createSpyObj<AuthenticationService>(
      'AuthenticationService',
      ['isGrnInstance']
    );

    authorizationService.canViewChats.and.returnValue(false);
    candidateService.getProfile.and.returnValue(of(candidate as any));

    linkedinService.isEligible.and.returnValue(of(false));
    casiPortalService.checkEligibility.and.returnValue(of(false));

    localStorageService.get.and.returnValue(null);

    location.path.and.returnValue('/profile');

    authenticationService.isGrnInstance.and.returnValue(false);

    await TestBed.configureTestingModule({
      imports: [
        CommonModule,
        NgbNavModule
      ],
      declarations: [
        ViewCandidateComponent,
        TranslatePipeStub
      ],
      providers: [
        {
          provide: AuthorizationService,
          useValue: authorizationService
        },
        {
          provide: CandidateService,
          useValue: candidateService
        },
        {
          provide: ChatService,
          useValue: chatService
        },
        {
          provide: LinkedinService,
          useValue: linkedinService
        },
        {
          provide: CasiPortalService,
          useValue: casiPortalService
        },
        {
          provide: LocalStorageService,
          useValue: localStorageService
        },
        {
          provide: Location,
          useValue: location
        },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({})
          }
        },
        {
          provide: AuthenticationService,
          useValue: authenticationService
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ViewCandidateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch the candidate profile', () => {
    expect(candidateService.getProfile).toHaveBeenCalled();
    expect(component.candidate).toEqual(candidate as any);
    expect(component.loading).toBeFalse();
  });

  it('should use the profile tab by default when there is no cached tab', () => {
    expect(component.activeTabId).toBe('Profile');
    expect(location.replaceState).toHaveBeenCalledWith('/profile?tab=Profile');
  });

  it('should update the active tab when the tab changes', () => {
    const event = {
      nextId: 'Tasks'
    } as NgbNavChangeEvent;

    component.onTabChanged(event);

    expect(component.activeTabId).toBe('Tasks');
    expect(localStorageService.set).toHaveBeenCalledWith(
      'CandidateLastTab',
      'Tasks'
    );
    expect(location.replaceState).toHaveBeenCalledWith('/profile?tab=Tasks');
  });

  it('should not show the jobs tab when there are no filtered opportunities', () => {
    expect(component.canSeeJobTab).toBeFalse();
  });

  it('should not show the chat tab when the user cannot view chats', () => {
    expect(component.canSeeChatTab).toBeFalse();
  });
});
