/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, EventEmitter, Input, Output, Pipe, PipeTransform} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {of} from 'rxjs';
import {CandidateOppComponent} from './candidate-opp.component';
import {AuthorizationService} from '../../../../../../../services/authorization.service';
import {ChatService} from '../../../../../../../services/chat.service';
import {JobChatType} from '../../../../../../../model/chat';

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
  template: '<button (click)="onClick.emit()"><ng-content></ng-content></button>',
})
class TcButtonStubComponent {
  @Input() size?: string;
  @Input() type?: string;
  @Input() color?: string;
  @Output() onClick = new EventEmitter<void>();
}

@Component({selector: 'tc-label', template: '<label><ng-content></ng-content></label>'})
class TcLabelStubComponent {
  @Input() for?: string;
}

@Component({selector: 'tc-table', template: '<ng-content></ng-content>'})
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

  const makeCandidate = () => ({
    id: 7,
    muted: false,
  } as any);

  const makeOpp = () => ({
    id: 11,
    stage: 'offer',
    lastActiveStage: 'offer',
    fileOfferLink: 'https://example.com/offer.pdf',
    fileOfferName: 'Offer PDF',
    closingCommentsForCandidate: 'Helpful notes',
    jobOpp: {
      id: 22,
      name: 'Mountain hotel role',
      submissionList: {
        fileInterviewGuidanceLink: 'https://example.com/interview.pdf',
        fileInterviewGuidanceName: 'Interview guide',
      },
    },
  } as any);

  beforeEach(async () => {
    authorizationServiceSpy = jasmine.createSpyObj('AuthorizationService', ['canViewChats']);
    chatServiceSpy = jasmine.createSpyObj('ChatService', ['getOrCreate', 'markChatAsRead']);
    authorizationServiceSpy.canViewChats.and.returnValue(true);
    chatServiceSpy.getOrCreate.and.callFake((request: any) => of({id: request.type + 1, type: request.type} as any));

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
        ChatReadStatusStubComponent,
      ],
      providers: [
        {provide: AuthorizationService, useValue: authorizationServiceSpy},
        {provide: ChatService, useValue: chatServiceSpy},
      ],
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
    expect(fixture.debugElement.query(By.directive(TcLoadingStubComponent))).toBeTruthy();
    const table = fixture.debugElement.query(By.directive(TcTableStubComponent)).componentInstance as TcTableStubComponent;
    expect(table).toBeTruthy();
    expect(table.striped).toBeTrue();

    const buttons = fixture.debugElement.queryAll(By.directive(TcButtonStubComponent));
    const labels = fixture.debugElement.queryAll(By.directive(TcLabelStubComponent));

    expect(buttons.length).toBe(1);
    expect((buttons[0].componentInstance as TcButtonStubComponent).size).toBe('sm');
    expect(labels.length).toBe(4);
    expect(fixture.nativeElement.textContent).toContain('Mountain hotel role');
  });

  it('should render the selected chat view with tc-button return actions', () => {
    component.selectedChatType = JobChatType.CandidateProspect;
    component.selectedChat = {id: 99, type: JobChatType.CandidateProspect} as any;
    fixture.detectChanges();

    const chatView = fixture.debugElement.query(By.directive(ViewChatPostsStubComponent));
    const buttons = fixture.debugElement.queryAll(By.directive(TcButtonStubComponent));

    expect(chatView).toBeTruthy();
    expect((chatView.componentInstance as ViewChatPostsStubComponent).chat).toBe(component.selectedChat);
    expect((chatView.componentInstance as ViewChatPostsStubComponent).readOnly).toBeFalse();
    expect(buttons.length).toBe(2);
  });

  it('should emit back when goBack is called', () => {
    spyOn(component.back, 'emit');

    component.goBack();

    expect(component.selectedOpp).toBeNull();
    expect(component.back.emit).toHaveBeenCalled();
  });
});
