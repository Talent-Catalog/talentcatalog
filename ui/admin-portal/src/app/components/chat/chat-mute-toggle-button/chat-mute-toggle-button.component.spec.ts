import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChatMuteToggleButtonComponent } from './chat-mute-toggle-button.component';

describe('MuteToggleButtonComponent', () => {
  let component: ChatMuteToggleButtonComponent;
  let fixture: ComponentFixture<ChatMuteToggleButtonComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ChatMuteToggleButtonComponent]
    });
    fixture = TestBed.createComponent(ChatMuteToggleButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
