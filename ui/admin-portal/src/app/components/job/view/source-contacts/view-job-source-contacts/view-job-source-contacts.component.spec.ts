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
import {
  HasNameSelectorComponent
} from '../../../../util/has-name-selector/has-name-selector.component';
import {JobChatType} from '../../../../../model/chat';

describe('ViewJobSourceContactsComponent', () => {
  let component: ViewJobSourceContactsComponent;
  let fixture: ComponentFixture<ViewJobSourceContactsComponent>;

  let authenticationService: jasmine.SpyObj<AuthenticationService>;
  let authorizationService: jasmine.SpyObj<AuthorizationService>;
  let chatService: jasmine.SpyObj<ChatService>;
  let modalService: jasmine.SpyObj<NgbModal>;
  let partnerService: jasmine.SpyObj<PartnerService>;
  let userService: jasmine.SpyObj<UserService>;

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
      close: jasmine.createSpy('close'),
      dismiss: jasmine.createSpy('dismiss')
    } as unknown as NgbModalRef;
  }

  beforeEach(async () => {
    authenticationService =
      jasmine.createSpyObj<AuthenticationService>(
        'AuthenticationService',
        ['getLoggedInUser']
      );

    authorizationService =
      jasmine.createSpyObj<AuthorizationService>(
        'AuthorizationService',
        [
          'canViewChats',
          'isReadOnly',
          'isViewingAsSource'
        ]
      );

    chatService = jasmine.createSpyObj<ChatService>(
      'ChatService',
      ['getOrCreate']
    );

    modalService = jasmine.createSpyObj<NgbModal>(
      'NgbModal',
      ['open']
    );

    partnerService = jasmine.createSpyObj<PartnerService>(
      'PartnerService',
      [
        'listSourcePartners',
        'updateJobContact'
      ]
    );

    userService = jasmine.createSpyObj<UserService>(
      'UserService',
      ['search']
    );

    authenticationService.getLoggedInUser.and.returnValue({
      partner: {
        id: loggedInPartner.id
      }
    } as any);

    authorizationService.canViewChats.and.returnValue(false);
    authorizationService.isReadOnly.and.returnValue(false);
    authorizationService.isViewingAsSource.and.returnValue(true);

    partnerService.listSourcePartners.and.returnValue(of([]));

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

    fixture = TestBed.createComponent(
      ViewJobSourceContactsComponent
    );
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
      {...loggedInPartner},
      {...otherPartner}
    ] as any[];

    partnerService.listSourcePartners.and.returnValue(of(partners));

    component.ngOnInit();

    expect(authenticationService.getLoggedInUser)
    .toHaveBeenCalled();

    expect(partnerService.listSourcePartners)
    .toHaveBeenCalledWith(job);

    expect(component.sourcePartners).toEqual(partners);
    expect(component.error).toBeNull();
    expect(component.loading).toBeFalse();
  });

  it('should handle source partner loading failure', () => {
    const error = new Error('load failed');

    partnerService.listSourcePartners.and.returnValue(
      throwError(error)
    );

    component.ngOnInit();

    expect(component.error).toBe(error);
    expect(component.loading).toBeFalse();
  });

  it('should return chat authorization result', () => {
    authorizationService.canViewChats.and.returnValue(true);

    expect(component.canViewChats()).toBeTrue();

    authorizationService.canViewChats.and.returnValue(false);

    expect(component.canViewChats()).toBeFalse();
  });

  it('should fetch a chat for every source partner when chats are viewable', () => {
    const partners = [
      {...loggedInPartner},
      {...otherPartner}
    ] as any[];

    authorizationService.canViewChats.and.returnValue(true);
    chatService.getOrCreate.and.returnValue(
      of({id: 100} as any)
    );

    (component as any).setSourcePartners(partners);

    expect(chatService.getOrCreate.calls.count()).toBe(2);

    expect(chatService.getOrCreate.calls.argsFor(0)[0])
    .toEqual({
      type: JobChatType.JobCreatorSourcePartner,
      jobId: job.id,
      sourcePartnerId: loggedInPartner.id
    });

    expect(partners[0]._jobChat).toEqual({id: 100} as any);
    expect(partners[1]._jobChat).toEqual({id: 100} as any);
  });

  it('should not fetch chats when chats are not viewable', () => {
    authorizationService.canViewChats.and.returnValue(false);

    (component as any).setSourcePartners([
      {...loggedInPartner}
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

    userService.search.and.returnValue(of(users));

    const selectSpy = spyOn<any>(
      component,
      'selectPartnerContactUser'
    );

    component.editPartnerContact(loggedInPartner);

    expect(userService.search).toHaveBeenCalledWith({
      partnerId: loggedInPartner.id,
      sortFields: ['firstName', 'lastName'],
      sortDirection: 'ASC'
    });

    expect(selectSpy).toHaveBeenCalledWith(
      loggedInPartner,
      users
    );

    expect(component.error).toBeNull();
    expect(component.loading).toBeFalse();
  });

  it('should handle partner user search failure', () => {
    const error = new Error('user search failed');

    userService.search.and.returnValue(throwError(error));

    component.editPartnerContact(loggedInPartner);

    expect(component.error).toBe(error);
    expect(component.loading).toBeFalse();
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
    modalService.open.and.returnValue(ref);

    const updateSpy = spyOn<any>(
      component,
      'updateContact'
    );

    (component as any).selectPartnerContactUser(
      loggedInPartner,
      users
    );

    tick();

    expect(users[0].name).toBe('Jane Doe');
    expect(users[1].name).toBe('John Smith');

    expect(modalService.open).toHaveBeenCalledWith(
      HasNameSelectorComponent,
      {
        centered: true,
        backdrop: 'static'
      }
    );

    expect(ref.componentInstance.label)
    .toBe('Select user to contact');

    expect(ref.componentInstance.hasNames)
    .toBe(users);

    expect(updateSpy).toHaveBeenCalledWith(
      loggedInPartner,
      selectedUser
    );
  }));

  it('should ignore user selector dismissal', fakeAsync(() => {
    const users = [
      {
        id: 5,
        firstName: 'Jane',
        lastName: 'Doe'
      }
    ] as any[];

    modalService.open.and.returnValue(
      modalRef(Promise.reject('dismissed'))
    );

    const updateSpy = spyOn<any>(
      component,
      'updateContact'
    );

    (component as any).selectPartnerContactUser(
      loggedInPartner,
      users
    );

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

    partnerService.updateJobContact.and.returnValue(
      of(updatedPartner)
    );

    component.sourcePartners = [
      {...loggedInPartner},
      {...otherPartner}
    ];

    (component as any).updateContact(
      loggedInPartner,
      selectedUser
    );

    expect(partnerService.updateJobContact)
    .toHaveBeenCalledWith(
      loggedInPartner.id,
      {
        jobId: job.id,
        userId: selectedUser.id
      }
    );

    expect(component.sourcePartners[0])
    .toBe(updatedPartner);

    expect(component.error).toBeNull();
    expect(component.loading).toBeFalse();
  });

  it('should handle partner contact update failure', () => {
    const error = new Error('update failed');

    partnerService.updateJobContact.and.returnValue(
      throwError(error)
    );

    (component as any).updateContact(
      loggedInPartner,
      {id: 5} as any
    );

    expect(component.error).toBe(error);
    expect(component.loading).toBeFalse();
  });

  it('should log when updated partner is not found', () => {
    const logSpy = spyOn(console, 'log');

    component.sourcePartners = [
      {...loggedInPartner}
    ];

    (component as any).updateSourcePartners(
      {...otherPartner}
    );

    expect(logSpy).toHaveBeenCalledWith(
      'Bug - partner 20 not found in source partners'
    );
  });

  it('should be editable only for the logged-in source partner', () => {
    authorizationService.isReadOnly.and.returnValue(false);
    authorizationService.isViewingAsSource.and.returnValue(true);

    expect(component.isEditable(loggedInPartner)).toBeTrue();
    expect(component.isEditable(otherPartner)).toBeFalse();
  });

  it('should not be editable in read-only mode', () => {
    authorizationService.isReadOnly.and.returnValue(true);
    authorizationService.isViewingAsSource.and.returnValue(true);

    expect(component.isEditable(loggedInPartner)).toBeFalse();
  });

  it('should not be editable when not viewing as source', () => {
    authorizationService.isReadOnly.and.returnValue(false);
    authorizationService.isViewingAsSource.and.returnValue(false);

    expect(component.isEditable(loggedInPartner)).toBeFalse();
  });

  it('should show read status for selectable partner with a chat', () => {
    component.selectable = true;

    expect(
      component.isShowReadStatus({
        ...otherPartner,
        _jobChat: {id: 1}
      } as any)
    ).toBeTrue();

    expect(
      component.isShowReadStatus({
        ...otherPartner,
        _jobChat: null
      } as any)
    ).toBeFalse();
  });

  it('should show read status only for logged-in partner when not selectable', () => {
    component.selectable = false;

    expect(
      component.isShowReadStatus(loggedInPartner)
    ).toBeTrue();

    expect(
      component.isShowReadStatus(otherPartner)
    ).toBeFalse();
  });

  it('should format source countries in parentheses', () => {
    const partner = {
      ...loggedInPartner,
      sourceCountries: [
        {name: 'Afghanistan'},
        {name: 'Pakistan'}
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

    const emitSpy = spyOn(
      component.sourcePartnerSelection,
      'emit'
    );

    component.selectCurrent(loggedInPartner);

    expect(component.currentSourcePartner)
    .toBe(loggedInPartner);

    expect(emitSpy)
    .toHaveBeenCalledWith(loggedInPartner);
  });

  it('should not select or emit when selection is disabled', () => {
    component.selectable = false;

    const emitSpy = spyOn(
      component.sourcePartnerSelection,
      'emit'
    );

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

    chatService.getOrCreate.and.returnValue(of(chat));

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

    chatService.getOrCreate.and.returnValue(
      throwError(error)
    );

    (component as any).fetchSourcePartnerChat(
      loggedInPartner
    );

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
