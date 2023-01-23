import {ComponentFixture, TestBed} from '@angular/core/testing';

import {JobSourceContactsTabComponent} from './job-source-contacts-tab.component';

describe('JobSourceContactsTabComponent', () => {
  let component: JobSourceContactsTabComponent;
  let fixture: ComponentFixture<JobSourceContactsTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobSourceContactsTabComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobSourceContactsTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
