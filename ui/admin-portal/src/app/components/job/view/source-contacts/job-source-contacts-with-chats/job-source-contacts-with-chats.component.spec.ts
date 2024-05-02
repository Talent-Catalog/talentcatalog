import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { JobSourceContactsWithChatsComponent } from './job-source-contacts-with-chats.component';
import { AuthenticationService } from '../../../../../services/authentication.service';
import { AuthorizationService } from '../../../../../services/authorization.service';
import { ChatService } from '../../../../../services/chat.service';
import { Partner } from '../../../../../model/partner';
import {JobChatType, CreateChatRequest, JobChat} from '../../../../../model/chat';
import { Job } from '../../../../../model/job';
import { of } from 'rxjs';
import {MockUser} from "../../../MockData/MockUser";
import {MockJob} from "../../../MockData/MockJob";
import {
  ViewJobSourceContactsComponent
} from "../view-job-source-contacts/view-job-source-contacts.component";
import {PartnerService} from "../../../../../services/partner.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {MockPartner} from "../../../MockData/MockPartner";
import {ViewPostComponent} from "../../../../chat/view-post/view-post.component";
import {ViewChatPostsComponent} from "../../../../chat/view-chat-posts/view-chat-posts.component";
import {
  CreateUpdatePostComponent
} from "../../../../chat/create-update-post/create-update-post.component";
import {
  FormBuilder,
  FormControl,
  FormsModule,
  NgControl,
  ReactiveFormsModule
} from "@angular/forms";
import {QuillModule} from "ngx-quill";
import {MockJobChat} from "../../../MockData/MockJobChat";

fdescribe('JobSourceContactsWithChatsComponent', () => {
  let component: JobSourceContactsWithChatsComponent;
  let fixture: ComponentFixture<JobSourceContactsWithChatsComponent>;
  let chatServiceSpy: jasmine.SpyObj<ChatService>;
  let partnerService: jasmine.SpyObj<PartnerService>;
   beforeEach(waitForAsync(() => {
    const authSpy = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);
    const authUser = { partner: MockPartner}; // Mock authenticated user
    authSpy.getLoggedInUser.and.returnValue(authUser);

    const authzSpy = jasmine.createSpyObj('AuthorizationService', ['isSourcePartner', 'isDefaultSourcePartner', 'isJobCreator']);
    authzSpy.isSourcePartner.and.returnValue(true);
    authzSpy.isDefaultSourcePartner.and.returnValue(false);

    const chatSpy = jasmine.createSpyObj('ChatService', ['getOrCreate','getChatIsRead$','getChatPosts$', 'markChatAsRead']);
    chatSpy.getOrCreate.and.returnValue(of(MockJobChat)); // Mock chat service response
    chatSpy.getChatPosts$.and.returnValue(of(MockJobChat)); // Mock chat service response
    chatSpy.getChatIsRead$.and.returnValue(of(MockJobChat)); // Mock chat service response
      partnerService = jasmine.createSpyObj('PartnerService',['listSourcePartners']);
    partnerService.listSourcePartners.and.returnValue(of([])); // Mock listSourcePartners response

     // partnerService.listSourcePartners.and.returnValue();
    TestBed.configureTestingModule({
      declarations: [JobSourceContactsWithChatsComponent,ViewPostComponent,CreateUpdatePostComponent,ViewJobSourceContactsComponent,ViewChatPostsComponent],
      imports: [HttpClientTestingModule,QuillModule,ReactiveFormsModule ],
      providers: [
        { provide: AuthenticationService, useValue: authSpy },
        { provide: AuthorizationService, useValue: authzSpy },
        { provide: ChatService, useValue: chatSpy },
        { provide: PartnerService, useValue: partnerService },
        { provide: FormBuilder },

      ]
    })
    .compileComponents();

  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JobSourceContactsWithChatsComponent);
    component = fixture.componentInstance;
    chatServiceSpy = TestBed.inject(ChatService) as jasmine.SpyObj<ChatService>;
    partnerService = TestBed.inject(PartnerService) as jasmine.SpyObj<PartnerService>;
    component.job = MockJob;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch job chat when source partner is selected', () => {
    const mockJob: Job = MockJob// Create a mock job
    const mockPartner: Partner = new MockPartner(); // Create a mock partner
    component.job = mockJob;

    component.onSourcePartnerSelected(mockPartner);

    const expectedRequest: CreateChatRequest = {
      type: JobChatType.JobCreatorSourcePartner,
      jobId: mockJob.id,
      sourcePartnerId: mockPartner.id
    };
      expect(chatServiceSpy.getOrCreate).toHaveBeenCalledWith(expectedRequest);
  });
  //
  it('should update chat header based on user and selected partner', () => {
    const mockJob: Job = MockJob// Create a mock job
    const mockPartner: Partner = new MockPartner(); // Create a mock partner
    mockPartner.name = 'XYZ';
    component.job = mockJob;
    component.onSourcePartnerSelected(mockPartner);
    expect(component.chatHeader).toBe('Chat with XYZ Partner');
  });

  it('should mark chat as read when requested', () => {
    const mockChat:JobChat = MockJobChat; // Mock chat object
    component.selectedSourcePartnerChat = mockChat;

    component.onMarkChatAsRead();

    expect(chatServiceSpy.markChatAsRead).toHaveBeenCalledWith(mockChat);
  });
});
