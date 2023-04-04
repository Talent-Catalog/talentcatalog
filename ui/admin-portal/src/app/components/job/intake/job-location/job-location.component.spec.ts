import {ComponentFixture, TestBed} from '@angular/core/testing';

import {JobLocationComponent} from './job-location.component';

describe('JobLocationComponent', () => {
  let component: JobLocationComponent;
  let fixture: ComponentFixture<JobLocationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobLocationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobLocationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
