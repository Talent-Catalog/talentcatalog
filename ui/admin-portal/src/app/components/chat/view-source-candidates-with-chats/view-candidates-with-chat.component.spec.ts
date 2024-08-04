import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewCandidatesWithChatComponent } from './view-candidates-with-chat.component';

describe('ViewSourceCandidatesWithChatsComponent', () => {
  let component: ViewCandidatesWithChatComponent;
  let fixture: ComponentFixture<ViewCandidatesWithChatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewCandidatesWithChatComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidatesWithChatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
