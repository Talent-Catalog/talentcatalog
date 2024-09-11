import {By} from '@angular/platform-browser';
import {ChatReadStatusComponent} from "./chat-read-status.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {ChatService} from "../../../services/chat.service";
import {JobChat} from "../../../model/chat";
import {MockJobChat} from "../../../MockData/MockJobChat";
import {of, throwError} from "rxjs";
import {DebugElement, SimpleChange} from "@angular/core";

describe('ChatReadStatusComponent', () => {
  let component: ChatReadStatusComponent;
  let fixture: ComponentFixture<ChatReadStatusComponent>;
  let chatService: jasmine.SpyObj<ChatService>;
  const mockJobChats = new MockJobChat();
  beforeEach(() => {
    const chatServiceSpy = jasmine.createSpyObj('ChatService', ['combineChatReadStatuses', 'getChatIsRead$']);

    TestBed.configureTestingModule({
      declarations: [ChatReadStatusComponent],
      providers: [
        { provide: ChatService, useValue: chatServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ChatReadStatusComponent);
    component = fixture.componentInstance;
    component.chats = [mockJobChats];
    chatService = TestBed.inject(ChatService) as jasmine.SpyObj<ChatService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set unreadIndicator to * if any chat is unread', () => {
    const chats: JobChat[] = [mockJobChats,mockJobChats];
    const combinedStatus = of(false);
    chatService.combineChatReadStatuses.and.returnValue(combinedStatus);

    component.chats = chats;
    component.ngOnChanges({ chats: new SimpleChange(null, chats, true) });
    expect(chatService.combineChatReadStatuses).toHaveBeenCalledWith(chats);
    expect(component.unreadIndicator).toBe('*');
  });

  it('should set unreadIndicator to blank if all chats are read', () => {
    const chats: JobChat[] = [{ id: 1 }, { id: 2 }];
    const combinedStatus = of(true);
    chatService.combineChatReadStatuses.and.returnValue(combinedStatus);

    component.chats = chats;

    component.ngOnChanges({ chats: new SimpleChange(null, chats, true) });
    expect(chatService.combineChatReadStatuses).toHaveBeenCalledWith(chats);
    expect(component.unreadIndicator).toBe('');
  });

  it('should set unreadIndicator to ? if chat status is unknown', () => {
    const chats: JobChat[] = [{ id: 1 }, { id: 2 }];
    const error = throwError('Error');
    chatService.combineChatReadStatuses.and.returnValue(error);

    component.chats = chats;
    component.ngOnChanges({ chats: new SimpleChange(null, chats, true) });
    expect(chatService.combineChatReadStatuses).toHaveBeenCalledWith(chats);
    expect(component.unreadIndicator).toBe('?');
  });

  it('should set unreadIndicator to * if observable emits false', () => {
    const observable = of(false);
    chatService.getChatIsRead$.and.returnValue(observable);

    component.observable = observable;
    component.ngOnChanges({ observable: new SimpleChange(null, observable, true) });
    expect(component.unreadIndicator).toBe('*');
  });

  it('should set unreadIndicator to blank if observable emits true', () => {
    const observable = of(true);
    chatService.getChatIsRead$.and.returnValue(observable);

    component.observable = observable;
    component.ngOnChanges({ observable: new SimpleChange(null, observable, true) });
    expect(component.unreadIndicator).toBe('');
  });

  it('should set unreadIndicator to ? if observable emits an error', () => {
    const observable = throwError('Error');
    chatService.getChatIsRead$.and.returnValue(observable);

    component.observable = observable;
    component.ngOnChanges({ observable: new SimpleChange(null, observable, true) });
    expect(component.unreadIndicator).toBe('?');
  });

  it('should display notification-dot class when unreadIndicator is *', () => {
    component.unreadIndicator = '*';
    fixture.detectChanges();
    const spanEl: DebugElement = fixture.debugElement.query(By.css('.notification-dot'));
    expect(spanEl).toBeTruthy();
  });

  it('should display unreadIndicator value when unreadIndicator is ?', () => {
    component.unreadIndicator = '?';
    fixture.detectChanges();
    const spanEl: DebugElement = fixture.debugElement.query(By.css('span'));
    expect(spanEl.nativeElement.textContent.trim()).toBe('?');
  });
});
