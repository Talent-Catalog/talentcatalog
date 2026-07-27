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
import {fakeAsync, flushMicrotasks, tick} from '@angular/core/testing';
import {Title} from '@angular/platform-browser';
import {ActivatedRoute, convertToParamMap, Router} from '@angular/router';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {of, Subject, throwError} from 'rxjs';

import {MockCandidate} from '../../../MockData/MockCandidate';
import {MockSavedList} from '../../../MockData/MockSavedList';
import {MockUser} from '../../../MockData/MockUser';
import {JobChatType} from '../../../model/chat';
import {Candidate} from '../../../model/candidate';
import {SavedList} from '../../../model/saved-list';
import {AuthorizationService} from '../../../services/authorization.service';
import {AuthenticationService} from '../../../services/authentication.service';
import {CandidateSavedListService} from '../../../services/candidate-saved-list.service';
import {CandidateService} from '../../../services/candidate.service';
import {ChatService} from '../../../services/chat.service';
import {LocalStorageService} from '../../../services/local-storage.service';
import {SavedListCandidateService} from '../../../services/saved-list-candidate.service';
import {SavedListService} from '../../../services/saved-list.service';
import {ViewCandidateComponent} from './view-candidate.component';

describe('ViewCandidateComponent', () => {
  let component: ViewCandidateComponent;
  let candidate: MockCandidate;
  let updatedCandidate$: Subject<Candidate>;
  let candidateService: jasmine.SpyObj<CandidateService>;
  let chatService: jasmine.SpyObj<ChatService>;
  let savedListService: jasmine.SpyObj<SavedListService>;
  let candidateSavedListService: jasmine.SpyObj<CandidateSavedListService>;
  let savedListCandidateService: jasmine.SpyObj<SavedListCandidateService>;
  let localStorageService: jasmine.SpyObj<LocalStorageService>;
  let router: jasmine.SpyObj<Router>;
  let modalService: jasmine.SpyObj<NgbModal>;
  let titleService: jasmine.SpyObj<Title>;
  let authorizationService: jasmine.SpyObj<AuthorizationService>;
  let authenticationService: jasmine.SpyObj<AuthenticationService>;
  let route: {paramMap: any};

  beforeEach(() => {
    candidate = new MockCandidate();
    candidate.user = {
      ...candidate.user,
      firstName: 'Jane',
      lastName: 'Doe'
    };
    updatedCandidate$ = new Subject<Candidate>();

    candidateService = jasmine.createSpyObj<CandidateService>(
      'CandidateService',
      [
        'candidateUpdated',
        'getByNumber',
        'updateStatus',
        'downloadCv',
        'generateToken',
        'updateNotificationPreference'
      ]
    );
    chatService = jasmine.createSpyObj<ChatService>(
      'ChatService',
      ['getCandidateProspectChat', 'create', 'markChatAsRead']
    );
    savedListService = jasmine.createSpyObj<SavedListService>(
      'SavedListService',
      ['searchPaged']
    );
    candidateSavedListService =
      jasmine.createSpyObj<CandidateSavedListService>(
        'CandidateSavedListService',
        ['search', 'replace']
      );
    savedListCandidateService =
      jasmine.createSpyObj<SavedListCandidateService>(
        'SavedListCandidateService',
        ['merge', 'remove']
      );
    localStorageService = jasmine.createSpyObj<LocalStorageService>(
      'LocalStorageService',
      ['get', 'set']
    );
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    modalService = jasmine.createSpyObj<NgbModal>('NgbModal', ['open']);
    titleService = jasmine.createSpyObj<Title>('Title', ['setTitle']);
    authorizationService = jasmine.createSpyObj<AuthorizationService>(
      'AuthorizationService',
      [
        'canViewCandidateCV',
        'isAnAdmin',
        'isEditableCandidate',
        'canEraseCandidateData',
        'canViewPrivateCandidateInfo',
        'canAccessSalesforce',
        'canAccessGoogleDrive',
        'isReadOnly',
        'canSeeGlobalLists',
        'canSeeJobDetails',
        'canViewCandidateName',
        'canViewChats'
      ]
    );
    authenticationService = jasmine.createSpyObj<AuthenticationService>(
      'AuthenticationService',
      ['isGrnInstance', 'getLoggedInUser']
    );
    route = {
      paramMap: of(convertToParamMap({candidateNumber: candidate.candidateNumber}))
    };

    const loggedInUser = new MockUser();
    loggedInUser.partner = {
      ...loggedInUser.partner,
      id: candidate.user.partner?.id,
      defaultSourcePartner: true
    };

    candidateService.candidateUpdated.and.returnValue(
      updatedCandidate$.asObservable()
    );
    candidateService.getByNumber.and.returnValue(of(candidate));
    candidateService.generateToken.and.returnValue(of('token-123'));
    candidateService.updateStatus.and.returnValue(of(null));
    candidateService.updateNotificationPreference.and.returnValue(of(null));
    chatService.getCandidateProspectChat.and.returnValue(
      of({id: 11} as any)
    );
    chatService.create.and.returnValue(of({id: 12} as any));
    savedListService.searchPaged.and.returnValue(
      of({content: [MockSavedList]} as any)
    );
    candidateSavedListService.search.and.returnValue(of([]));
    candidateSavedListService.replace.and.returnValue(of(null));
    savedListCandidateService.merge.and.returnValue(of(null));
    savedListCandidateService.remove.and.returnValue(of(null));
    localStorageService.get.and.returnValue('General');
    authenticationService.isGrnInstance.and.returnValue(true);
    authenticationService.getLoggedInUser.and.returnValue(loggedInUser);
    authorizationService.canSeeGlobalLists.and.returnValue(true);

    component = new ViewCandidateComponent(
      candidateService,
      chatService,
      savedListService,
      candidateSavedListService,
      localStorageService,
      savedListCandidateService,
      route as ActivatedRoute,
      router,
      modalService,
      titleService,
      authorizationService,
      authenticationService
    );
    component.candidate = candidate;
  });

  it('should initialize, load the profile, lists, token, chat, and updated candidate', fakeAsync(() => {
    component.ngOnInit();

    const listResults: SavedList[][] = [];
    component.lists$.subscribe(results => listResults.push(results));
    component.listsInput$.next('job');
    tick(300);

    const refreshed = {
      ...candidate,
      status: 'inactive'
    } as Candidate;
    candidateService.getByNumber.and.returnValue(of(refreshed));
    updatedCandidate$.next(refreshed);

    expect(component.showAspirations).toBeTrue();
    expect(component.loggedInUser).toBe(authenticationService.getLoggedInUser());
    expect(component.activeTabId).toBe('General');
    expect(component.selectedLists).toEqual([]);
    expect(component.token).toBe('token-123');
    expect(component.candidateChat).toEqual({id: 11} as any);
    expect(component.candidateProspectTabVisible).toBeTrue();
    expect(component.uploadedCvAvailable).toBeTrue();
    expect(component.listsLoading).toBeFalse();
    expect(listResults).toEqual([[MockSavedList]]);
    expect(component.candidate).toBe(refreshed);
  }));

  it('should return an empty list when typeahead searching fails', () => {
    savedListService.searchPaged.and.returnValue(
      throwError(() => new Error('Search failed'))
    );

    let result: SavedList[];
    component['doListSearch']('missing').subscribe(value => result = value);

    expect(result).toEqual([]);
  });

  it('should handle a missing candidate', () => {
    candidateService.getByNumber.and.returnValue(of(null));

    component.refreshCandidateProfile();

    expect(component.loadingError).toBeTrue();
    expect(component.error)
    .toBe(`There is no candidate with number: ${candidate.candidateNumber}`);
    expect(component.loading).toBeFalse();
  });

  it('should handle a candidate-loading error', () => {
    const error = new Error('Candidate request failed');
    candidateService.getByNumber.and.returnValue(throwError(error));

    component.refreshCandidateProfile();

    expect(component.loadingError).toBeTrue();
    expect(component.error).toBe(error);
    expect(component.loading).toBeFalse();
  });

  it('should handle an error while loading selected lists', () => {
    const error = new Error('List request failed');
    candidateSavedListService.search.and.returnValue(
      throwError(error)
    );

    component['loadSelectedLists']();

    expect(component.error).toBe(error);
    expect(component.loading).toBeFalse();
  });

  it('should expose the JobChatType enum', () => {
    expect(component.JobChatType).toBe(JobChatType);
  });

  it('should delete a candidate and navigate home', fakeAsync(() => {
    const modalRef = createModalRef(Promise.resolve(true));
    modalService.open.and.returnValue(modalRef);

    component.deleteCandidate();
    flushMicrotasks();

    expect(modalRef.componentInstance.candidate).toBe(candidate);
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  }));

  it('should erase candidate data and navigate home', fakeAsync(() => {
    const modalRef = createModalRef(Promise.resolve(true));
    modalService.open.and.returnValue(modalRef);

    component.eraseCandidateData();
    flushMicrotasks();

    expect(modalRef.componentInstance.candidate).toBe(candidate);
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  }));

  it('should edit and update the candidate status', fakeAsync(() => {
    const info = {status: 'inactive'} as any;
    const originalStatus = candidate.status;
    const modalRef = createModalRef(Promise.resolve(info));
    modalService.open.and.returnValue(modalRef);

    component.editCandidate();
    flushMicrotasks();

    expect(modalRef.componentInstance.candidateStatus).toBe(originalStatus);
    expect(candidateService.updateStatus).toHaveBeenCalledWith({
      candidateIds: [candidate.id],
      info
    });
    expect(component.candidate.status).toBe('inactive');
    expect(component.loading).toBeFalse();
  }));

  it('should handle a status-update error', () => {
    const error = new Error('Status update failed');
    candidateService.updateStatus.and.returnValue(throwError(error));

    component['updateCandidateStatus']({status: 'inactive'} as any);

    expect(component.error).toBe(error);
    expect(component.loading).toBeFalse();
  });

  it('should set a title with and without a candidate name', () => {
    component.setCandidate(candidate);
    expect(titleService.setTitle).toHaveBeenCalledWith(
      `${candidate.user.firstName} ${candidate.user.lastName} ` +
      candidate.candidateNumber
    );

    const candidateWithoutName = {
      ...candidate,
      user: {...candidate.user, firstName: null, lastName: null}
    } as Candidate;
    component.setCandidate(candidateWithoutName);

    expect(titleService.setTitle)
    .toHaveBeenCalledWith(candidate.candidateNumber);
  });

  it('should create a candidate chat', () => {
    component.createChat();

    expect(chatService.create).toHaveBeenCalledWith({
      type: JobChatType.CandidateProspect,
      candidateId: candidate.id
    });
    expect(component.candidateChat).toEqual({id: 12} as any);
    expect(component.loadingButton).toBeFalse();
  });

  it('should handle an error while creating a chat', () => {
    const error = new Error('Chat failed');
    chatService.create.and.returnValue(throwError(error));

    component.createChat();

    expect(component.error).toBe(error);
    expect(component.loadingButton).toBeFalse();
  });

  it('should open the generated-CV modal when names are viewable', () => {
    authorizationService.canViewCandidateName.and.returnValue(true);
    const modalRef = createModalRef(new Promise(() => undefined));
    modalService.open.and.returnValue(modalRef);

    component.downloadGeneratedCV();

    expect(modalRef.componentInstance.candidateId).toBe(candidate.id);
  });

  it('should download an anonymous generated CV', () => {
    authorizationService.canViewCandidateName.and.returnValue(false);
    const blob = new Blob(['cv'], {type: 'application/pdf'});
    candidateService.downloadCv.and.returnValue(of(blob));
    const tab = {location: {href: ''}} as Window;
    spyOn(window, 'open').and.returnValue(tab);
    spyOn(URL, 'createObjectURL').and.returnValue('blob:cv');

    component.downloadGeneratedCV();

    expect(candidateService.downloadCv).toHaveBeenCalledWith({
      candidateId: candidate.id,
      showName: false,
      showContact: false
    });
    expect(tab.location.href).toBe('blob:cv');
  });

  it('should handle an anonymous CV download error', () => {
    authorizationService.canViewCandidateName.and.returnValue(false);
    const error = new Error('Download failed');
    candidateService.downloadCv.and.returnValue(throwError(error));
    spyOn(window, 'open').and.returnValue(
      {location: {href: ''}} as Window
    );

    component.downloadGeneratedCV();

    expect(component.error).toBe(error);
  });

  it('should change tabs and finish loading synchronous tabs', fakeAsync(() => {
    component.activeTabId = 'General';

    component.onTabChanged('Experience');
    tick();

    expect(component.activeTabId).toBe('Experience');
    expect(localStorageService.set)
    .toHaveBeenCalledWith('CandidateLastTab', 'Experience');
    expect(component.tabLoading).toBeFalse();

    localStorageService.set.calls.reset();
    component.onTabChanged('Experience');
    expect(localStorageService.set).not.toHaveBeenCalled();
  }));

  it('should update loading only for the active tab', () => {
    component.activeTabId = 'Visa';

    component.onTabLoadingChange({tabId: 'Visa', loading: true});
    expect(component.tabLoading).toBeTrue();

    component.onTabLoadingChange({tabId: 'General', loading: false});
    expect(component.tabLoading).toBeTrue();
  });

  it('should build the public CV URL for the current environment', () => {
    component.token = 'abc';

    expect(component.publicCvUrl()).toBe(
      `${document.location.protocol}//${document.location.hostname}` +
      '/public-portal/cv/abc'
    );
  });

  it('should delegate authorization checks', () => {
    authorizationService.canViewCandidateCV.and.returnValue(true);
    authorizationService.isAnAdmin.and.returnValue(true);
    authorizationService.isEditableCandidate.and.returnValue(true);
    authorizationService.canEraseCandidateData.and.returnValue(true);
    authorizationService.canViewPrivateCandidateInfo.and.returnValue(true);
    authorizationService.canAccessSalesforce.and.returnValue(true);
    authorizationService.canAccessGoogleDrive.and.returnValue(true);
    authorizationService.isReadOnly.and.returnValue(true);
    authorizationService.canSeeGlobalLists.and.returnValue(true);
    authorizationService.canSeeJobDetails.and.returnValue(true);
    authorizationService.canViewCandidateName.and.returnValue(true);
    authorizationService.canViewChats.and.returnValue(true);

    expect(component.isCVViewable()).toBeTrue();
    expect(component.isAnAdmin()).toBeTrue();
    expect(component.isEditable()).toBeTrue();
    expect(component.canEraseCandidateData()).toBeTrue();
    expect(component.canViewPrivateInfo()).toBeTrue();
    expect(component.canAccessSalesforce()).toBeTrue();
    expect(component.canAccessGoogleDrive()).toBeTrue();
    expect(component.isReadOnlyUser()).toBeTrue();
    expect(component['canSeeGlobalLists']()).toBeTrue();
    expect(component.canSeeJobDetails()).toBeTrue();
    expect(component.canViewCandidateName()).toBeTrue();
    expect(component.canViewChats()).toBeTrue();
  });

  it('should mark the candidate chat as read', () => {
    component.candidateChat = {id: 21} as any;

    component.onMarkCandidateChatAsRead();

    expect(chatService.markChatAsRead)
    .toHaveBeenCalledWith(component.candidateChat);
  });

  it('should select and compare lists', () => {
    const addSpy = spyOn<any>(component, 'addCandidateToList');

    component.onItemSelect({id: '42'});

    expect(addSpy).toHaveBeenCalledWith(42, false);
    expect(component.compareLists({id: 1}, {id: 1})).toBeTrue();
  });

  it('should remove a candidate from a selected list', fakeAsync(() => {
    component.selectedLists = [
      {id: 1, name: 'One'},
      {id: 2, name: 'Two'}
    ] as SavedList[];
    const modalRef = createModalRef(Promise.resolve(true));
    modalService.open.and.returnValue(modalRef);

    component.onItemDeSelect(component.selectedLists[0]);
    flushMicrotasks();

    expect(modalRef.componentInstance.message).toContain('Jane');
    expect(savedListCandidateService.remove).toHaveBeenCalledWith(
      1,
      {candidateIds: [candidate.id]}
    );
    expect(component.selectedLists.map(list => list.id)).toEqual([2]);
    expect(component.savingList).toBeFalse();
  }));

  it('should create a list and add the candidate with reload', fakeAsync(() => {
    const savedList = {id: 9, name: 'New'} as SavedList;
    modalService.open.and.returnValue(
      createModalRef(Promise.resolve(savedList))
    );
    const loadSelectedListsSpy =
      spyOn<any>(component, 'loadSelectedLists');

    component.onNewList();
    flushMicrotasks();

    expect(savedListCandidateService.merge).toHaveBeenCalledWith(
      9,
      {candidateIds: [candidate.id]}
    );
    expect(loadSelectedListsSpy).toHaveBeenCalled();
    expect(component.savingList).toBeFalse();
  }));

  it('should handle an error while adding a candidate to a list', () => {
    const error = new Error('Add failed');
    savedListCandidateService.merge.and.returnValue(
      throwError(error)
    );

    component['addCandidateToList'](5, false);

    expect(component.error).toBe(error);
    expect(component.savingList).toBeFalse();
  });

  it('should replace candidate lists and collect their IDs', () => {
    const lists = [
      {id: 3, name: 'Three'},
      {id: 4, name: 'Four'}
    ] as SavedList[];

    component['setCandidateLists'](lists);

    expect(candidateSavedListService.replace).toHaveBeenCalledWith(
      candidate.id,
      {savedListIds: [3, 4]}
    );
    expect(component.savingList).toBeFalse();
  });

  it('should handle an error while replacing candidate lists', () => {
    const error = new Error('Replace failed');
    candidateSavedListService.replace.and.returnValue(
      throwError(error)
    );

    component['setCandidateLists']([]);

    expect(component.error).toBe(error);
    expect(component.savingList).toBeFalse();
  });

  it('should handle an error while removing a candidate from a list', () => {
    const error = new Error('Remove failed');
    savedListCandidateService.remove.and.returnValue(
      throwError(error)
    );

    component['removeCandidateFromList'](1);

    expect(component.error).toBe(error);
    expect(component.savingList).toBeFalse();
  });

  it('should handle token generation success and error', () => {
    component.generateToken();
    expect(component.token).toBe('token-123');

    const error = new Error('Token failed');
    candidateService.generateToken.and.returnValue(
      throwError(error)
    );
    component.generateToken();
    expect(component.error).toBe(error);
  });

  it('should open a tailored-CV modal with candidate details', () => {
    const modalRef = createModalRef(new Promise(() => undefined));
    modalService.open.and.returnValue(modalRef);

    component.createTailoredCv();

    expect(modalRef.componentInstance.candidateId).toBe(candidate.id);
    expect(modalRef.componentInstance.candidateNumber)
    .toBe(candidate.candidateNumber);
  });

  it('should compute and toggle notification preferences', fakeAsync(() => {
    candidate.allNotifications = false;
    expect(component.computeNotificationButtonLabel())
    .toBe('Notification Opt In');

    const modalRef = createModalRef(Promise.resolve(true));
    modalService.open.and.returnValue(modalRef);
    const toggleSpy =
      spyOn<any>(component, 'doToggleNotificationPreferences');

    component.toggleNotificationPreferences();
    flushMicrotasks();

    expect(modalRef.componentInstance.title)
    .toBe("Override candidate's chat notification preferences?");
    expect(toggleSpy).toHaveBeenCalled();
  }));

  it('should update notification preferences and refresh the profile', () => {
    candidate.allNotifications = false;
    const refreshSpy = spyOn(component, 'refreshCandidateProfile');

    component['doToggleNotificationPreferences']();

    expect(candidateService.updateNotificationPreference)
    .toHaveBeenCalledWith(candidate.id, {allNotifications: true});
    expect(candidate.allNotifications).toBeTrue();
    expect(refreshSpy).toHaveBeenCalled();
  });

  it('should handle a notification-preference update error', () => {
    const error = new Error('Preference failed');
    candidateService.updateNotificationPreference.and.returnValue(
      throwError(error)
    );

    component['doToggleNotificationPreferences']();

    expect(component.error).toBe(error);
  });

  it('should refresh after mute changes and complete cleanup on destroy', () => {
    const refreshSpy = spyOn(component, 'refreshCandidateProfile');
    const destroyNextSpy = spyOn(component['destroy$'], 'next');
    const destroyCompleteSpy = spyOn(component['destroy$'], 'complete');

    component.onMuteToggled();
    component.ngOnDestroy();

    expect(refreshSpy).toHaveBeenCalled();
    expect(destroyNextSpy).toHaveBeenCalled();
    expect(destroyCompleteSpy).toHaveBeenCalled();
  });

  function createModalRef(result: Promise<unknown>): NgbModalRef {
    return {
      componentInstance: {},
      result
    } as NgbModalRef;
  }
});
