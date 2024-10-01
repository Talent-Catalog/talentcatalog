import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ChatsComponent} from './chats.component';
import {By} from '@angular/platform-browser';
import {JobChat} from "../../../model/chat";
import {MockJobChat} from "../../../MockData/MockJobChat";
import {ViewChatComponent} from "../view-chat/view-chat.component";
import {ChatReadStatusComponent} from "../chat-read-status/chat-read-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {LocalStorageModule} from "angular-2-local-storage";
import {ChatService} from "../../../services/chat.service";
import {TranslateModule} from "@ngx-translate/core";

describe('ChatsComponent', () => {
  let component: ChatsComponent;
  let fixture: ComponentFixture<ChatsComponent>;
  let chatService: jasmine.SpyObj<ChatService>;
  const mockJobChat = new MockJobChat();
  beforeEach(async () => {
    const chatServiceSpy = jasmine.createSpyObj('ChatService',
      ['getChatInfoParticipantsKey','getChatInfoPurposeKey','getJobChatUserInfo',
        'getChatIsRead$']);

    await TestBed.configureTestingModule({
      declarations: [ ChatsComponent,ViewChatComponent,ChatReadStatusComponent ],
      imports: [HttpClientTestingModule,LocalStorageModule.forRoot({}),
        TranslateModule.forRoot({})],
      providers:[
        { provide: ChatService, useValue: chatServiceSpy }
      ]
    })
    .compileComponents();
    chatService = TestBed.inject(ChatService) as jasmine.SpyObj<ChatService>;

  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ChatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render loading spinner when loading is true', () => {
    component.loading = true;
    fixture.detectChanges();
    const spinnerElement = fixture.debugElement.query(By.css('.fa-spinner'));
    expect(spinnerElement).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('loading...');
  });

  it('should render error message when error is present', () => {
    const errorMessage = 'An error occurred!';
    component.error = errorMessage;
    fixture.detectChanges();
    const errorElement = fixture.debugElement.query(By.css('.alert-danger'));
    expect(errorElement).toBeTruthy();
    expect(errorElement.nativeElement.textContent.trim()).toBe(errorMessage);
  });

  it('should render chats when chats are provided', () => {
    const mockChats: JobChat[] = [mockJobChat];
    component.chats = mockChats;
    fixture.detectChanges();
    const chatRows = fixture.debugElement.queryAll(By.css('li'));
    expect(chatRows.length).toBe(mockChats.length);
  });

  it('should emit chatSelection event when a chat is selected', () => {
    const mockChat: JobChat = mockJobChat;
    spyOn(component.chatSelection, 'emit');
    component.selectCurrent(mockChat);
    expect(component.currentChat).toEqual(mockChat);
    expect(component.chatSelection.emit).toHaveBeenCalledWith(mockChat);
  });

  it('should select the first chat as current when chats are provided', () => {
    const mockChats: JobChat[] = [mockJobChat];
    component.chats = mockChats;
    component.ngOnChanges({});
    fixture.detectChanges();
    expect(component.currentChat).toEqual(mockChats[0]);
  });
});
