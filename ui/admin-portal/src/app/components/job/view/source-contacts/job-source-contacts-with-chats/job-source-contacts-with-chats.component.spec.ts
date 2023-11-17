import {ComponentFixture, TestBed} from '@angular/core/testing';

import {JobSourceContactsWithChatsComponent} from './job-source-contacts-with-chats.component';

describe('JobSourceContactsWithChatsComponent', () => {
  let component: JobSourceContactsWithChatsComponent;
  let fixture: ComponentFixture<JobSourceContactsWithChatsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobSourceContactsWithChatsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobSourceContactsWithChatsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
