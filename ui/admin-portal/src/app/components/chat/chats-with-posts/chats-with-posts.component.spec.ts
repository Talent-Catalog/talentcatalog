import {ChatsWithPostsComponent} from "./chats-with-posts.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {ChatService} from "../../../services/chat.service";
import {JobChat} from "../../../model/chat";
import {By} from "@angular/platform-browser";
import {ChatsComponent} from "../chats/chats.component";
import {ViewChatPostsComponent} from "../view-chat-posts/view-chat-posts.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {LocalStorageModule} from "angular-2-local-storage";
import {of} from "rxjs";
import {MockJobChat} from "../../../MockData/MockJobChat";
import {MockChatPost} from "../../../MockData/MockChatPost";
import {ViewPostComponent} from "../view-post/view-post.component";
import {CreateUpdatePostComponent} from "../create-update-post/create-update-post.component";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";
import {QuillModule} from "ngx-quill";
import {MainSidePanelBase} from "../../util/split/MainSidePanelBase";
import {MockUser} from "../../../MockData/MockUser";
import {AuthenticationService} from "../../../services/authentication.service";

fdescribe('ChatsWithPostsComponent', () => {
  let component: ChatsWithPostsComponent;
  let fixture: ComponentFixture<ChatsWithPostsComponent>;
  let chatService: jasmine.SpyObj<ChatService>;
  let authenticationService: jasmine.SpyObj<AuthenticationService>;
  const mockJobChat = new MockJobChat();
  beforeEach(async () => {
    const chatServiceSpy = jasmine.createSpyObj('ChatService', ['markChatAsRead','getChatPosts$','getOrCreate','getChatIsRead$','listPosts']);
    const authSpy = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);

    await TestBed.configureTestingModule({
      declarations: [ ChatsWithPostsComponent, ChatsComponent, ViewChatPostsComponent, ViewPostComponent, CreateUpdatePostComponent ],
      imports: [HttpClientTestingModule,LocalStorageModule.forRoot({}),NgbTooltipModule,FormsModule,ReactiveFormsModule,QuillModule],

      providers: [
        FormBuilder,
        { provide: ChatService, useValue: chatServiceSpy },
        { provide: AuthenticationService, useValue: authSpy }
      ]
    })
    .compileComponents();
    authenticationService = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ChatsWithPostsComponent);
    component = fixture.componentInstance;
    chatService = TestBed.inject(ChatService) as jasmine.SpyObj<ChatService>;
    chatService.getChatPosts$.and.returnValue(of(new MockChatPost()));
    chatService.getChatIsRead$.and.returnValue(of(true));
    authenticationService.getLoggedInUser.and.returnValue((new MockUser()));
    chatService.markChatAsRead.and.returnValue();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render "Select a chat" message when no chat is selected', () => {
    expect(fixture.nativeElement.textContent).toContain('Select a chat');
  });

  it('should render selected chat name when a chat is selected', () => {
    const mockChat: JobChat = { id: 1, name: 'Test Chat' };
    component.selectedChat = mockChat;
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Test Chat');
  });

  it('should render error message when error is present', () => {
    const errorMessage = 'An error occurred!';
    component.error = errorMessage;
    component.selectedChat = mockJobChat;

    fixture.detectChanges();
    const errorElement = fixture.debugElement.query(By.css('.alert-danger'));
    expect(errorElement).toBeTruthy();
    expect(errorElement.nativeElement.textContent.trim()).toBe(errorMessage);
  });

  it('should emit chatSelection event when a chat is selected', () => {
    const mockChat: JobChat = { id: 1, name: 'Test Chat' };
    spyOn(component.chatSelection, 'emit');
    component.onChatSelected(mockChat);
    expect(component.selectedChat).toEqual(mockChat);
    expect(component.chatSelection.emit).toHaveBeenCalledWith(mockChat);
  });

  it('should call chatService.markChatAsRead when a chat is marked as read', () => {
    const mockChat: JobChat = { id: 1, name: 'Test Chat' };
    component.selectedChat = mockChat;
    fixture.detectChanges();
    component.onMarkChatAsRead();
    expect(chatService.markChatAsRead).toHaveBeenCalledWith(mockChat);
  });


  it('should return true from canToggleSizes method when minSidePanelWidth and maxSidePanelWidth are different', () => {
    // Create an instance of MainSidePanelBase with different min and max widths
    // @ts-expect-error
    const sidePanelBase = new MainSidePanelBase(100, 200);
    expect(sidePanelBase.canToggleSizes()).toBe(true);
  });

  it('should return false from canToggleSizes method when minSidePanelWidth and maxSidePanelWidth are the same', () => {
    // Create an instance of MainSidePanelBase with same min and max widths
    // @ts-expect-error
    const sidePanelBase = new MainSidePanelBase(100, 100);
    expect(sidePanelBase.canToggleSizes()).toBe(false);
  });
});
