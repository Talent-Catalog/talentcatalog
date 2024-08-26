import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CandidatesWithChatComponent } from './candidates-with-chat.component';

describe('CandidatesWithChatComponent', () => {
  let component: CandidatesWithChatComponent;
  let fixture: ComponentFixture<CandidatesWithChatComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CandidatesWithChatComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidatesWithChatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
