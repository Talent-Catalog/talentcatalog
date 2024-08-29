import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShowCandidatesWithChatComponent } from './show-candidates-with-chat.component';

describe('ShowCandidatesWithChatComponent', () => {
  let component: ShowCandidatesWithChatComponent;
  let fixture: ComponentFixture<ShowCandidatesWithChatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ShowCandidatesWithChatComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ShowCandidatesWithChatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
