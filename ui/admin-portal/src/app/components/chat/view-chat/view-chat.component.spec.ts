import {ViewChatComponent} from "./view-chat.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {JobChat, JobChatType} from "../../../model/chat";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {TranslateModule} from "@ngx-translate/core";
import {LocalStorageModule} from "angular-2-local-storage";

describe('ViewChatComponent', () => {
  let component: ViewChatComponent;
  let fixture: ComponentFixture<ViewChatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, TranslateModule.forRoot({}),
        LocalStorageModule.forRoot({})],
      declarations: [ViewChatComponent],
      schemas: [NO_ERRORS_SCHEMA] // To ignore subcomponent and directive errors
    }).compileComponents();

    fixture = TestBed.createComponent(ViewChatComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display chat-read-status component with correct input', () => {
    const mockChat: JobChat = { id: 1, type: JobChatType.CandidateProspect, name: 'Test Chat' };
    component.chat = mockChat;

    fixture.detectChanges();

    const chatReadStatusComponent = fixture.debugElement.nativeElement.querySelector('app-chat-read-status');
    expect(chatReadStatusComponent).toBeTruthy();
  });
});
