/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {
  Component,
  EventEmitter,
  Input,
  Output,
  Pipe,
  PipeTransform,
  SimpleChange
} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {of, Subject, throwError} from 'rxjs';

import {CandidateOppsComponent} from './candidate-opps.component';
import {AuthorizationService} from '../../../../../../services/authorization.service';
import {ChatService} from '../../../../../../services/chat.service';
import {JobChat, JobChatType} from '../../../../../../model/chat';
import {Candidate} from '../../../../../../model/candidate';
import {CandidateOpportunity} from '../../../../../../model/candidate-opportunity';

@Pipe({name: 'translate'})
class TranslatePipeStub implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

@Pipe({name: 'truncate'})
class TruncatePipeStub implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

@Component({selector: 'app-error', template: ''})
class ErrorStubComponent {
  @Input() error: unknown;
}

@Component({selector: 'tc-loading', template: ''})
class TcLoadingStubComponent {
  @Input() loading = false;
}

@Component({selector: 'app-candidate-opp', template: ''})
class CandidateOppStubComponent {
  @Input() selectedOpp: unknown;
  @Input() candidate: unknown;
  @Output() back = new EventEmitter<void>();
}

@Component({
  selector: 'app-tab-header',
  template: '<ng-content></ng-content>'
})
class TabHeaderStubComponent {
}

@Component({selector: 'app-chat-read-status', template: ''})
class ChatReadStatusStubComponent {
  @Input() chats: unknown;
}

@Component({
  selector: 'tc-table',
  template: '<ng-content></ng-content>'
})
class TcTableStubComponent {
  @Input() striped = false;
}

describe('CandidateOppsComponent', () => {
  let component: CandidateOppsComponent;
  let fixture: ComponentFixture<CandidateOppsComponent>;
  let authorizationServiceSpy: jasmine.SpyObj<AuthorizationService>;
  let chatServiceSpy: jasmine.SpyObj<ChatService>;

  const makeCandidate = (
    opportunities: CandidateOpportunity[] = []
  ): Candidate => ({
    id: 7,
    candidateOpportunities: opportunities
  } as Candidate);

  const makeOpp = (
    id = 1,
    name = 'Chef role'
  ): CandidateOpportunity => ({
    id,
    jobOpp: {
      id: id + 10,
      name
    }
  } as CandidateOpportunity);

  const makeChat = (
    id: number,
    type: JobChatType
  ): JobChat => ({
    id,
    type
  } as JobChat);

  beforeEach(async () => {
    authorizationServiceSpy = jasmine.createSpyObj<AuthorizationService>(
      'AuthorizationService',
      ['canViewChats']
    );

    chatServiceSpy = jasmine.createSpyObj<ChatService>(
      'ChatService',
      ['getOrCreate', 'removeDuplicateChats']
    );

    authorizationServiceSpy.canViewChats.and.returnValue(false);

    chatServiceSpy.getOrCreate.and.returnValue(
      of(makeChat(1, JobChatType.CandidateProspect))
    );

    chatServiceSpy.removeDuplicateChats.and.callFake(
      (chats: JobChat[]) => chats
    );

    await TestBed.configureTestingModule({
      declarations: [
        CandidateOppsComponent,
        TranslatePipeStub,
        TruncatePipeStub,
        ErrorStubComponent,
        TcLoadingStubComponent,
        CandidateOppStubComponent,
        TabHeaderStubComponent,
        ChatReadStatusStubComponent,
        TcTableStubComponent
      ],
      providers: [
        {
          provide: AuthorizationService,
          useValue: authorizationServiceSpy
        },
        {
          provide: ChatService,
          useValue: chatServiceSpy
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateOppsComponent);
    component = fixture.componentInstance;
    component.candidate = makeCandidate();
    component.filteredOpps = [makeOpp()];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render tc-loading and tc-table when showing the opp list', () => {
    const loading = fixture.debugElement.query(
      By.directive(TcLoadingStubComponent)
    ).componentInstance as TcLoadingStubComponent;

    const table = fixture.debugElement.query(
      By.directive(TcTableStubComponent)
    ).componentInstance as TcTableStubComponent;

    expect(loading.loading).toBeFalsy();
    expect(table).toBeTruthy();
    expect(table.striped).toBeTrue();

    const rows = fixture.nativeElement.querySelectorAll('tbody tr');

    expect(rows.length).toBe(1);
    expect(rows[0].textContent).toContain('Chef role');
  });

  it('should render the selected opportunity child component after clicking a row', () => {
    const opp = component.filteredOpps[0];

    fixture.nativeElement.querySelector('tbody tr').click();
    fixture.detectChanges();

    const child = fixture.debugElement.query(
      By.directive(CandidateOppStubComponent)
    );

    expect(component.selectedOpp).toBe(opp);
    expect(child).toBeTruthy();

    expect(
      (child.componentInstance as CandidateOppStubComponent).selectedOpp
    ).toBe(opp);

    expect(
      (child.componentInstance as CandidateOppStubComponent).candidate
    ).toBe(component.candidate);
  });

  it('should emit refresh when unSelectOpp is called', () => {
    spyOn(component.refresh, 'emit');
    component.selectedOpp = component.filteredOpps[0];

    component.unSelectOpp();

    expect(component.selectedOpp).toBeNull();
    expect(component.refresh.emit).toHaveBeenCalled();
  });

  it('should expose whether the user can view chats', () => {
    authorizationServiceSpy.canViewChats.and.returnValue(true);

    expect(component.canViewChats).toBeTrue();
    expect(authorizationServiceSpy.canViewChats).toHaveBeenCalled();
  });

  it('should return false when the user cannot view chats', () => {
    authorizationServiceSpy.canViewChats.and.returnValue(false);

    expect(component.canViewChats).toBeFalse();
  });

  it('should select an opportunity', () => {
    const opp = makeOpp(2, 'Developer role');

    component.selectOpp(opp);

    expect(component.selectedOpp).toBe(opp);
  });

  it('should clear the selected opportunity and emit refresh', () => {
    const refreshSpy = spyOn(component.refresh, 'emit');
    component.selectedOpp = makeOpp();

    component.unSelectOpp();

    expect(component.selectedOpp).toBeNull();
    expect(refreshSpy).toHaveBeenCalledTimes(1);
  });

  it('should fetch chats when the candidate changes and chats are viewable', () => {
    const firstOpp = makeOpp(1, 'Chef role');
    const secondOpp = makeOpp(2, 'Developer role');

    component.candidate = makeCandidate([firstOpp, secondOpp]);
    authorizationServiceSpy.canViewChats.and.returnValue(true);

    const fetchOppChatsSpy = spyOn(component, 'fetchOppChats');

    component.ngOnChanges({
      candidate: new SimpleChange(
        undefined,
        component.candidate,
        true
      )
    });

    expect(fetchOppChatsSpy).toHaveBeenCalledTimes(2);
    expect(fetchOppChatsSpy).toHaveBeenCalledWith(firstOpp);
    expect(fetchOppChatsSpy).toHaveBeenCalledWith(secondOpp);
  });

  it('should not fetch chats when the candidate changes but chats are not viewable', () => {
    component.candidate = makeCandidate([makeOpp()]);
    authorizationServiceSpy.canViewChats.and.returnValue(false);

    const fetchOppChatsSpy = spyOn(component, 'fetchOppChats');

    component.ngOnChanges({
      candidate: new SimpleChange(
        undefined,
        component.candidate,
        true
      )
    });

    expect(fetchOppChatsSpy).not.toHaveBeenCalled();
  });

  it('should not fetch chats when a different input changes', () => {
    authorizationServiceSpy.canViewChats.and.returnValue(true);

    const fetchOppChatsSpy = spyOn(component, 'fetchOppChats');

    component.ngOnChanges({
      filteredOpps: new SimpleChange(
        undefined,
        component.filteredOpps,
        true
      )
    });

    expect(fetchOppChatsSpy).not.toHaveBeenCalled();
  });

  it('should make all required chat requests for an opportunity', () => {
    const opp = makeOpp(3, 'Nurse role');

    chatServiceSpy.getOrCreate.calls.reset();

    component.fetchOppChats(opp);

    expect(chatServiceSpy.getOrCreate).toHaveBeenCalledTimes(3);

    expect(chatServiceSpy.getOrCreate).toHaveBeenCalledWith({
      type: JobChatType.CandidateRecruiting,
      candidateId: 7,
      jobId: 13
    });

    expect(chatServiceSpy.getOrCreate).toHaveBeenCalledWith({
      type: JobChatType.CandidateProspect,
      candidateId: 7
    });

    expect(chatServiceSpy.getOrCreate).toHaveBeenCalledWith({
      type: JobChatType.AllJobCandidates,
      jobId: 13
    });
  });

  it('should remove duplicate chats and store them by opportunity ID', () => {
    const opp = makeOpp(3, 'Nurse role');

    const recruitingChat = makeChat(
      1,
      JobChatType.CandidateRecruiting
    );

    const prospectChat = makeChat(
      2,
      JobChatType.CandidateProspect
    );

    const allCandidatesChat = makeChat(
      3,
      JobChatType.AllJobCandidates
    );

    const uniqueChats = [
      recruitingChat,
      prospectChat,
      allCandidatesChat
    ];

    chatServiceSpy.getOrCreate.and.returnValues(
      of(recruitingChat),
      of(prospectChat),
      of(allCandidatesChat)
    );

    chatServiceSpy.removeDuplicateChats.and.returnValue(uniqueChats);

    component.fetchOppChats(opp);

    expect(chatServiceSpy.removeDuplicateChats)
    .toHaveBeenCalledTimes(1);

    expect(chatServiceSpy.removeDuplicateChats)
    .toHaveBeenCalledWith([
      recruitingChat,
      prospectChat,
      allCandidatesChat
    ]);

    expect(component.getOppChats(opp)).toBe(uniqueChats);
    expect(component.loading).toBeFalse();
    expect(component.error).toBeNull();
  });

  it('should return undefined when chats have not been fetched for an opportunity', () => {
    const opp = makeOpp(99, 'Unknown role');

    expect(component.getOppChats(opp)).toBeUndefined();
  });

  it('should set loading while the chat requests are pending', () => {
    const chatSubject = new Subject<JobChat>();

    chatServiceSpy.getOrCreate.and.returnValue(
      chatSubject.asObservable()
    );

    component.error = 'Previous error';

    component.fetchOppChats(makeOpp());

    expect(component.loading).toBeTrue();
    expect(component.error).toBeNull();
  });

  it('should clear a previous error before fetching chats', () => {
    const chatSubject = new Subject<JobChat>();

    chatServiceSpy.getOrCreate.and.returnValue(
      chatSubject.asObservable()
    );

    component.error = 'Previous error';

    component.fetchOppChats(makeOpp());

    expect(component.error).toBeNull();
  });

  it('should store the error and stop loading when fetching chats fails', () => {
    const serviceError = 'Unable to load opportunity chats';

    chatServiceSpy.getOrCreate.and.returnValues(
      of(makeChat(1, JobChatType.CandidateRecruiting)),
      throwError(serviceError),
      of(makeChat(3, JobChatType.AllJobCandidates))
    );

    component.fetchOppChats(makeOpp());

    expect(component.error).toBe(serviceError);
    expect(component.loading).toBeFalse();
    expect(chatServiceSpy.removeDuplicateChats).not.toHaveBeenCalled();
  });

  it('should pass the error to the error component', () => {
    component.error = 'Unable to load chats';
    fixture.detectChanges();

    const errorComponent = fixture.debugElement.query(
      By.directive(ErrorStubComponent)
    ).componentInstance as ErrorStubComponent;

    expect(errorComponent.error).toBe('Unable to load chats');
  });

  it('should pass loading state to the loading component', () => {
    component.loading = true;
    fixture.detectChanges();

    const loadingComponent = fixture.debugElement.query(
      By.directive(TcLoadingStubComponent)
    ).componentInstance as TcLoadingStubComponent;

    expect(loadingComponent.loading).toBeTrue();
  });

  it('should hide the opportunity list while loading', () => {
    component.loading = true;
    fixture.detectChanges();

    const table = fixture.debugElement.query(
      By.directive(TcTableStubComponent)
    );

    const selectedOpp = fixture.debugElement.query(
      By.directive(CandidateOppStubComponent)
    );

    expect(table).toBeNull();
    expect(selectedOpp).toBeNull();
  });

  it('should show the no-results message when there are no filtered opportunities', () => {
    component.loading = false;
    component.selectedOpp = null;
    component.filteredOpps = [];

    fixture.detectChanges();

    const noResultsElement = fixture.debugElement.query(
      By.css('td.text-center')
    );

    expect(noResultsElement).not.toBeNull();
    expect(noResultsElement.nativeElement.textContent)
    .toContain('CANDIDATE-OPPS.TABLE.NONE');
  });

  it('should render chat read status when the user can view chats', () => {
    const opp = component.filteredOpps[0];
    const chats = [
      makeChat(1, JobChatType.CandidateProspect)
    ];

    authorizationServiceSpy.canViewChats.and.returnValue(true);

    chatServiceSpy.getOrCreate.and.returnValue(of(chats[0]));
    chatServiceSpy.removeDuplicateChats.and.returnValue(chats);

    component.fetchOppChats(opp);
    fixture.detectChanges();

    const chatReadStatus = fixture.debugElement.query(
      By.directive(ChatReadStatusStubComponent)
    );

    expect(chatReadStatus).not.toBeNull();

    expect(
      (chatReadStatus.componentInstance as ChatReadStatusStubComponent).chats
    ).toBe(chats);
  });

  it('should hide chat read status when the user cannot view chats', () => {
    authorizationServiceSpy.canViewChats.and.returnValue(false);
    fixture.detectChanges();

    const chatReadStatus = fixture.debugElement.query(
      By.directive(ChatReadStatusStubComponent)
    );

    expect(chatReadStatus).toBeNull();
  });

  it('should unselect the opportunity when the child emits back', () => {
    const refreshSpy = spyOn(component.refresh, 'emit');
    const opp = component.filteredOpps[0];

    component.selectedOpp = opp;
    fixture.detectChanges();

    const child = fixture.debugElement.query(
      By.directive(CandidateOppStubComponent)
    );

    (child.componentInstance as CandidateOppStubComponent).back.emit();
    fixture.detectChanges();

    expect(component.selectedOpp).toBeNull();
    expect(refreshSpy).toHaveBeenCalledTimes(1);
  });
});
