import {ComponentFixture, TestBed} from '@angular/core/testing';

import {JobIntakeTabComponent} from './job-intake-tab.component';

describe('JobIntakeTabComponent', () => {
  let component: JobIntakeTabComponent;
  let fixture: ComponentFixture<JobIntakeTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobIntakeTabComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobIntakeTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
