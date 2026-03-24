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
import {EMPTY, of} from 'rxjs';
import {ViewChatPostsComponent} from './view-chat-posts.component';
import {ChatService} from '../../../services/chat.service';
import {ChatPostService} from '../../../services/chat-post.service';
import {JobChatType} from '../../../model/chat';

@Pipe({name: 'translate'})
class TranslatePipeStub implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

@Component({selector: 'tc-loading', template: ''})
class TcLoadingStubComponent {
  @Input() loading = false;
}

@Component({selector: 'tc-alert', template: '<ng-content></ng-content>'})
class TcAlertStubComponent {
  @Input() type?: string;
}

@Component({
  selector: 'tc-button',
  template: '<button (click)="onClick.emit()"><ng-content></ng-content></button>',
})
class TcButtonStubComponent {
  @Input() color?: string;
  @Input() type?: string;
  @Input() size?: string;
  @Output() onClick = new EventEmitter<void>();
}

@Component({selector: 'tc-icon', template: '<ng-content></ng-content>'})
class TcIconStubComponent {
  @Input() color?: string;
}

@Component({selector: 'app-view-post', template: ''})
class ViewPostStubComponent {
  @Input() readOnly = false;
  @Input() post: unknown;
}

@Component({selector: 'app-create-update-post', template: ''})
class CreateUpdatePostStubComponent {
  @Input() chat: unknown;
}

describe('ViewChatPostsComponent', () => {
  let component: ViewChatPostsComponent;
  let fixture: ComponentFixture<ViewChatPostsComponent>;
  let chatServiceSpy: jasmine.SpyObj<ChatService>;
  let chatPostServiceSpy: jasmine.SpyObj<ChatPostService>;

  beforeEach(async () => {
    chatServiceSpy = jasmine.createSpyObj('ChatService', ['getOrCreate', 'getChatPosts$', 'getChatIsRead$', 'markChatAsRead']);
    chatPostServiceSpy = jasmine.createSpyObj('ChatPostService', ['listPosts']);

    chatServiceSpy.getOrCreate.and.returnValue(of({id: 1, type: JobChatType.CandidateProspect} as any));
    chatServiceSpy.getChatPosts$.and.returnValue(EMPTY);
    chatServiceSpy.getChatIsRead$.and.returnValue(of(false));
    chatPostServiceSpy.listPosts.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      declarations: [
        ViewChatPostsComponent,
        TranslatePipeStub,
        TcLoadingStubComponent,
        TcAlertStubComponent,
        TcButtonStubComponent,
        TcIconStubComponent,
        ViewPostStubComponent,
        CreateUpdatePostStubComponent,
      ],
      providers: [
        {provide: ChatService, useValue: chatServiceSpy},
        {provide: ChatPostService, useValue: chatPostServiceSpy},
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewChatPostsComponent);
    component = fixture.componentInstance;
    component.chat = {id: 44, type: JobChatType.CandidateProspect} as any;
    component.ngOnChanges({
      chat: {
        previousValue: null,
        currentValue: component.chat,
        firstChange: true,
        isFirstChange: () => true,
      },
    });
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render tc-loading, tc-alert and the mark-as-read tc-button', () => {
    component.error = 'Chat failed';
    component.readOnly = false;
    component.chatIsRead = false;
    fixture.detectChanges();

    const loading = fixture.debugElement.query(By.directive(TcLoadingStubComponent));
    const alert = fixture.debugElement.query(By.directive(TcAlertStubComponent));
    const button = fixture.debugElement.query(By.directive(TcButtonStubComponent));

    expect(loading).toBeTruthy();
    expect(alert).toBeTruthy();
    expect((alert.componentInstance as TcAlertStubComponent).type).toBe('danger');
    expect(button).toBeTruthy();
    expect((button.componentInstance as TcButtonStubComponent).type).toBe('outline');
    expect((button.componentInstance as TcButtonStubComponent).color).toBe('secondary');
  });

  it('should render view posts and the create-update-post child when editable', () => {
    component.groupedMessages = [
      {
        date: 'Monday, Jan 1, 2024',
        messages: [{id: 9, createdDate: new Date(), content: 'Hello'} as any],
      },
    ];
    component.posts = component.groupedMessages[0].messages as any;
    component.readOnly = false;
    fixture.detectChanges();

    const post = fixture.debugElement.query(By.directive(ViewPostStubComponent));
    const editor = fixture.debugElement.query(By.directive(CreateUpdatePostStubComponent));

    expect(post).toBeTruthy();
    expect(editor).toBeTruthy();
    expect((editor.componentInstance as CreateUpdatePostStubComponent).chat).toBe(component.chat);
  });

  it('should mark the current chat as read', () => {
    component.onMarkChatAsRead();

    expect(chatServiceSpy.markChatAsRead).toHaveBeenCalledWith(component.chat);
  });
});
