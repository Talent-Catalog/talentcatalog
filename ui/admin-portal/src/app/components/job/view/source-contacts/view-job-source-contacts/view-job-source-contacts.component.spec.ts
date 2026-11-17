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
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {of, throwError} from 'rxjs';

import {ViewJobSourceContactsComponent} from './view-job-source-contacts.component';
import {PartnerService} from '../../../../../services/partner.service';
import {UserService} from '../../../../../services/user.service';
import {AuthenticationService} from '../../../../../services/authentication.service';
import {ChatService} from '../../../../../services/chat.service';
import {AuthorizationService} from '../../../../../services/authorization.service';
import {HasNameSelectorComponent} from '../../../../util/has-name-selector/has-name-selector.component';
import {JobChatType} from '../../../../../model/chat';
import {expect, vi} from 'vitest';

describe('ViewJobSourceContactsComponent', () => {
    let component: ViewJobSourceContactsComponent;
    let fixture: ComponentFixture<ViewJobSourceContactsComponent>;

    let authenticationService: MockedObject<AuthenticationService>;
    let authorizationService: MockedObject<AuthorizationService>;
    let chatService: MockedObject<ChatService>;
    let modalService: MockedObject<NgbModal>;
    let partnerService: MockedObject<PartnerService>;
    let userService: MockedObject<UserService>;

    const job = {
        id: 99
    } as any;

    const loggedInPartner = {
        id: 10,
        name: 'Logged-in partner',
        sourceCountries: []
    } as any;

    const otherPartner = {
        id: 20,
        name: 'Other partner',
        sourceCountries: []
    } as any;

    function modalRef(result: Promise<any>): NgbModalRef {
        return {
            componentInstance: {},
            result,
            close: vi.fn().mockName('close'),
            dismiss: vi.fn().mockName('dismiss')
        } as unknown as NgbModalRef;
    }

    beforeEach(async () => {
        authenticationService = ({
            getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser")
        } as unknown as typeof authenticationService);

        authorizationService = ({
            canViewChats: vi.fn().mockName("AuthorizationService.canViewChats"),
            isReadOnly: vi.fn().mockName("AuthorizationService.isReadOnly"),
            isViewingAsSource: vi.fn().mockName("AuthorizationService.isViewingAsSource")
        } as unknown as typeof authorizationService);

        chatService = ({
            getOrCreate: vi.fn().mockName("ChatService.getOrCreate")
        } as unknown as typeof chatService);

        modalService = ({
            open: vi.fn().mockName("NgbModal.open")
        } as unknown as typeof modalService);

        partnerService = ({
            listSourcePartners: vi.fn().mockName("PartnerService.listSourcePartners"),
            updateJobContact: vi.fn().mockName("PartnerService.updateJobContact")
        } as unknown as typeof partnerService);

        userService = ({
            search: vi.fn().mockName("UserService.search")
        } as unknown as typeof userService);

        authenticationService.getLoggedInUser.mockReturnValue({
            partner: {
                id: loggedInPartner.id
            }
        } as any);

        authorizationService.canViewChats.mockReturnValue(false);
        authorizationService.isReadOnly.mockReturnValue(false);
        authorizationService.isViewingAsSource.mockReturnValue(true);

        partnerService.listSourcePartners.mockReturnValue(of([]));

        await TestBed.configureTestingModule({
            declarations: [ViewJobSourceContactsComponent],
            providers: [
                {
                    provide: AuthenticationService,
                    useValue: authenticationService
                },
                {
                    provide: AuthorizationService,
                    useValue: authorizationService
                },
                {
                    provide: ChatService,
                    useValue: chatService
                },
                {
                    provide: NgbModal,
                    useValue: modalService
                },
                {
                    provide: PartnerService,
                    useValue: partnerService
                },
                {
                    provide: UserService,
                    useValue: userService
                }
            ],
            schemas: [NO_ERRORS_SCHEMA]
        })
            .overrideTemplate(ViewJobSourceContactsComponent, '')
            .compileComponents();

        fixture = TestBed.createComponent(ViewJobSourceContactsComponent);
        component = fixture.componentInstance;
        component.job = job;
        component.selectable = false;

        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should load source partners on init', () => {
        const partners = [
            { ...loggedInPartner },
            { ...otherPartner }
        ] as any[];

        partnerService.listSourcePartners.mockReturnValue(of(partners));

        component.ngOnInit();

        expect(authenticationService.getLoggedInUser)
            .toHaveBeenCalled();

        expect(partnerService.listSourcePartners)
            .toHaveBeenCalledWith(job);

        expect(component.sourcePartners).toEqual(partners);
        expect(component.error).toBeNull();
        expect(component.loading).toBe(false);
    });

    it('should handle source partner loading failure', () => {
        const error = new Error('load failed');

        partnerService.listSourcePartners.mockReturnValue(throwError(error));

        component.ngOnInit();

        expect(component.error).toBe(error);
        expect(component.loading).toBe(false);
    });

    it('should return chat authorization result', () => {
        authorizationService.canViewChats.mockReturnValue(true);

        expect(component.canViewChats()).toBe(true);

        authorizationService.canViewChats.mockReturnValue(false);

        expect(component.canViewChats()).toBe(false);
    });

    it('should fetch a chat for every source partner when chats are viewable', () => {
        const partners = [
            { ...loggedInPartner },
            { ...otherPartner }
        ] as any[];

        authorizationService.canViewChats.mockReturnValue(true);
        chatService.getOrCreate.mockReturnValue(of({ id: 100 } as any));

        (component as any).setSourcePartners(partners);

        expect(vi.mocked(chatService.getOrCreate).mock.calls.length).toBe(2);

        expect(vi.mocked(chatService.getOrCreate).mock.calls[0][0])
            .toEqual({
            type: JobChatType.JobCreatorSourcePartner,
            jobId: job.id,
            sourcePartnerId: loggedInPartner.id
        });

        expect(partners[0]._jobChat).toEqual({ id: 100 } as any);
        expect(partners[1]._jobChat).toEqual({ id: 100 } as any);
    });

    it('should not fetch chats when chats are not viewable', () => {
        authorizationService.canViewChats.mockReturnValue(false);

        (component as any).setSourcePartners([
            { ...loggedInPartner }
        ]);

        expect(chatService.getOrCreate).not.toHaveBeenCalled();
    });

    it('should search partner users before editing a contact', () => {
        const users = [
            {
                id: 1,
                firstName: 'Jane',
                lastName: 'Doe'
            }
        ] as any[];

        userService.search.mockReturnValue(of(users));

        const selectSpy = vi.spyOn((component as any), 'selectPartnerContactUser').mockReturnValue(undefined);

        component.editPartnerContact(loggedInPartner);

        expect(userService.search).toHaveBeenCalledWith({
            partnerId: loggedInPartner.id,
            sortFields: ['firstName', 'lastName'],
            sortDirection: 'ASC'
        });

        expect(selectSpy).toHaveBeenCalledWith(loggedInPartner, users);

        expect(component.error).toBeNull();
        expect(component.loading).toBe(false);
    });

    it('should handle partner user search failure', () => {
        const error = new Error('user search failed');

        userService.search.mockReturnValue(throwError(error));

        component.editPartnerContact(loggedInPartner);

        expect(component.error).toBe(error);
        expect(component.loading).toBe(false);
    });

    it('should open the user selector and update the selected contact', fakeAsync(() => {
        const selectedUser = {
            id: 5,
            firstName: 'Jane',
            lastName: 'Doe'
        } as any;

        const users = [
            selectedUser,
            {
                id: 6,
                firstName: 'John',
                lastName: 'Smith'
            }
        ] as any[];

        const ref = modalRef(Promise.resolve(selectedUser));
        modalService.open.mockReturnValue(ref);

        const updateSpy = vi.spyOn((component as any), 'updateContact').mockReturnValue(undefined);

        (component as any).selectPartnerContactUser(loggedInPartner, users);

        tick();

        expect(users[0].name).toBe('Jane Doe');
        expect(users[1].name).toBe('John Smith');

        expect(modalService.open).toHaveBeenCalledWith(HasNameSelectorComponent, {
            centered: true,
            backdrop: 'static'
        });

        expect(ref.componentInstance.label)
            .toBe('Select user to contact');

        expect(ref.componentInstance.hasNames)
            .toBe(users);

        expect(updateSpy).toHaveBeenCalledWith(loggedInPartner, selectedUser);
    }));

    it('should ignore user selector dismissal', fakeAsync(() => {
        const users = [
            {
                id: 5,
                firstName: 'Jane',
                lastName: 'Doe'
            }
        ] as any[];

        modalService.open.mockReturnValue(modalRef(Promise.reject('dismissed')));

        const updateSpy = vi.spyOn((component as any), 'updateContact').mockReturnValue(undefined);

        (component as any).selectPartnerContactUser(loggedInPartner, users);

        tick();

        expect(updateSpy).not.toHaveBeenCalled();
    }));

    it('should update a partner job contact successfully', () => {
        const selectedUser = {
            id: 5
        } as any;

        const updatedPartner = {
            ...loggedInPartner,
            jobContact: selectedUser
        } as any;

        partnerService.updateJobContact.mockReturnValue(of(updatedPartner));

        component.sourcePartners = [
            { ...loggedInPartner },
            { ...otherPartner }
        ];

        (component as any).updateContact(loggedInPartner, selectedUser);

        expect(partnerService.updateJobContact)
            .toHaveBeenCalledWith(loggedInPartner.id, {
            jobId: job.id,
            userId: selectedUser.id
        });

        expect(component.sourcePartners[0])
            .toBe(updatedPartner);

        expect(component.error).toBeNull();
        expect(component.loading).toBe(false);
    });

    it('should handle partner contact update failure', () => {
        const error = new Error('update failed');

        partnerService.updateJobContact.mockReturnValue(throwError(error));

        (component as any).updateContact(loggedInPartner, { id: 5 } as any);

        expect(component.error).toBe(error);
        expect(component.loading).toBe(false);
    });

    it('should log when updated partner is not found', () => {
        const logSpy = vi.spyOn(console, 'log').mockReturnValue(undefined);

        component.sourcePartners = [
            { ...loggedInPartner }
        ];

        (component as any).updateSourcePartners({ ...otherPartner });

        expect(logSpy).toHaveBeenCalledWith('Bug - partner 20 not found in source partners');
    });

    it('should be editable only for the logged-in source partner', () => {
        authorizationService.isReadOnly.mockReturnValue(false);
        authorizationService.isViewingAsSource.mockReturnValue(true);

        expect(component.isEditable(loggedInPartner)).toBe(true);
        expect(component.isEditable(otherPartner)).toBe(false);
    });

    it('should not be editable in read-only mode', () => {
        authorizationService.isReadOnly.mockReturnValue(true);
        authorizationService.isViewingAsSource.mockReturnValue(true);

        expect(component.isEditable(loggedInPartner)).toBe(false);
    });

    it('should not be editable when not viewing as source', () => {
        authorizationService.isReadOnly.mockReturnValue(false);
        authorizationService.isViewingAsSource.mockReturnValue(false);

        expect(component.isEditable(loggedInPartner)).toBe(false);
    });

    it('should show read status for selectable partner with a chat', () => {
        component.selectable = true;

        expect(component.isShowReadStatus({
            ...otherPartner,
            _jobChat: { id: 1 }
        } as any)).toBe(true);

        expect(component.isShowReadStatus({
            ...otherPartner,
            _jobChat: null
        } as any)).toBe(false);
    });

    it('should show read status only for logged-in partner when not selectable', () => {
        component.selectable = false;

        expect(component.isShowReadStatus(loggedInPartner)).toBe(true);

        expect(component.isShowReadStatus(otherPartner)).toBe(false);
    });

    it('should format source countries in parentheses', () => {
        const partner = {
            ...loggedInPartner,
            sourceCountries: [
                { name: 'Afghanistan' },
                { name: 'Pakistan' }
            ]
        } as any;

        expect(component.sourceCountries(partner))
            .toBe('(Afghanistan, Pakistan)');
    });

    it('should return an empty string when source countries are absent', () => {
        const partner = {
            ...loggedInPartner,
            sourceCountries: []
        } as any;

        expect(component.sourceCountries(partner)).toBe('');
    });

    it('should emit sourcePartnerSelection event when a source partner is selected', () => {
        component.selectable = true;
        component.sourcePartners = [loggedInPartner];

        const emitSpy = vi.spyOn(component.sourcePartnerSelection, 'emit').mockReturnValue(undefined);

        component.selectCurrent(loggedInPartner);

        expect(component.currentSourcePartner)
            .toBe(loggedInPartner);

        expect(emitSpy)
            .toHaveBeenCalledWith(loggedInPartner);
    });

    it('should not select or emit when selection is disabled', () => {
        component.selectable = false;

        const emitSpy = vi.spyOn(component.sourcePartnerSelection, 'emit').mockReturnValue(undefined);

        component.selectCurrent(loggedInPartner);

        expect(component.currentSourcePartner)
            .toBeUndefined();

        expect(emitSpy).not.toHaveBeenCalled();
    });

    it('should fetch and assign a source partner chat', () => {
        const partner = {
            ...loggedInPartner
        } as any;

        const chat = {
            id: 100
        } as any;

        chatService.getOrCreate.mockReturnValue(of(chat));

        (component as any).fetchSourcePartnerChat(partner);

        expect(chatService.getOrCreate).toHaveBeenCalledWith({
            type: JobChatType.JobCreatorSourcePartner,
            jobId: job.id,
            sourcePartnerId: partner.id
        });

        expect(partner._jobChat).toBe(chat);
        expect(component.error).toBeNull();
    });

    it('should handle source partner chat failure', () => {
        const error = new Error('chat failed');

        chatService.getOrCreate.mockReturnValue(throwError(error));

        (component as any).fetchSourcePartnerChat(loggedInPartner);

        expect(component.error).toBe(error);
    });

    it('should return job contact when present', () => {
        const jobContact = {
            id: 1,
            firstName: 'Job',
            lastName: 'Contact'
        } as any;

        const partner = {
            ...loggedInPartner,
            jobContact,
            defaultContact: {
                id: 2
            }
        } as any;

        expect(component.jobContact(partner))
            .toBe(jobContact);
    });

    it('should fall back to default contact', () => {
        const defaultContact = {
            id: 2,
            firstName: 'Default',
            lastName: 'Contact'
        } as any;

        const partner = {
            ...loggedInPartner,
            jobContact: null,
            defaultContact
        } as any;

        expect(component.jobContact(partner))
            .toBe(defaultContact);
    });
});
