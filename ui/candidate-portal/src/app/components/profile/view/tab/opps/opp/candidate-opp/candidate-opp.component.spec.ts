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

import {CandidateOppComponent} from './candidate-opp.component';
import {AuthorizationService} from '../../../../../../../services/authorization.service';
import {ChatService} from '../../../../../../../services/chat.service';
import {JobChat, JobChatType} from '../../../../../../../model/chat';
import {Candidate} from '../../../../../../../model/candidate';
import {CandidateOpportunity} from '../../../../../../../model/candidate-opportunity';

@Pipe({name: 'translate'})
class TranslatePipeStub implements PipeTransform {
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

@Component({
  selector: 'tc-button',
  template: `
    <button (click)="onClick.emit()">
      <ng-content></ng-content>
    </button>
  `
})
class TcButtonStubComponent {
  @Input() size?: string;
  @Input() type?: string;
  @Input() color?: string;
  @Output() onClick = new EventEmitter<void>();
}

@Component({
  selector: 'tc-label',
  template: '<label><ng-content></ng-content></label>'
})
class TcLabelStubComponent {
  @Input() for?: string;
}

@Component({
  selector: 'tc-table',
  template: '<ng-content></ng-content>'
})
class TcTableStubComponent {
  @Input() striped = false;
}

@Component({selector: 'app-view-chat-posts', template: ''})
class ViewChatPostsStubComponent {
  @Input() chat: unknown;
  @Input() readOnly = false;
}

@Component({selector: 'app-chat-read-status', template: ''})
class ChatReadStatusStubComponent {
  @Input() chats: unknown;
}

describe('CandidateOppComponent', () => {
  let component: CandidateOppComponent;
  let fixture: ComponentFixture<CandidateOppComponent>;
  let authorizationServiceSpy: jasmine.SpyObj<AuthorizationService>;
  let chatServiceSpy: jasmine.SpyObj<ChatService>;

  const makeCandidate = (): Candidate => ({
    id: 7,
    muted: false
  } as Candidate);

  const makeOpp = (
    lastActiveStage = 'offer'
  ): CandidateOpportunity => ({
    id: 11,
    stage: 'offer',
    lastActiveStage,
    fileOfferLink: 'https://example.com/offer.pdf',
    fileOfferName: 'Offer PDF',
    closingCommentsForCandidate: 'Helpful notes',
    jobOpp: {
      id: 22,
      name: 'Mountain hotel role',
      submissionList: {
        fileInterviewGuidanceLink: 'https://example.com/interview.pdf',
        fileInterviewGuidanceName: 'Interview guide'
      }
    }
  } as unknown as CandidateOpportunity);

  const makeChat = (
    id: number,
    type: JobChatType
  ): JobChat => ({
    id,
    type
  } as JobChat);

  beforeEach(async () => {
    authorizationServiceSpy =
      jasmine.createSpyObj<AuthorizationService>(
        'AuthorizationService',
        ['canViewChats']
      );

    chatServiceSpy = jasmine.createSpyObj<ChatService>(
      'ChatService',
      ['getOrCreate', 'markChatAsRead']
    );

    authorizationServiceSpy.canViewChats.and.returnValue(true);

    chatServiceSpy.getOrCreate.and.callFake(
      (request: any) => of(
        makeChat(
          Number(request.type) + 1,
          request.type
        )
      )
    );

    await TestBed.configureTestingModule({
      declarations: [
        CandidateOppComponent,
        TranslatePipeStub,
        ErrorStubComponent,
        TcLoadingStubComponent,
        TcButtonStubComponent,
        TcLabelStubComponent,
        TcTableStubComponent,
        ViewChatPostsStubComponent,
        ChatReadStatusStubComponent
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
    fixture = TestBed.createComponent(CandidateOppComponent);
    component = fixture.componentInstance;
    component.candidate = makeCandidate();
    component.selectedOpp = makeOpp();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render tc-loading, tc-button, tc-label and tc-table for opp details', () => {
    expect(
      fixture.debugElement.query(
        By.directive(TcLoadingStubComponent)
      )
    ).toBeTruthy();

    const table = fixture.debugElement.query(
      By.directive(TcTableStubComponent)
    ).componentInstance as TcTableStubComponent;

    expect(table).toBeTruthy();
    expect(table.striped).toBeTrue();

    const buttons = fixture.debugElement.queryAll(
      By.directive(TcButtonStubComponent)
    );

    const labels = fixture.debugElement.queryAll(
      By.directive(TcLabelStubComponent)
    );

    expect(buttons.length).toBe(1);

    expect(
      (buttons[0].componentInstance as TcButtonStubComponent).size
    ).toBe('sm');

    expect(labels.length).toBe(4);

    expect(fixture.nativeElement.textContent)
    .toContain('Mountain hotel role');
  });

  it('should render the selected chat view with tc-button return actions', () => {
    component.selectedChatType = JobChatType.CandidateProspect;

    component.selectedChat = makeChat(
      99,
      JobChatType.CandidateProspect
    );

    fixture.detectChanges();

    const chatView = fixture.debugElement.query(
      By.directive(ViewChatPostsStubComponent)
    );

    const buttons = fixture.debugElement.queryAll(
      By.directive(TcButtonStubComponent)
    );

    expect(chatView).toBeTruthy();

    expect(
      (chatView.componentInstance as ViewChatPostsStubComponent).chat
    ).toBe(component.selectedChat);

    expect(
      (chatView.componentInstance as ViewChatPostsStubComponent)
        .readOnly
    ).toBeFalse();

    expect(buttons.length).toBe(2);
  });

  it('should emit back when goBack is called', () => {
    spyOn(component.back, 'emit');

    component.goBack();

    expect(component.selectedOpp).toBeNull();
    expect(component.back.emit).toHaveBeenCalled();
  });

  it('should expose whether the user can view chats', () => {
    authorizationServiceSpy.canViewChats.and.returnValue(true);

    expect(component.canViewChats).toBeTrue();
    expect(
      authorizationServiceSpy.canViewChats
    ).toHaveBeenCalled();
  });

  it('should return false when the user cannot view chats', () => {
    authorizationServiceSpy.canViewChats.and.returnValue(false);

    expect(component.canViewChats).toBeFalse();
  });

  it('should expose JobChatType to the template', () => {
    expect(component.JobChatType).toBe(JobChatType);
  });

  it('should fetch chats when an opportunity exists and chats are viewable', () => {
    authorizationServiceSpy.canViewChats.and.returnValue(true);
    chatServiceSpy.getOrCreate.calls.reset();

    component.ngOnChanges({
      selectedOpp: new SimpleChange(
        undefined,
        component.selectedOpp,
        true
      )
    });

    expect(chatServiceSpy.getOrCreate).toHaveBeenCalled();
  });

  it('should not fetch chats when no opportunity is selected', () => {
    authorizationServiceSpy.canViewChats.and.returnValue(true);
    chatServiceSpy.getOrCreate.calls.reset();
    component.selectedOpp = null;

    component.ngOnChanges({
      selectedOpp: new SimpleChange(
        makeOpp(),
        null,
        false
      )
    });

    expect(chatServiceSpy.getOrCreate).not.toHaveBeenCalled();
  });

  it('should not fetch chats when chats cannot be viewed', () => {
    authorizationServiceSpy.canViewChats.and.returnValue(false);
    chatServiceSpy.getOrCreate.calls.reset();

    component.ngOnChanges({
      selectedOpp: new SimpleChange(
        undefined,
        component.selectedOpp,
        true
      )
    });

    expect(chatServiceSpy.getOrCreate).not.toHaveBeenCalled();
  });

  it('should request source, destination and all-candidates chats at offer stage', () => {
    chatServiceSpy.getOrCreate.calls.reset();

    component.selectedOpp = makeOpp('offer');

    component.ngOnChanges({
      selectedOpp: new SimpleChange(
        undefined,
        component.selectedOpp,
        true
      )
    });

    expect(component.showDestinationChat).toBeTrue();
    expect(component.showAllChat).toBeTrue();

    expect(chatServiceSpy.getOrCreate).toHaveBeenCalledTimes(3);

    expect(chatServiceSpy.getOrCreate).toHaveBeenCalledWith({
      type: JobChatType.CandidateProspect,
      candidateId: 7
    });

    expect(chatServiceSpy.getOrCreate).toHaveBeenCalledWith({
      type: JobChatType.CandidateRecruiting,
      candidateId: 7,
      jobId: 22
    });

    expect(chatServiceSpy.getOrCreate).toHaveBeenCalledWith({
      type: JobChatType.AllJobCandidates,
      jobId: 22
    });
  });

  it('should store source, destination and all-candidates chats', () => {
    const sourceChat = makeChat(
      1,
      JobChatType.CandidateProspect
    );

    const destinationChat = makeChat(
      2,
      JobChatType.CandidateRecruiting
    );

    const allJobCandidatesChat = makeChat(
      3,
      JobChatType.AllJobCandidates
    );

    chatServiceSpy.getOrCreate.and.returnValues(
      of(sourceChat),
      of(destinationChat),
      of(allJobCandidatesChat)
    );

    component.loading = true;
    component.selectedOpp = makeOpp('offer');

    component.ngOnChanges({
      selectedOpp: new SimpleChange(
        undefined,
        component.selectedOpp,
        true
      )
    });

    expect(component.loading).toBeFalse();
    expect(component.sourceChat).toBe(sourceChat);
    expect(component.destinationChat).toBe(destinationChat);
    expect(component.allJobCandidatesChat)
    .toBe(allJobCandidatesChat);
  });

  it('should request only the source chat before CV review', () => {
    const sourceChat = makeChat(
      1,
      JobChatType.CandidateProspect
    );

    chatServiceSpy.getOrCreate.calls.reset();
    chatServiceSpy.getOrCreate.and.returnValue(of(sourceChat));

    component.selectedOpp = makeOpp('prospect');

    component.ngOnChanges({
      selectedOpp: new SimpleChange(
        undefined,
        component.selectedOpp,
        true
      )
    });

    expect(component.showDestinationChat).toBeFalse();
    expect(component.showAllChat).toBeFalse();
    expect(chatServiceSpy.getOrCreate).toHaveBeenCalledTimes(1);

    expect(chatServiceSpy.getOrCreate).toHaveBeenCalledWith({
      type: JobChatType.CandidateProspect,
      candidateId: 7
    });

    expect(component.sourceChat).toBe(sourceChat);
    expect(component.destinationChat).toBeNull();
    expect(component.allJobCandidatesChat).toBeNull();
  });

  it('should show the destination chat at CV review but not the all-candidates chat', () => {
    const sourceChat = makeChat(
      1,
      JobChatType.CandidateProspect
    );

    const destinationChat = makeChat(
      2,
      JobChatType.CandidateRecruiting
    );

    chatServiceSpy.getOrCreate.calls.reset();

    chatServiceSpy.getOrCreate.and.returnValues(
      of(sourceChat),
      of(destinationChat)
    );

    component.selectedOpp = makeOpp('cvReview');

    component.ngOnChanges({
      selectedOpp: new SimpleChange(
        undefined,
        component.selectedOpp,
        true
      )
    });

    expect(component.showDestinationChat).toBeTrue();
    expect(component.showAllChat).toBeFalse();
    expect(chatServiceSpy.getOrCreate).toHaveBeenCalledTimes(2);

    expect(component.sourceChat).toBe(sourceChat);
    expect(component.destinationChat).toBe(destinationChat);
    expect(component.allJobCandidatesChat).toBeNull();
  });

  it('should store the error and stop loading when fetching chats fails', () => {
    const serviceError = 'Unable to load opportunity chats';

    chatServiceSpy.getOrCreate.and.returnValues(
      throwError(serviceError),
      of(makeChat(2, JobChatType.CandidateRecruiting)),
      of(makeChat(3, JobChatType.AllJobCandidates))
    );

    component.loading = true;
    component.selectedOpp = makeOpp('offer');

    component.ngOnChanges({
      selectedOpp: new SimpleChange(
        undefined,
        component.selectedOpp,
        true
      )
    });

    expect(component.error).toBe(serviceError);
    expect(component.loading).toBeFalse();
  });

  it('should leave the chat request pending until all chats complete', () => {
    const sourceSubject = new Subject<JobChat>();
    const destinationSubject = new Subject<JobChat>();
    const allCandidatesSubject = new Subject<JobChat>();

    chatServiceSpy.getOrCreate.and.returnValues(
      sourceSubject.asObservable(),
      destinationSubject.asObservable(),
      allCandidatesSubject.asObservable()
    );

    component.loading = true;
    component.selectedOpp = makeOpp('offer');

    component.ngOnChanges({
      selectedOpp: new SimpleChange(
        undefined,
        component.selectedOpp,
        true
      )
    });

    expect(component.loading).toBeTrue();

    sourceSubject.next(
      makeChat(1, JobChatType.CandidateProspect)
    );
    sourceSubject.complete();

    destinationSubject.next(
      makeChat(2, JobChatType.CandidateRecruiting)
    );
    destinationSubject.complete();

    expect(component.loading).toBeTrue();

    allCandidatesSubject.next(
      makeChat(3, JobChatType.AllJobCandidates)
    );
    allCandidatesSubject.complete();

    expect(component.loading).toBeFalse();
  });

  it('should return the source chat header translation key', () => {
    expect(
      component.getChatHeaderTranslationKey(
        JobChatType.CandidateProspect
      )
    ).toBe('CANDIDATE-OPPS.TABLE.SOURCE-CHAT');
  });

  it('should return the destination chat header translation key', () => {
    expect(
      component.getChatHeaderTranslationKey(
        JobChatType.CandidateRecruiting
      )
    ).toBe('CANDIDATE-OPPS.TABLE.DESTINATION-CHAT');
  });

  it('should return the all-job-candidates chat header translation key', () => {
    expect(
      component.getChatHeaderTranslationKey(
        JobChatType.AllJobCandidates
      )
    ).toBe(
      'CANDIDATE-OPPS.TABLE.ALL-JOB-CANDIDATES-CHAT'
    );
  });

  it('should return null for an unknown chat type', () => {
    expect(
      component.getChatHeaderTranslationKey(
        null as JobChatType
      )
    ).toBeNull();
  });

  it('should build the candidate opportunity stage translation key', () => {
    expect(
      component.getCandidateOpportunityStageTranslationKey(
        'cvReview'
      )
    ).toBe('CASE-STAGE.CVREVIEW');

    expect(
      component.getCandidateOpportunityStageTranslationKey(
        'offer'
      )
    ).toBe('CASE-STAGE.OFFER');
  });

  it('should clear the opportunity and emit back', () => {
    const backSpy = spyOn(component.back, 'emit');

    component.goBack();

    expect(component.selectedOpp).toBeNull();
    expect(backSpy).toHaveBeenCalledTimes(1);
  });

  it('should report whether the candidate is muted', () => {
    expect(component.isCandidateMuted()).toBeFalse();
  });

  it('should select the source chat', () => {
    const sourceChat = makeChat(
      1,
      JobChatType.CandidateProspect
    );

    component.sourceChat = sourceChat;

    component.setSelectedChatType(
      JobChatType.CandidateProspect
    );

    expect(component.selectedChatType)
    .toBe(JobChatType.CandidateProspect);

    expect(component.selectedChat).toBe(sourceChat);
  });

  it('should select the destination chat', () => {
    const destinationChat = makeChat(
      2,
      JobChatType.CandidateRecruiting
    );

    component.destinationChat = destinationChat;

    component.setSelectedChatType(
      JobChatType.CandidateRecruiting
    );

    expect(component.selectedChatType)
    .toBe(JobChatType.CandidateRecruiting);

    expect(component.selectedChat).toBe(destinationChat);
  });

  it('should select the all-job-candidates chat', () => {
    const allCandidatesChat = makeChat(
      3,
      JobChatType.AllJobCandidates
    );

    component.allJobCandidatesChat = allCandidatesChat;

    component.setSelectedChatType(
      JobChatType.AllJobCandidates
    );

    expect(component.selectedChatType)
    .toBe(JobChatType.AllJobCandidates);

    expect(component.selectedChat).toBe(allCandidatesChat);
  });

  it('should clear the selected chat for an unknown chat type', () => {
    component.selectedChat = makeChat(
      1,
      JobChatType.CandidateProspect
    );

    component.setSelectedChatType(null as JobChatType);

    expect(component.selectedChatType).toBeNull();
    expect(component.selectedChat).toBeNull();
  });

  it('should unselect the current chat', () => {
    component.selectedChat = makeChat(
      1,
      JobChatType.CandidateProspect
    );

    component.unSelectChat();

    expect(component.selectedChat).toBeNull();
  });

  it('should mark the selected chat as read', () => {
    const selectedChat = makeChat(
      1,
      JobChatType.CandidateProspect
    );

    component.selectedChat = selectedChat;

    component.onMarkChatAsRead();

    expect(chatServiceSpy.markChatAsRead)
    .toHaveBeenCalledTimes(1);

    expect(chatServiceSpy.markChatAsRead)
    .toHaveBeenCalledWith(selectedChat);
  });

  it('should pass error and loading states to their components', () => {
    component.error = 'Unable to load chats';
    component.loading = true;
    fixture.detectChanges();

    const errors = fixture.debugElement.queryAll(
      By.directive(ErrorStubComponent)
    );

    const loading = fixture.debugElement.query(
      By.directive(TcLoadingStubComponent)
    ).componentInstance as TcLoadingStubComponent;

    expect(errors.length).toBe(1);

    expect(
      (errors[0].componentInstance as ErrorStubComponent).error
    ).toBe('Unable to load chats');

    expect(loading.loading).toBeTrue();
  });

  it('should hide opportunity and chat details while loading', () => {
    component.loading = true;
    fixture.detectChanges();

    expect(
      fixture.debugElement.query(
        By.directive(TcTableStubComponent)
      )
    ).toBeNull();

    expect(
      fixture.debugElement.query(
        By.directive(ViewChatPostsStubComponent)
      )
    ).toBeNull();
  });

  it('should render source, destination and all-candidates chat rows at offer stage', () => {
    component.selectedOpp = makeOpp('offer');

    component.ngOnChanges({
      selectedOpp: new SimpleChange(
        undefined,
        component.selectedOpp,
        true
      )
    });

    fixture.detectChanges();

    const rows = fixture.debugElement.queryAll(
      By.css('#chats tbody tr')
    );

    expect(rows.length).toBe(3);
  });

  it('should render only the source chat row before CV review', () => {
    component.selectedOpp = makeOpp('prospect');

    component.ngOnChanges({
      selectedOpp: new SimpleChange(
        undefined,
        component.selectedOpp,
        true
      )
    });

    fixture.detectChanges();

    const rows = fixture.debugElement.queryAll(
      By.css('#chats tbody tr')
    );

    expect(rows.length).toBe(1);
  });

  it('should select the source chat when its row is clicked', () => {
    component.selectedOpp = makeOpp('offer');

    component.ngOnChanges({
      selectedOpp: new SimpleChange(
        undefined,
        component.selectedOpp,
        true
      )
    });

    fixture.detectChanges();

    const rows = fixture.debugElement.queryAll(
      By.css('#chats tbody tr')
    );

    rows[0].triggerEventHandler('click', null);

    expect(component.selectedChatType)
    .toBe(JobChatType.CandidateProspect);

    expect(component.selectedChat).toBe(component.sourceChat);
  });

  it('should return to opportunity details from the selected chat view', () => {
    component.selectedChatType =
      JobChatType.CandidateProspect;

    component.selectedChat = makeChat(
      1,
      JobChatType.CandidateProspect
    );

    fixture.detectChanges();

    const buttons = fixture.debugElement.queryAll(
      By.directive(TcButtonStubComponent)
    );

    (buttons[0].componentInstance as TcButtonStubComponent)
    .onClick.emit();

    fixture.detectChanges();

    expect(component.selectedChat).toBeNull();

    expect(
      fixture.debugElement.query(
        By.directive(ViewChatPostsStubComponent)
      )
    ).toBeNull();
  });

  it('should emit back when the opportunity return button is clicked', () => {
    const backSpy = spyOn(component.back, 'emit');

    fixture.detectChanges();

    const button = fixture.debugElement.query(
      By.directive(TcButtonStubComponent)
    ).componentInstance as TcButtonStubComponent;

    button.onClick.emit();

    expect(component.selectedOpp).toBeNull();
    expect(backSpy).toHaveBeenCalledTimes(1);
  });
});
