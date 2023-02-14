import {ComponentFixture, TestBed} from '@angular/core/testing';

import {JobTitleComponent} from './job-title.component';

describe('JobTitleComponent', () => {
  let component: JobTitleComponent;
  let fixture: ComponentFixture<JobTitleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobTitleComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobTitleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
