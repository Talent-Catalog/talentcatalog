import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChatConsoleComponent } from './chat-console.component';

describe('ChatConsoleComponent', () => {
  let component: ChatConsoleComponent;
  let fixture: ComponentFixture<ChatConsoleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ChatConsoleComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ChatConsoleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
