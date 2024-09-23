import {ViewChatComponent} from "./view-chat.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {JobChat} from "../../../model/chat";

describe('ViewChatComponent', () => {
  let component: ViewChatComponent;
  let fixture: ComponentFixture<ViewChatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ViewChatComponent],
      schemas: [NO_ERRORS_SCHEMA] // To ignore subcomponent and directive errors
    }).compileComponents();

    fixture = TestBed.createComponent(ViewChatComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display chat name if available', () => {
    const mockChat: JobChat = { id: 1, name: 'Test Chat' };
    component.chat = mockChat;

    fixture.detectChanges();

    const compiled = fixture.nativeElement;
    expect(compiled.textContent).toContain('Test Chat');
  });

  it('should display chat id if name is not available', () => {
    const mockChat: JobChat = { id: 1, name: '' };
    component.chat = mockChat;

    fixture.detectChanges();

    const compiled = fixture.nativeElement;
    expect(compiled.textContent).toContain(1);
  });

  it('should display chat-read-status component with correct input', () => {
    const mockChat: JobChat = { id: 1, name: 'Test Chat' };
    component.chat = mockChat;

    fixture.detectChanges();

    const chatReadStatusComponent = fixture.debugElement.nativeElement.querySelector('app-chat-read-status');
    expect(chatReadStatusComponent).toBeTruthy();
  });

  it('should have a working displayName getter', () => {
    let mockChat: JobChat = { id: 1, name: 'Test Chat' };
    component.chat = mockChat;
    expect(component.displayName).toBe('Test Chat');

    mockChat = { id: 1, name: '' };
    component.chat = mockChat;
    expect(component.displayName).toBe(1);
  });
});
