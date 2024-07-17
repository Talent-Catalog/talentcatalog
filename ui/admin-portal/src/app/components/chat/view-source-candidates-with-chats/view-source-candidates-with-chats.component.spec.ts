import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewSourceCandidatesWithChatsComponent } from './view-source-candidates-with-chats.component';

describe('ViewSourceCandidatesWithChatsComponent', () => {
  let component: ViewSourceCandidatesWithChatsComponent;
  let fixture: ComponentFixture<ViewSourceCandidatesWithChatsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewSourceCandidatesWithChatsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewSourceCandidatesWithChatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
