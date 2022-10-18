import {ComponentFixture, TestBed} from '@angular/core/testing';

import {JobsWithDetailComponent} from './jobs-with-detail.component';

describe('JobsWithDetailComponent', () => {
  let component: JobsWithDetailComponent;
  let fixture: ComponentFixture<JobsWithDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ JobsWithDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(JobsWithDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
