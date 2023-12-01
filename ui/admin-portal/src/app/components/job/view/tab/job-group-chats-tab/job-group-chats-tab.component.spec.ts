import {ComponentFixture, TestBed} from '@angular/core/testing';

import {JobGroupChatsTabComponent} from './job-group-chats-tab.component';

describe('JobGroupChatsTabComponent', () => {
  let component: JobGroupChatsTabComponent;
  let fixture: ComponentFixture<JobGroupChatsTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobGroupChatsTabComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobGroupChatsTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
