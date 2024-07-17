import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SourceCandidateChatsComponent } from './source-candidate-chats.component';

describe('SourceCandidateChatsComponent', () => {
  let component: SourceCandidateChatsComponent;
  let fixture: ComponentFixture<SourceCandidateChatsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SourceCandidateChatsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SourceCandidateChatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
