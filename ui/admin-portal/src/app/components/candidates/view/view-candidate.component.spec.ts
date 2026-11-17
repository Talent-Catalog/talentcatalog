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
import {fakeAsync, flushMicrotasks, tick} from '@angular/core/testing';
import {Title} from '@angular/platform-browser';
import {ActivatedRoute, convertToParamMap, Router} from '@angular/router';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {of, Subject, throwError} from 'rxjs';

import {MockCandidate} from '../../../MockData/MockCandidate';
import {MockSavedList} from '../../../MockData/MockSavedList';
import {MockUser} from '../../../MockData/MockUser';
import {JobChatType} from '../../../model/chat';
import {Candidate, CandidateStatus, UpdateCandidateStatusInfo} from '../../../model/candidate';
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
import {expect, vi} from 'vitest';

describe('ViewCandidateComponent', () => {
    let component: ViewCandidateComponent;
    let candidate: MockCandidate;
    let updatedCandidate$: Subject<Candidate>;
    let candidateService: MockedObject<CandidateService>;
    let chatService: MockedObject<ChatService>;
    let savedListService: MockedObject<SavedListService>;
    let candidateSavedListService: MockedObject<CandidateSavedListService>;
    let savedListCandidateService: MockedObject<SavedListCandidateService>;
    let localStorageService: MockedObject<LocalStorageService>;
    let router: MockedObject<Router>;
    let modalService: MockedObject<NgbModal>;
    let titleService: MockedObject<Title>;
    let authorizationService: MockedObject<AuthorizationService>;
    let authenticationService: MockedObject<AuthenticationService>;
    let route: {
        paramMap: any;
    };

    beforeEach(() => {
        candidate = new MockCandidate();
        candidate.user = {
            ...candidate.user,
            firstName: 'Jane',
            lastName: 'Doe'
        };
        updatedCandidate$ = new Subject<Candidate>();

        candidateService = ({
            candidateUpdated: vi.fn().mockName("CandidateService.candidateUpdated"),
            getByNumber: vi.fn().mockName("CandidateService.getByNumber"),
            updateStatus: vi.fn().mockName("CandidateService.updateStatus"),
            downloadCv: vi.fn().mockName("CandidateService.downloadCv"),
            generateToken: vi.fn().mockName("CandidateService.generateToken"),
            updateNotificationPreference: vi.fn().mockName("CandidateService.updateNotificationPreference")
        } as unknown as typeof candidateService);
        chatService = ({
            getCandidateProspectChat: vi.fn().mockName("ChatService.getCandidateProspectChat"),
            create: vi.fn().mockName("ChatService.create"),
            markChatAsRead: vi.fn().mockName("ChatService.markChatAsRead")
        } as unknown as typeof chatService);
        savedListService = ({
            searchPaged: vi.fn().mockName("SavedListService.searchPaged")
        } as unknown as typeof savedListService);
        candidateSavedListService = ({
            search: vi.fn().mockName("CandidateSavedListService.search"),
            replace: vi.fn().mockName("CandidateSavedListService.replace")
        } as unknown as typeof candidateSavedListService);
        savedListCandidateService = ({
            merge: vi.fn().mockName("SavedListCandidateService.merge"),
            remove: vi.fn().mockName("SavedListCandidateService.remove")
        } as unknown as typeof savedListCandidateService);
        localStorageService = ({
            get: vi.fn().mockName("LocalStorageService.get"),
            set: vi.fn().mockName("LocalStorageService.set")
        } as unknown as typeof localStorageService);
        router = ({
            navigate: vi.fn().mockName("Router.navigate")
        } as unknown as typeof router);
        modalService = ({
            open: vi.fn().mockName("NgbModal.open")
        } as unknown as typeof modalService);
        titleService = ({
            setTitle: vi.fn().mockName("Title.setTitle")
        } as unknown as typeof titleService);
        authorizationService = ({
            canViewCandidateCV: vi.fn().mockName("AuthorizationService.canViewCandidateCV"),
            isAnAdmin: vi.fn().mockName("AuthorizationService.isAnAdmin"),
            isEditableCandidate: vi.fn().mockName("AuthorizationService.isEditableCandidate"),
            canEraseCandidateData: vi.fn().mockName("AuthorizationService.canEraseCandidateData"),
            canViewPrivateCandidateInfo: vi.fn().mockName("AuthorizationService.canViewPrivateCandidateInfo"),
            canAccessSalesforce: vi.fn().mockName("AuthorizationService.canAccessSalesforce"),
            canAccessGoogleDrive: vi.fn().mockName("AuthorizationService.canAccessGoogleDrive"),
            isReadOnly: vi.fn().mockName("AuthorizationService.isReadOnly"),
            canSeeGlobalLists: vi.fn().mockName("AuthorizationService.canSeeGlobalLists"),
            canSeeJobDetails: vi.fn().mockName("AuthorizationService.canSeeJobDetails"),
            canViewCandidateName: vi.fn().mockName("AuthorizationService.canViewCandidateName"),
            canViewChats: vi.fn().mockName("AuthorizationService.canViewChats")
        } as unknown as typeof authorizationService);
        authenticationService = ({
            isGrnInstance: vi.fn().mockName("AuthenticationService.isGrnInstance"),
            getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser")
        } as unknown as typeof authenticationService);
        route = {
            paramMap: of(convertToParamMap({ candidateNumber: candidate.candidateNumber }))
        };

        const loggedInUser = new MockUser();
        loggedInUser.partner = {
            ...loggedInUser.partner,
            id: candidate.user.partner?.id,
            defaultSourcePartner: true
        };

        candidateService.candidateUpdated.mockReturnValue(updatedCandidate$.asObservable());
        candidateService.getByNumber.mockReturnValue(of(candidate));
        candidateService.generateToken.mockReturnValue(of('token-123'));
        candidateService.updateStatus.mockReturnValue(of(null));
        candidateService.updateNotificationPreference.mockReturnValue(of(null));
        chatService.getCandidateProspectChat.mockReturnValue(of({ id: 11 } as any));
        chatService.create.mockReturnValue(of({ id: 12 } as any));
        savedListService.searchPaged.mockReturnValue(of({ content: [MockSavedList] } as any));
        candidateSavedListService.search.mockReturnValue(of([]));
        candidateSavedListService.replace.mockReturnValue(of(null));
        savedListCandidateService.merge.mockReturnValue(of(null));
        savedListCandidateService.remove.mockReturnValue(of(null));
        localStorageService.get.mockReturnValue('General');
        authenticationService.isGrnInstance.mockReturnValue(true);
        authenticationService.getLoggedInUser.mockReturnValue(loggedInUser);
        authorizationService.canSeeGlobalLists.mockReturnValue(true);

        component = new ViewCandidateComponent(candidateService, chatService, savedListService, candidateSavedListService, localStorageService, savedListCandidateService, route as ActivatedRoute, router, modalService, titleService, authorizationService, authenticationService);
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
        candidateService.getByNumber.mockReturnValue(of(refreshed));
        updatedCandidate$.next(refreshed);

        expect(component.showAspirations).toBe(true);
        expect(component.loggedInUser).toBe(authenticationService.getLoggedInUser());
        expect(component.activeTabId).toBe('General');
        expect(component.selectedLists).toEqual([]);
        expect(component.token).toBe('token-123');
        expect(component.candidateChat).toEqual({ id: 11 } as any);
        expect(component.candidateProspectTabVisible).toBe(true);
        expect(component.uploadedCvAvailable).toBe(true);
        expect(component.listsLoading).toBe(false);
        expect(listResults).toEqual([[MockSavedList]]);
        expect(component.candidate).toBe(refreshed);
    }));

    it('should return an empty list when typeahead searching fails', () => {
        savedListService.searchPaged.mockReturnValue(throwError(() => new Error('Search failed')));

        let result: SavedList[];
        component['doListSearch']('missing').subscribe(value => result = value);

        expect(result).toEqual([]);
    });

    it('should handle a missing candidate', () => {
        candidateService.getByNumber.mockReturnValue(of(null));

        component.refreshCandidateProfile();

        expect(component.loadingError).toBe(true);
        expect(component.error)
            .toBe(`There is no candidate with number: ${candidate.candidateNumber}`);
        expect(component.loading).toBe(false);
    });

    it('should handle a candidate-loading error', () => {
        const error = new Error('Candidate request failed');
        candidateService.getByNumber.mockReturnValue(throwError(error));

        component.refreshCandidateProfile();

        expect(component.loadingError).toBe(true);
        expect(component.error).toBe(error);
        expect(component.loading).toBe(false);
    });

    it('should handle an error while loading selected lists', () => {
        const error = new Error('List request failed');
        candidateSavedListService.search.mockReturnValue(throwError(error));

        component['loadSelectedLists']();

        expect(component.error).toBe(error);
        expect(component.loading).toBe(false);
    });

    it('should expose the JobChatType enum', () => {
        expect(component.JobChatType).toBe(JobChatType);
    });

    it('should delete a candidate and navigate home', fakeAsync(() => {
        const info: UpdateCandidateStatusInfo = { status: CandidateStatus.deleted } as UpdateCandidateStatusInfo;
        const modalRef = createModalRef(Promise.resolve(info));
        modalService.open.mockReturnValue(modalRef);

        component.deleteCandidate();
        flushMicrotasks();

        expect(modalRef.componentInstance.candidateStatus).toBe(CandidateStatus.deleted);
        expect(candidateService.updateStatus).toHaveBeenCalledWith({
            candidateIds: [candidate.id],
            info
        });
    }));

    it('should erase candidate data and navigate home', fakeAsync(() => {
        const modalRef = createModalRef(Promise.resolve(true));
        modalService.open.mockReturnValue(modalRef);

        component.eraseCandidateData();
        flushMicrotasks();

        expect(modalRef.componentInstance.candidate).toBe(candidate);
        expect(router.navigate).toHaveBeenCalledWith(['/']);
    }));

    it('should edit and update the candidate status', fakeAsync(() => {
        const info = { status: 'inactive' } as any;
        const originalStatus = candidate.status;
        const modalRef = createModalRef(Promise.resolve(info));
        modalService.open.mockReturnValue(modalRef);

        component.editCandidateStatus();
        flushMicrotasks();

        expect(modalRef.componentInstance.candidateStatus).toBe(originalStatus);
        expect(candidateService.updateStatus).toHaveBeenCalledWith({
            candidateIds: [candidate.id],
            info
        });
        expect(component.candidate.status).toBe('inactive');
        expect(component.loading).toBe(false);
    }));

    it('should handle a status-update error', () => {
        const error = new Error('Status update failed');
        candidateService.updateStatus.mockReturnValue(throwError(error));

        component['updateCandidateStatus']({ status: 'inactive' } as any);

        expect(component.error).toBe(error);
        expect(component.loading).toBe(false);
    });

    it('should set a title with and without a candidate name', () => {
        component.setCandidate(candidate);
        expect(titleService.setTitle).toHaveBeenCalledWith(`${candidate.user.firstName} ${candidate.user.lastName} ` +
            candidate.candidateNumber);

        const candidateWithoutName = {
            ...candidate,
            user: { ...candidate.user, firstName: null, lastName: null }
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
        expect(component.candidateChat).toEqual({ id: 12 } as any);
        expect(component.loadingButton).toBe(false);
    });

    it('should handle an error while creating a chat', () => {
        const error = new Error('Chat failed');
        chatService.create.mockReturnValue(throwError(error));

        component.createChat();

        expect(component.error).toBe(error);
        expect(component.loadingButton).toBe(false);
    });

    it('should open the generated-CV modal when names are viewable', () => {
        authorizationService.canViewCandidateName.mockReturnValue(true);
        const modalRef = createModalRef(new Promise(() => undefined));
        modalService.open.mockReturnValue(modalRef);

        component.downloadGeneratedCV();

        expect(modalRef.componentInstance.candidateId).toBe(candidate.id);
    });

    it('should download an anonymous generated CV', () => {
        authorizationService.canViewCandidateName.mockReturnValue(false);
        const blob = new Blob(['cv'], { type: 'application/pdf' });
        candidateService.downloadCv.mockReturnValue(of(blob));
        const tab = { location: { href: '' } } as Window;
        vi.spyOn(window, 'open').mockReturnValue(tab);
        vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:cv');

        component.downloadGeneratedCV();

        expect(candidateService.downloadCv).toHaveBeenCalledWith({
            candidateId: candidate.id,
            showName: false,
            showContact: false
        });
        expect(tab.location.href).toBe('blob:cv');
    });

    it('should handle an anonymous CV download error', () => {
        authorizationService.canViewCandidateName.mockReturnValue(false);
        const error = new Error('Download failed');
        candidateService.downloadCv.mockReturnValue(throwError(error));
        vi.spyOn(window, 'open').mockReturnValue({ location: { href: '' } } as Window);

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
        expect(component.tabLoading).toBe(false);

        localStorageService.set.mockClear();
        component.onTabChanged('Experience');
        expect(localStorageService.set).not.toHaveBeenCalled();
    }));

    it('should update loading only for the active tab', () => {
        component.activeTabId = 'Visa';

        component.onTabLoadingChange({ tabId: 'Visa', loading: true });
        expect(component.tabLoading).toBe(true);

        component.onTabLoadingChange({ tabId: 'General', loading: false });
        expect(component.tabLoading).toBe(true);
    });

    it('should build the public CV URL for the current environment', () => {
        component.token = 'abc';

        expect(component.publicCvUrl()).toBe(`${document.location.protocol}//${document.location.hostname}` +
            '/public-portal/cv/abc');
    });

    it('should delegate authorization checks', () => {
        authorizationService.canViewCandidateCV.mockReturnValue(true);
        authorizationService.isAnAdmin.mockReturnValue(true);
        authorizationService.isEditableCandidate.mockReturnValue(true);
        authorizationService.canEraseCandidateData.mockReturnValue(true);
        authorizationService.canViewPrivateCandidateInfo.mockReturnValue(true);
        authorizationService.canAccessSalesforce.mockReturnValue(true);
        authorizationService.canAccessGoogleDrive.mockReturnValue(true);
        authorizationService.isReadOnly.mockReturnValue(true);
        authorizationService.canSeeGlobalLists.mockReturnValue(true);
        authorizationService.canSeeJobDetails.mockReturnValue(true);
        authorizationService.canViewCandidateName.mockReturnValue(true);
        authorizationService.canViewChats.mockReturnValue(true);

        expect(component.isCVViewable()).toBe(true);
        expect(component.isAnAdmin()).toBe(true);
        expect(component.isEditable()).toBe(true);
        expect(component.canEraseCandidateData()).toBe(true);
        expect(component.canViewPrivateInfo()).toBe(true);
        expect(component.canAccessSalesforce()).toBe(true);
        expect(component.canAccessGoogleDrive()).toBe(true);
        expect(component.isReadOnlyUser()).toBe(true);
        expect(component['canSeeGlobalLists']()).toBe(true);
        expect(component.canSeeJobDetails()).toBe(true);
        expect(component.canViewCandidateName()).toBe(true);
        expect(component.canViewChats()).toBe(true);
    });

    it('should only allow marking a candidate as deleted when editable and not already deleted', () => {
        authorizationService.isEditableCandidate.mockReturnValue(true);
        candidate.status = 'active';
        expect(component.canMarkAsDeleted()).toBe(true);

        authorizationService.isEditableCandidate.mockReturnValue(false);
        candidate.status = 'active';
        expect(component.canMarkAsDeleted()).toBe(false);

        authorizationService.isEditableCandidate.mockReturnValue(true);
        candidate.status = 'deleted';
        expect(component.canMarkAsDeleted()).toBe(false);
    });

    it('should mark the candidate chat as read', () => {
        component.candidateChat = { id: 21 } as any;

        component.onMarkCandidateChatAsRead();

        expect(chatService.markChatAsRead)
            .toHaveBeenCalledWith(component.candidateChat);
    });

    it('should select and compare lists', () => {
        const addSpy = vi.spyOn((component as any), 'addCandidateToList').mockReturnValue(undefined);

        component.onItemSelect({ id: '42' });

        expect(addSpy).toHaveBeenCalledWith(42, false);
        expect(component.compareLists({ id: 1 }, { id: 1 })).toBe(true);
    });

    it('should remove a candidate from a selected list', fakeAsync(() => {
        component.selectedLists = [
            { id: 1, name: 'One' },
            { id: 2, name: 'Two' }
        ] as SavedList[];
        const modalRef = createModalRef(Promise.resolve(true));
        modalService.open.mockReturnValue(modalRef);

        component.onItemDeSelect(component.selectedLists[0]);
        flushMicrotasks();

        expect(modalRef.componentInstance.message).toContain('Jane');
        expect(savedListCandidateService.remove).toHaveBeenCalledWith(1, { candidateIds: [candidate.id] });
        expect(component.selectedLists.map(list => list.id)).toEqual([2]);
        expect(component.savingList).toBe(false);
    }));

    it('should create a list and add the candidate with reload', fakeAsync(() => {
        const savedList = { id: 9, name: 'New' } as SavedList;
        modalService.open.mockReturnValue(createModalRef(Promise.resolve(savedList)));
        const loadSelectedListsSpy = vi.spyOn((component as any), 'loadSelectedLists').mockReturnValue(undefined);

        component.onNewList();
        flushMicrotasks();

        expect(savedListCandidateService.merge).toHaveBeenCalledWith(9, { candidateIds: [candidate.id] });
        expect(loadSelectedListsSpy).toHaveBeenCalled();
        expect(component.savingList).toBe(false);
    }));

    it('should handle an error while adding a candidate to a list', () => {
        const error = new Error('Add failed');
        savedListCandidateService.merge.mockReturnValue(throwError(error));

        component['addCandidateToList'](5, false);

        expect(component.error).toBe(error);
        expect(component.savingList).toBe(false);
    });

    it('should replace candidate lists and collect their IDs', () => {
        const lists = [
            { id: 3, name: 'Three' },
            { id: 4, name: 'Four' }
        ] as SavedList[];

        component['setCandidateLists'](lists);

        expect(candidateSavedListService.replace).toHaveBeenCalledWith(candidate.id, { savedListIds: [3, 4] });
        expect(component.savingList).toBe(false);
    });

    it('should handle an error while replacing candidate lists', () => {
        const error = new Error('Replace failed');
        candidateSavedListService.replace.mockReturnValue(throwError(error));

        component['setCandidateLists']([]);

        expect(component.error).toBe(error);
        expect(component.savingList).toBe(false);
    });

    it('should handle an error while removing a candidate from a list', () => {
        const error = new Error('Remove failed');
        savedListCandidateService.remove.mockReturnValue(throwError(error));

        component['removeCandidateFromList'](1);

        expect(component.error).toBe(error);
        expect(component.savingList).toBe(false);
    });

    it('should handle token generation success and error', () => {
        component.generateToken();
        expect(component.token).toBe('token-123');

        const error = new Error('Token failed');
        candidateService.generateToken.mockReturnValue(throwError(error));
        component.generateToken();
        expect(component.error).toBe(error);
    });

    it('should open a tailored-CV modal with candidate details', () => {
        const modalRef = createModalRef(new Promise(() => undefined));
        modalService.open.mockReturnValue(modalRef);

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
        modalService.open.mockReturnValue(modalRef);
        const toggleSpy = vi.spyOn((component as any), 'doToggleNotificationPreferences').mockReturnValue(undefined);

        component.toggleNotificationPreferences();
        flushMicrotasks();

        expect(modalRef.componentInstance.title)
            .toBe("Override candidate's chat notification preferences?");
        expect(toggleSpy).toHaveBeenCalled();
    }));

    it('should update notification preferences and refresh the profile', () => {
        candidate.allNotifications = false;
        const refreshSpy = vi.spyOn(component, 'refreshCandidateProfile').mockReturnValue(undefined);

        component['doToggleNotificationPreferences']();

        expect(candidateService.updateNotificationPreference)
            .toHaveBeenCalledWith(candidate.id, { allNotifications: true });
        expect(candidate.allNotifications).toBe(true);
        expect(refreshSpy).toHaveBeenCalled();
    });

    it('should handle a notification-preference update error', () => {
        const error = new Error('Preference failed');
        candidateService.updateNotificationPreference.mockReturnValue(throwError(error));

        component['doToggleNotificationPreferences']();

        expect(component.error).toBe(error);
    });

    it('should refresh after mute changes and complete cleanup on destroy', () => {
        const refreshSpy = vi.spyOn(component, 'refreshCandidateProfile').mockReturnValue(undefined);
        const destroyNextSpy = vi.spyOn(component['destroy$'], 'next').mockReturnValue(undefined);
        const destroyCompleteSpy = vi.spyOn(component['destroy$'], 'complete').mockReturnValue(undefined);

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
