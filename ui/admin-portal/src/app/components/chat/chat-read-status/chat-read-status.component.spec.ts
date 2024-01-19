import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ChatReadStatusComponent} from './chat-read-status.component';

describe('ChatReadStatusComponent', () => {
  let component: ChatReadStatusComponent;
  let fixture: ComponentFixture<ChatReadStatusComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ChatReadStatusComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ChatReadStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
